(ns ttt-server.handlers-spec
  (:require [speclj.core :refer :all]
            [ttt-server.game :as game]
            [ttt-server.handlers :as handlers]
            [ttt-server.sessions :as session]))

(describe "Tic Tac Toe Handlers"

  (it "fallback handler"
    (let [response (handlers/fallback-handler nil)]
      (should= 404 (:status response))
      (should= "<h1>Route Not Found</h1>" (:body response))))

  (it "resets the session"
    (let [request  {:session-id "the-session-id"}
          response (handlers/reset-requested-game request :postgres)
          game     (session/find-game "the-session-id" :postgres)]
      (should= 302 (:status response))
      (should= "" (:body response))
      (should= "/" (get-in response [:headers "Location"]))
      (should= "sessionId=the-session-id" (get-in response [:headers "Set-Cookie"]))
      (should= game/starting-board (:board game))
      (should-be uuid? (:game-id game))))

  (it "game-handler"
    (let [game     session/new-game-state
          request  {:session-id "the-session-id"}
          response (handlers/game-handler request game)
          body     (:body response)
          buttons  (re-seq #"<button[^>]*class=\"square\"" body)]
      (should= 200 (:status response))
      (should-contain "<h1>Tic Tac Toe</h1>" body)
      (should-contain "id=\"board\"" body)
      (should= 9 (count buttons))))

  (it "move-handler"
    (let [request  {:params {:row "0" :col "0"}}
          response (handlers/move-handler request)
          body     (:body response)]
      (should-contain "<button class=\"square\" type=\"submit\">X</button>" body)
      (should-contain "Turn: :O" body)))

  (it "shows winner in status message"
    (let [game {:board  [[:X :X :X]
                         [nil nil nil]
                         [nil nil nil]]
                :winner :X}
          body (:body (handlers/game-handler "session-id" game))]
      (should-contain "Winner: :X" body)
      (should-contain "return false;" body)))

  (it "shows draw in status message"
    (let [game {:board [[:X :O :X]
                        [:O :X :O]
                        [:O :X :O]]
                :draw  true}
          body (:body (handlers/game-handler "session-id" game))]
      (should-contain "Draw!" body)
      (should-contain "return false;" body)))
  )