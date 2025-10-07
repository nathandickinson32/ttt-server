(ns ttt-server.handlers
  (:require [hiccup2.core :as h]
            [ttt-server.sessions :as session]
            [ttt-server.game :as game])
  (:import (java.util UUID)
           (server Request)))

(defn index-handler [session-id game]
  (let [board      (or (:board game) game/starting-board)
        {:keys [current-token winner draw]} game
        status-msg (cond
                     winner (str "Winner: " winner)
                     draw "Draw!"
                     :else (str "Turn: " current-token))]
    {:status  200
     :headers {"Set-Cookie" (str "sessionId=" session-id)}
     :body    (str (h/html
                     [:html
                      [:head
                       [:meta {:charset "UTF-8"}]
                       [:title "Tic Tac Toe"]
                       [:style "
body { display: flex; flex-direction: column; align-items: center; font-family: sans-serif; }
#board { display: grid; grid-template-columns: repeat(3, 100px); grid-gap: 5px; margin-top: 20px; }
.square { display: flex; justify-content: center; align-items: center; font-size: 2em; background: #fff; border: 2px solid #333; width: 100px; height: 100px; text-align: center; cursor: pointer; }
button.reset { margin-top: 15px; padding: 8px 16px; font-size: 1em; }"]]
                      [:body
                       [:h1 "Tic Tac Toe"]
                       [:div#board
                        (mapcat
                          (fn [row]
                            (map
                              (fn [col]
                                (let [cell-value (get-in board [row col])
                                      disable?   (or cell-value winner draw)]
                                  [:form {:method   "POST" :action "/move" :style "display:inline;"
                                          :onsubmit (when disable? "return false;")}
                                   [:input {:name "row" :type "hidden" :value row}]
                                   [:input {:name "col" :type "hidden" :value col}]
                                   [:button.square {:type "submit"} (or cell-value "")]]))
                              (range 3)))
                          (range 3))]
                       [:div#status status-msg]
                       [:form {:method "POST" :action "/reset"}
                        [:button.reset {:type "submit"} "Reset"]]]]))}))

(defn index-request-handler [^Request req]
  (session/with-session req index-handler))

(defn move-handler [^Request req]
  (session/with-session
    req
    (fn [session-id game]
      (let [params  (.getParams req)
            row-str (get params "row")
            col-str (get params "col")
            game    (if (and row-str col-str)
                      (game/update-game-state game [(Integer/parseInt row-str)
                                                    (Integer/parseInt col-str)])
                      game)]
        (swap! session/sessions assoc session-id game)
        (index-handler session-id game)))))

(defn reset-handler [^Request req]
  (let [[session-id] (session/get-or-create-session req)
        game {:game-id       (UUID/randomUUID)
              :board         game/starting-board
              :board-size    game/default-board-size
              :current-token :X
              :X             :human
              :O             :human
              :turn-count    0}]
    (swap! session/sessions assoc session-id game)
    {:status  302
     :headers {"Location"   "/"
               "Set-Cookie" (str "sessionId=" session-id)}
     :body    ""}))

(defn fallback-handler [_]
  {:status 404
   :body   (str (h/html [:h1 "Route Not Found"]))})