(ns ttt-server.handlers.reset-handler
  (:require [ttt-server.sessions :as session]))

(defn reset-requested-game [{:keys [session-id]} database]
  (session/reset-session session-id database)
  {:status  302
   :headers {"Location" "/" "Set-Cookie" (str "sessionId=" session-id)}
   :body    ""})

(defn reset-handler [request]
  (let [session-id (session/find-or-create-session-id request)]
    (reset-requested-game {:session-id session-id} (:database request))))