(ns ttt-server.main
  (:gen-class)
  (:require [ttt-server.handlers :as handlers]
            [ttt-server.http :as http])
  (:import [server HttpServer]))

(defn register-routes [^HttpServer server database]
  (.addHandler server "GET" "/" (http/->request-handler handlers/game-request-handler database))
  (.addHandler server "POST" "/move" (http/->request-handler handlers/move-handler database))
  (.addHandler server "POST" "/reset" (http/->request-handler handlers/reset-handler database)))

(defn -main [& args]
  (let [database (if (some #(= "--edn" %) args) :edn-file :postgres)
        fallback (http/->request-handler handlers/fallback-handler database)
        server   (HttpServer. 8081 10 fallback)]
    (register-routes server database)
    (println "TTT server running on http://localhost:8081 with database:" database)
    (.start server)))