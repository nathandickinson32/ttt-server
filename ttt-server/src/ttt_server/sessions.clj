(ns ttt-server.sessions
  (:require [clojure.string :as str]
            [ttt-server.game :as game])
  (:import (java.util UUID)
           (server Request)))

(defonce sessions (atom {}))

(defn new-session-id []
  (str (UUID/randomUUID)))

(defn get-or-create-session [^Request req]
  (let [cookie-header (.getHeader req "Cookie")
        session-id    (if cookie-header
                        (some #(when (.startsWith % "sessionId=")
                                 (second (str/split % #"=")))
                              (str/split cookie-header #";")))
        session-id    (or session-id (new-session-id))
        game          (get @sessions session-id)]
    [session-id
     (if game
       game
       {:game-id       (UUID/randomUUID)
        :board         game/starting-board
        :board-size    game/default-board-size
        :current-token :X
        :X             :human
        :O             :human
        :turn-count    0})]))

(defn with-session [req handler-fn]
  (let [[session-id game] (get-or-create-session req)]
    (handler-fn session-id game)))