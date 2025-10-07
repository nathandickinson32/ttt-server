(ns ttt-server.helpers
  (:require [clojure.test :refer :all]
            [ttt-server.game :as game]
            [ttt-server.handlers :as handlers]
            [ttt-server.http :as http]
            [ttt-server.sessions :as session])
  (:import (java.util UUID)
           (server Request)))

(def nil-board [[nil nil nil] [nil nil nil] [nil nil nil]])

(defn new-session []
  (let [[session-id] (session/get-or-create-session (Request. "GET" "/" "HTTP/1.1"))]
    session-id))

(defn reset-session [session-id]
  (swap! session/sessions assoc session-id
         {:game-id       (UUID/randomUUID)
          :board         game/starting-board
          :board-size    game/default-board-size
          :current-token :X
          :X             :human
          :O             :human
          :turn-count    0
          :winner        nil
          :draw          false}))

(defn get-index [session-id]
  (let [req (doto (Request. "GET" "/" "HTTP/1.1")
              (.addHeader "Cookie" (str "sessionId=" session-id)))]
    (.handle (http/->request-handler handlers/index-request-handler) req)))

(defn post-move [session-id row col]
  (doto (Request. "POST" "/move" "HTTP/1.1")
    (.setParams {"row" (str row) "col" (str col)})
    (.addHeader "Cookie" (str "sessionId=" session-id))))

(defn handler-apply-moves [session-id moves]
  (let [handler (http/->request-handler handlers/move-handler)]
    (doseq [[row col] moves]
      (.handle handler (post-move session-id row col)))))

(defn get-game [session-id]
  (get @session/sessions session-id))

(defn apply-moves [session-id moves]
  (doseq [[row col] moves]
    (swap! session/sessions update session-id game/update-game-state [row col])))