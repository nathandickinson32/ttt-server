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

(def handlers
  {["GET" "/"]                    choose-board/welcome-request-handler
   ["GET" "/start"]               game/game-request-handler
   ["GET" "/select-player-x"]     choose-player/select-player-x-handler
   ["GET" "/select-player-o"]     choose-player/select-player-o-handler
   ["GET" "/choose-first-player"] first-player/choose-first-player-handler
   ["POST" "/move"]               game/move-handler
   ["POST" "/reset"]              reset/reset-handler})

(defn register-routes [^HttpServer server database]
  (doseq [[[method route] handler] handlers]
    (.addHandler server method route (http/->request-handler handler database))))

(defn -main [& args]
  (let [database (if (some #(= "--edn" %) args) :edn-file :postgres)
        fallback (http/->request-handler fallback/fallback-handler database)
        server   (HttpServer. 8081 10 fallback)]
    (register-routes server database)
    (println "TTT server running on http://localhost:8081 with database:" database)
    (.start server)))