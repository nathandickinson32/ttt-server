(ns ttt-server.core
  (:gen-class)
  (:require [ttt-server.handlers :as h]
            [ttt-server.http :as http])
  (:import [server HttpServer]))

(defn register-routes [^HttpServer server]
  (.addHandler server "GET" "/" (http/->request-handler h/index-request-handler))
  (.addHandler server "POST" "/move" (http/->request-handler h/move-handler))
  (.addHandler server "POST" "/reset" (http/->request-handler h/reset-handler)))

(defn -main [& _]
  (let [fallback (http/->request-handler h/fallback-handler)
        server   (HttpServer. 8081 10 fallback)]
    (register-routes server)
    (println "TTT server running on http://localhost:8081")
    (.start server)))