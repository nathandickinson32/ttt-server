(ns ttt-server.handlers.choose-first-player
  (:require [hiccup2.core :as h]
            [ttt-server.handlers.core :as core]))

(defn choose-first-player-handler [request]
  (let [params        (:params request)
        board-size    (get params :board-size)
        player-x-type (get params :player-x-type)
        player-o-type (get params :player-o-type)]
    {:status 200
     :body
     (str
       (h/html
         [:html
          [:head [:meta {:charset "UTF-8"}] [:title "Choose First Player"]
           [:style @core/game-styles]]
          [:body
           [:h1 "Who Goes First?"]
           [:form {:method "GET" :action "/start"}
            [:input {:type "hidden" :name "board-size" :value board-size}]
            [:input {:type "hidden" :name "player-x-type" :value player-x-type}]
            [:input {:type "hidden" :name "player-o-type" :value player-o-type}]
            [:button.selection-button {:name "first-player" :type "submit" :value "X"} "X"]
            [:button.selection-button {:name "first-player" :type "submit" :value "O"} "O"]]]]))}))