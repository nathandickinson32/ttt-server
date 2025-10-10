(ns ttt-server.handlers
  (:require [hiccup2.core :as h]
            [ttt-server.game :as game]
            [ttt-server.sessions :as session]))

(def game-styles (delay (slurp "resources/public/stylesheet.css")))

(defn ->button-square [board winner draw row col]
  (let [move     (get-in board [row col])
        disable? (or move winner draw)]
    [:form {:method   "POST"
            :action   "/move"
            :style    "display:inline;"
            :onsubmit (when disable? "return false;")}
     [:input {:name "row" :type "hidden" :value row}]
     [:input {:name "col" :type "hidden" :value col}]
     [:button.square {:type "submit"} (or move "")]]))

(defn ->button-grid [board winner draw]
  (for [row (range 3)
        col (range 3)]
    (->button-square board winner draw row col)))

(defn ->status-message [current-token winner draw]
  (cond
    winner (str "Winner: " winner)
    draw "Draw!"
    :else (str "Turn: " current-token)))

(defn ->game-handler-body [game]
  (let [{:keys [current-token winner draw board]} game
        board  (or board game/starting-board)
        status (->status-message current-token winner draw)]
    [:html
     [:head
      [:meta {:charset "UTF-8"}]
      [:title "Tic Tac Toe"]
      [:style @game-styles]]
     [:body
      [:h1 "Tic Tac Toe"]
      [:div#board (->button-grid board winner draw)]
      [:div#status status]
      [:form {:method "POST" :action "/reset"}
       [:button.reset {:type "submit"} "Reset"]]]]))

(defn game-handler [session-id game]
  {:status  200
   :headers {"Set-Cookie" (str "sessionId=" session-id)}
   :body    (-> game ->game-handler-body h/html str)})

(defn game-request-handler [request]
  (session/resume-or-new-session request game-handler))

(defn parse-move [params]
  (let [row-str (get params :row)
        col-str (get params :col)]
    [(Integer/parseInt row-str)
     (Integer/parseInt col-str)]))

(defn handle-move [session-id game params]
  (let [move         (parse-move params)
        updated-game (if move
                       (game/update-or-ensure-game game move)
                       game)]
    (session/set-session session-id updated-game)
    (game-handler session-id updated-game)))

(defn move-handler [request]
  (let [params (:params request)]
    (session/resume-or-new-session request
                                   (fn [session-id game]
                                     (handle-move session-id game params)))))

(defn reset-requested-game [{:keys [session-id]}]
  (session/reset-session session-id)
  {:status  302
   :headers {"Location"   "/"
             "Set-Cookie" (str "sessionId=" session-id)}
   :body    ""})

(defn reset-handler [request]
  (let [session-id (session/find-or-create-session-id request)]
    (reset-requested-game {:session-id session-id})))

(defn fallback-handler [_]
  {:status 404
   :body   (str (h/html [:h1 "Route Not Found"]))})