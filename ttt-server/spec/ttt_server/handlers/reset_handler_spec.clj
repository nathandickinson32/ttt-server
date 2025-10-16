(ns ttt-server.handlers.reset-handler-spec
  (:require [speclj.core :refer :all]
            [ttt-server.game :as game]
            [ttt-server.handlers.reset-handler :as reset]
            [ttt-server.sessions :as session]))

(describe "reset handler"

  (it "resets the session"
    (let [request  {:session-id "the-session-id"}
          response (reset/reset-requested-game request :postgres)
          game     (session/find-game "the-session-id" :postgres)]
      (should= 302 (:status response))
      (should= "" (:body response))
      (should= "/" (get-in response [:headers "Location"]))
      (should= "sessionId=the-session-id" (get-in response [:headers "Set-Cookie"]))
      (should= (game/starting-board :3x3) (:board game))
      (should-be uuid? (:game-id game))))
  )