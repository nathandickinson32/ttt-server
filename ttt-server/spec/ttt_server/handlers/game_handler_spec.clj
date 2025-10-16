(ns ttt-server.handlers.game-handler-spec
  (:require [speclj.core :refer :all]
            [ttt-server.game :as game]
            [ttt-server.handlers.game-handler :as sut]
            [ttt-server.sessions :as session]))

(describe "Game Page"

  (it "game-handler"
    (let [game     session/new-game-state
          request  {:session-id "the-session-id"}
          response (sut/game-handler request game)
          body     (:body response)
          buttons  (re-seq #"<button[^>]*class=\"square\"" body)]
      (should= 200 (:status response))
      (should-contain "<h1>Tic Tac Toe</h1>" body)
      (should-contain "id=\"board\"" body)
      (should= 9 (count buttons))))

  (it "move-handler"
    (let [request  {:params {:row "0" :col "0"}}
          response (sut/move-handler request)
          body     (:body response)]
      (should-contain "<button class=\"square\" type=\"submit\">X</button>" body)
      (should-contain "Turn: :O" body)))

  (it "shows winner in status message"
    (let [game {:board  [[:X :X :X]
                         [:O :O nil]
                         [nil nil nil]]
                :winner :X}
          body (:body (sut/game-handler "session-id" game))]
      (should-contain "Winner: :X" body)
      (should-contain "return false;" body)))

  (it "shows draw in status message"
    (let [game {:board [[:X :O :X]
                        [:O :X :O]
                        [:O :X :O]]
                :draw  true}
          body (:body (sut/game-handler "session-id" game))]
      (should-contain "Draw!" body)
      (should-contain "return false;" body)))

  (it "makes an easy-ai move automatically"
    (let [game     {:board         (game/starting-board :3x3)
                    :X             :human-ai
                    :O             :easy-ai
                    :current-token :O}
          response (sut/game-handler "session-id" game)
          body     (:body response)]
      (should-contain "O" body)))
  )