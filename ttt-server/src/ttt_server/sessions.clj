(ns ttt-server.sessions
  (:require [ttt-server.game :as game]))

(defonce sessions (atom {}))

(defn new-game-state [database]
  {:game-id       (random-uuid)
   :board         game/starting-board
   :board-size    game/default-board-size
   :current-token :X
   :X             :human
   :O             :human
   :turn-count    0
   :database      database})

(defn new-session-id []
  (str (random-uuid)))

(defn find-or-create-session-id [request]
  (or (:session-id request)
      (new-session-id)))

(defn find-game [session-id database]
  (get @sessions session-id (new-game-state database)))

(defn get-or-create-session [request]
  (let [session-id (find-or-create-session-id request)
        game       (find-game session-id (:database request))]
    [session-id game]))

(defn resume-or-new-session [request handler-fn]
  (let [[session-id game] (get-or-create-session request)]
    (handler-fn session-id game)))

(defn set-session [session-id game]
  (swap! sessions assoc session-id game))

(defn reset-session [session-id database]
  (set-session session-id (new-game-state database)))