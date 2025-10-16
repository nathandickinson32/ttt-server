(ns ttt-server.handlers.choose-first-player
  (:require [hiccup2.core :as h]
            [ttt-server.handlers.core :as core]))

(defn body [{:keys [board-size player-x-type player-o-type]}]
  [:html
   (core/head "Choose First Player")
   [:body
    [:h1 "Who Goes First?"]
    [:form {:method "GET" :action "/start"}
     [:input {:type "hidden" :name "board-size" :value board-size}]
     [:input {:type "hidden" :name "player-x-type" :value player-x-type}]
     [:input {:type "hidden" :name "player-o-type" :value player-o-type}]
     (for [token ["X" "O"]]
       [:button.selection-button
        {:name "first-player" :type "submit" :value token}
        token])]]])

(defn choose-first-player-handler [request]
  {:status 200
   :body   (-> request :params body h/html str)})
