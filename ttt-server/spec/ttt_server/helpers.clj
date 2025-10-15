(ns ttt-server.helpers
  (:require [clojure.test :refer :all]
            [ttt-server.game :as game]
            [ttt-server.sessions :as session])
  (:import (server Request)))

(def nil-board [[nil nil nil] [nil nil nil] [nil nil nil]])

(defn new-session []
  (let [[session-id] (session/get-or-create-session (Request. "GET" "/" "HTTP/1.1"))]
    session-id))

(defn reset-session [session-id]
  (swap! session/sessions assoc session-id
         {:game-id       (random-uuid)
          :board         game/starting-board
          :board-size    game/default-board-size
          :current-token :X
          :X             :human
          :O             :human
          :turn-count    0
          :winner        nil
          :draw          false}))

(defn get-game [session-id]
  (get @session/sessions session-id))

(defn apply-moves [session-id moves]
  (doseq [[row col] moves]
    (swap! session/sessions update session-id game/update-or-ensure-game [row col])))