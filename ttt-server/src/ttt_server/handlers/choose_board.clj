(ns ttt-server.handlers.choose-board
  (:require [hiccup2.core :as h]
            [ttt-server.handlers.core :as core]
            [ttt-server.sessions :as session]))

(def body
  (delay
    [:html
     (core/head "Welcome to Tic Tac Toe")
     [:body
      [:h1 "Welcome to Tic Tac Toe!"]
      [:h3 "Choose your board size:"]
      [:form {:method "GET" :action "/select-player-x"}
       [:button.selection-button
        {:name "board-size" :type "submit" :value "3x3"}
        "3x3"]
       [:button.selection-button
        {:name "board-size" :type "submit" :value "4x4"}
        "4x4"]]]]))

(defn welcome-request-handler [request]
  (let [session-id (session/find-or-create-session-id request)]
    {:status  200
     :headers {"Set-Cookie" (str "sessionId=" session-id)}
     :body    (str (h/html @body))}))
