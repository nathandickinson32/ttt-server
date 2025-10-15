(ns ttt-server.main
  (:gen-class)
  (:require [ttt-server.http :as http]
            [ttt-server.handlers.reset-handler :as reset]
            [ttt-server.handlers.fallback-handler :as fallback]
            [ttt-server.handlers.game-handler :as game]
            [ttt-server.handlers.choose-player-type :as choose-player]
            [ttt-server.handlers.choose-first-player :as first-player]
            [ttt-server.handlers.choose-board :as choose-board])
  (:import [server HttpServer]))

(defn register-routes [^HttpServer server database]
  (.addHandler server "GET" "/" (http/->request-handler choose-board/welcome-request-handler database))
  (.addHandler server "GET" "/select-player-x" (http/->request-handler choose-player/select-player-x-handler database))
  (.addHandler server "GET" "/select-player-o" (http/->request-handler choose-player/select-player-o-handler database))
  (.addHandler server "GET" "/choose-first-player" (http/->request-handler first-player/choose-first-player-handler database))
  (.addHandler server "GET" "/start" (http/->request-handler game/game-request-handler database))
  (.addHandler server "POST" "/move" (http/->request-handler game/move-handler database))
  (.addHandler server "POST" "/reset" (http/->request-handler reset/reset-handler database)))

(defn -main [& args]
  (let [database (if (some #(= "--edn" %) args) :edn-file :postgres)
        fallback (http/->request-handler fallback/fallback-handler database)
        server   (HttpServer. 8081 10 fallback)]
    (register-routes server database)
    (println "TTT server running on http://localhost:8081 with database:" database)
    (.start server)))