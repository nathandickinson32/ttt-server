(ns ttt-server.handlers.choose-player-type
  (:require [hiccup2.core :as h]
            [ttt-server.handlers.core :as core]))

(defn select-player-x-handler [request]
  (let [params     (:params request)
        board-size (get params :board-size)]
    {:status 200
     :body
     (str
       (h/html
         [:html
          [:head [:meta {:charset "UTF-8"}] [:title "Select Player X"]
           [:style @core/game-styles]]
          [:body
           [:h1 "Select Player X Type"]
           [:form {:method "GET" :action "/select-player-o"}
            [:input {:type "hidden" :name "board-size" :value board-size}]
            [:button.selection-button {:name "player-x-type" :type "submit" :value "human"} "human"]
            [:button.selection-button {:name "player-x-type" :type "submit" :value "easy-ai"} "easy-ai"]
            [:button.selection-button {:name "player-x-type" :type "submit" :value "medium-ai"} "medium-ai"]
            [:button.selection-button {:name "player-x-type" :type "submit" :value "expert-ai"} "expert-ai"]]]]))}))

(defn select-player-o-handler [request]
  (let [params        (:params request)
        board-size    (get params :board-size)
        player-x-type (get params :player-x-type)]
    {:status 200
     :body
     (str
       (h/html
         [:html
          [:head [:meta {:charset "UTF-8"}] [:title "Select Player O"]
           [:style @core/game-styles]]
          [:body
           [:h1 "Select Player O Type"]
           [:form {:method "GET" :action "/choose-first-player"}
            [:input {:type "hidden" :name "board-size" :value board-size}]
            [:input {:type "hidden" :name "player-x-type" :value player-x-type}]
            [:button.selection-button {:name "player-o-type" :type "submit" :value "human"} "human"]
            [:button.selection-button {:name "player-o-type" :type "submit" :value "easy-ai"} "easy-ai"]
            [:button.selection-button {:name "player-o-type" :type "submit" :value "medium-ai"} "medium-ai"]
            [:button.selection-button {:name "player-o-type" :type "submit" :value "expert-ai"} "expert-ai"]]]]))}))