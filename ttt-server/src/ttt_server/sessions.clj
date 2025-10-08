(ns ttt-server.sessions
  (:require [clojure.string :as str]
            [ttt-server.game :as game])
  (:import (java.util UUID)
           (server Request)))

(defonce sessions (atom {}))

(defn new-game-state []
  {:game-id       (UUID/randomUUID)
   :board         game/starting-board
   :board-size    game/default-board-size
   :current-token :X
   :X             :human
   :O             :human
   :turn-count    0})

(defn new-session-id []
  (str (UUID/randomUUID)))

(defn parse-session-id [^Request req]
  (when-let [cookie-header (.getHeader req "Cookie")]
    (->> (str/split cookie-header #";")
         (some #(when (.startsWith % "sessionId=")
                  (second (str/split % #"=")))))))

(defn find-or-create-session-id [^Request req]
  (or (parse-session-id req)
      (new-session-id)))

(defn get-or-create-session [^Request req]
  (let [session-id (find-or-create-session-id req)
        game       (get @sessions session-id (new-game-state))]
    [session-id game]))

(defn resume-or-new-session [^Request req handler-fn]
  (let [[session-id game] (get-or-create-session req)]
    (handler-fn session-id game)))

(defn reset-session [session-id]
  (swap! sessions assoc session-id (new-game-state)))