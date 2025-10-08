(ns ttt-server.handlers
  (:require [hiccup2.core :as h]
            [ttt-server.sessions :as session]
            [ttt-server.game :as game])
  (:import (java.util UUID)
           (server Request)))

(def game-styles
  "
body {
  display: flex;
  flex-direction: column;
  align-items: center;
  font-family: sans-serif;
}
#board {
  display: grid;
  grid-template-columns: repeat(3, 100px);
  grid-gap: 5px;
  margin-top: 20px;
}
.square {
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 2em;
  background: #fff;
  border: 2px solid #333;
  width: 100px;
  height: 100px;
  text-align: center;
  cursor: pointer;
}
button.reset {
  margin-top: 15px;
  padding: 8px 16px;
  font-size: 1em;
}
")

(defn ->button-square [board winner draw row col]
  (let [move     (get-in board [row col])
        disable? (or move winner draw)]
    [:form {:method   "POST" :action "/move" :style "display:inline;"
            :onsubmit (when disable? "return false;")}
     [:input {:name "row" :type "hidden" :value row}]
     [:input {:name "col" :type "hidden" :value col}]
     [:button.square {:type "submit"} (or move "")]]))

(defn ->button-row [board winner draw row]
  (for [col (range 3)]
    (->button-square board winner draw row col)))

(defn ->button-grid [board winner draw]
  (apply concat
         (for [row (range 3)]
           (->button-row board winner draw row))))

(defn reset-form []
  [:form {:method "POST" :action "/reset"}
   [:button.reset {:type "submit"} "Reset"]])

(defn ->status-message [current-token winner draw]
  (cond
    winner (str "Winner: " winner)
    draw "Draw!"
    :else (str "Turn: " current-token)))

(defn ->game-handler-body [board winner draw status-msg]
  (str (h/html
         [:html
          [:head
           [:meta {:charset "UTF-8"}]
           [:title "Tic Tac Toe"]
           [:style game-styles]]
          [:body
           [:h1 "Tic Tac Toe"]
           [:div#board
            (->button-grid board winner draw)]
           [:div#status status-msg]
           (reset-form)]])))

(defn game-handler [session-id game]
  (let [board      (or (:board game) game/starting-board)
        {:keys [current-token winner draw]} game
        status-msg (->status-message current-token winner draw)]
    {:status  200
     :headers {"Set-Cookie" (str "sessionId=" session-id)}
     :body    (->game-handler-body board winner draw status-msg)}))

(defn game-request-handler [^Request req]
  (session/resume-or-new-session req game-handler))

(defn parse-move [params]
  (let [row-str (get params "row")
        col-str (get params "col")]
    [(Integer/parseInt row-str)
     (Integer/parseInt col-str)]))

(defn handle-move [session-id game params]
  (let [move         (parse-move params)
        updated-game (if move
                       (game/update-game-state game move)
                       game)]
    (swap! session/sessions assoc session-id updated-game)
    (game-handler session-id updated-game)))

(defn move-handler [^Request req]
  (let [params (.getParams req)]
    (session/resume-or-new-session req
                                   (fn [session-id game]
                            (handle-move session-id game params)))))

(defn reset-handler [^Request req]
  (let [[session-id] (session/get-or-create-session req)]
    (session/reset-session session-id)
    {:status  302
     :headers {"Location"   "/"
               "Set-Cookie" (str "sessionId=" session-id)}
     :body    ""}))


(defn fallback-handler [_]
  {:status 404
   :body   (str (h/html [:h1 "Route Not Found"]))})