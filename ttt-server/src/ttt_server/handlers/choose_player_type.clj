(ns ttt-server.handlers.choose-player-type
  (:require [hiccup2.core :as h]
            [ttt-server.handlers.core :as core]))

(defn select-player-x-handler [request]
  {:status 200
   :body
   (str
     (h/html
       [:html
        (core/head "Select Player X")
        [:body
         [:h1 "Select Player X Type"]
         [:form {:method "GET" :action "/select-player-o"}
          [:input {:type "hidden" :name "board-size" :value (-> request :params :board-size)}]
          (for [player-type ["human" "easy-ai" "medium-ai" "expert-ai"]]
            [:button.selection-button
             {:name "player-x-type" :type "submit" :value player-type}
             player-type])]]]))})

(defn select-player-o-handler [request]
  {:status 200
   :body
   (str
     (h/html
       [:html
        (core/head "Select Player O")
        [:body
         [:h1 "Select Player O Type"]
         [:form {:method "GET" :action "/choose-first-player"}
          [:input {:type "hidden" :name "board-size" :value (-> request :params :board-size)}]
          [:input {:type "hidden" :name "player-x-type" :value (-> request :params :player-x-type)}]
          (for [player-type ["human" "easy-ai" "medium-ai" "expert-ai"]]
            [:button.selection-button
             {:name "player-o-type" :type "submit" :value player-type}
             player-type])]]]))})
