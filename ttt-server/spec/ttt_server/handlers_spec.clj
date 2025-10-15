(ns ttt-server.handlers-spec
  (:require [speclj.core :refer :all]
            [ttt-server.game :as game]
            [ttt-server.handlers.choose-board :as choose-board]
            [ttt-server.handlers.choose-first-player :as first-player]
            [ttt-server.handlers.reset-handler :as reset]
            [ttt-server.handlers.fallback-handler :as fallback]
            [ttt-server.handlers.game-handler :as game-handler]
            [ttt-server.handlers.choose-player-type :as choose-player]
            [ttt-server.sessions :as session]))

(describe "Tic Tac Toe Handlers"

  (it "fallback handler"
    (let [response (fallback/fallback-handler nil)]
      (should= 404 (:status response))
      (should= "<h1>Route Not Found</h1>" (:body response))))

  (it "resets the session"
    (let [request  {:session-id "the-session-id"}
          response (reset/reset-requested-game request :postgres)
          game     (session/find-game "the-session-id" :postgres)]
      (should= 302 (:status response))
      (should= "" (:body response))
      (should= "/" (get-in response [:headers "Location"]))
      (should= "sessionId=the-session-id" (get-in response [:headers "Set-Cookie"]))
      (should= game/starting-board (:board game))
      (should-be uuid? (:game-id game))))

  (context "Game Page"

    (it "game-handler"
      (let [game     session/new-game-state
            request  {:session-id "the-session-id"}
            response (game-handler/game-handler request game)
            body     (:body response)
            buttons  (re-seq #"<button[^>]*class=\"square\"" body)]
        (should= 200 (:status response))
        (should-contain "<h1>Tic Tac Toe</h1>" body)
        (should-contain "id=\"board\"" body)
        (should= 9 (count buttons))))

    (it "move-handler"
      (let [request  {:params {:row "0" :col "0"}}
            response (game-handler/move-handler request)
            body     (:body response)]
        (should-contain "<button class=\"square\" type=\"submit\">X</button>" body)
        (should-contain "Turn: :O" body)))

    (it "shows winner in status message"
      (let [game {:board  [[:X :X :X]
                           [:O :O nil]
                           [nil nil nil]]
                  :winner :X}
            body (:body (game-handler/game-handler "session-id" game))]
        (should-contain "Winner: :X" body)
        (should-contain "return false;" body)))

    (it "shows draw in status message"
      (let [game {:board [[:X :O :X]
                          [:O :X :O]
                          [:O :X :O]]
                  :draw  true}
            body (:body (game-handler/game-handler "session-id" game))]
        (should-contain "Draw!" body)
        (should-contain "return false;" body)))

    (it "makes an easy-ai move automatically"
      (let [game     {:board         game/starting-board
                      :X             :human-ai
                      :O             :easy-ai
                      :current-token :O}
            response (game-handler/game-handler "session-id" game)
            body     (:body response)]
        (should-contain "O" body)))
    )

  (context "Welcome Page"

    (it "renders the welcome page with board size options"
      (let [response (choose-board/welcome-request-handler {})
            body     (:body response)]
        (should= 200 (:status response))
        (should-contain "Welcome to Tic Tac Toe!" body)
        (should-contain "value=\"3x3\"" body)
        (should-contain "value=\"4x4\"" body)
        (should-contain "Set-Cookie" (str (keys (:headers response))))))

    (it "creates a 3x3 board when board-size=3x3 is selected"
      (let [request  {:params {:board-size    "3x3"
                               :player-x-type "human"
                               :player-o-type "human"
                               :first-player  "X"}}
            response (game-handler/game-request-handler request)
            body     (:body response)
            buttons  (re-seq #"<button[^>]*class=\"square\"" body)]
        (should= 200 (:status response))
        (should-contain "<h1>Tic Tac Toe</h1>" body)
        (should-contain "--board-size:3" body)
        (should-contain "id=\"board\"" body)
        (should= 9 (count buttons))))

    (it "creates a 4x4 board when board-size=4x4 is selected"
      (let [request  {:params {:board-size    "4x4"
                               :player-x-type "human"
                               :player-o-type "human"
                               :first-player  "X"}}
            response (game-handler/game-request-handler request)
            body     (:body response)
            buttons  (re-seq #"<button[^>]*class=\"square\"" body)]
        (should= 200 (:status response))
        (should-contain "<h1>Tic Tac Toe</h1>" body)
        (should-contain "--board-size:4" body)
        (should-contain "id=\"board\"" body)
        (should= 16 (count buttons))))
    )

  (context "Player Selection Pages"

    (it "renders the Player X selection page"
      (let [request  {:params {:board-size "3x3"}}
            response (choose-player/select-player-x-handler request)
            body     (:body response)]
        (should= 200 (:status response))
        (should-contain "<h1>Select Player X Type</h1>" body)
        (should-contain "value=\"human\"" body)
        (should-contain "value=\"easy-ai\"" body)
        (should-contain "value=\"medium-ai\"" body)
        (should-contain "value=\"expert-ai\"" body)
        (should-contain "3x3" body)))

    (it "renders the Player O selection page with Player X chosen"
      (let [request  {:params {:board-size    "4x4"
                               :player-x-type "easy-ai"}}
            response (choose-player/select-player-o-handler request)
            body     (:body response)]
        (should= 200 (:status response))
        (should-contain "<h1>Select Player O Type</h1>" body)
        (should-contain "value=\"human\"" body)
        (should-contain "value=\"easy-ai\"" body)
        (should-contain "value=\"medium-ai\"" body)
        (should-contain "value=\"expert-ai\"" body)
        (should-contain "4x4" body)
        (should-contain "easy-ai" body)))

    (it "renders the Choose First Player page with X and O types set"
      (let [request  {:params {:board-size    "3x3"
                               :player-x-type "human"
                               :player-o-type "easy-ai"}}
            response (first-player/choose-first-player-handler request)
            body     (:body response)]
        (should= 200 (:status response))
        (should-contain "<h1>Who Goes First?" body)
        (should-contain "value=\"X\"" body)
        (should-contain "value=\"O\"" body)
        (should-contain "human" body)
        (should-contain "easy-ai" body)))
    )
  )