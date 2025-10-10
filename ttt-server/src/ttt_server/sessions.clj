(ns ttt-server.sessions
  (:require [clojure.string :as str]
            [ttt-server.game :as game]))

(defonce sessions (atom {}))

(defn new-game-state []
  {:game-id       (random-uuid)
   :board         game/starting-board
   :board-size    game/default-board-size
   :current-token :X
   :X             :human
   :O             :human
   :turn-count    0})

(defn new-session-id []
  (str (random-uuid)))

(defn parse-session-id [request]
  (when-let [cookie-header (get-in request [:headers "Cookie"])]
    (->> (str/split cookie-header #";")
         (some #(when (str/starts-with? % "sessionId=")
                  (second (str/split % #"=")))))))

(defn find-or-create-session-id [request]
  (or (parse-session-id request)
      (new-session-id)))

(defn find-game [session-id]
  (get @sessions session-id (new-game-state)))

(defn get-or-create-session [request]
  (let [session-id (find-or-create-session-id request)
        game       (find-game session-id)]
    [session-id game]))

(defn resume-or-new-session [request handler-fn]
  (let [[session-id game] (get-or-create-session request)]
    (handler-fn session-id game)))

(defn set-session [session-id game]
  (swap! sessions assoc session-id game))

(defn reset-session [session-id]
  (set-session session-id (new-game-state)))