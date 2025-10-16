(ns ttt-server.handlers.choose-board-spec
  (:require [speclj.core :refer :all]
            [ttt-server.handlers.choose-board :as choose-board]
            [ttt-server.handlers.game-handler :as game-handler]))

(describe "Choose board/Welcome handler"

  (it "renders the welcome page with board size options"
    (let [response (choose-board/welcome-request-handler {})
          body     (:body response)]
      (should= 200 (:status response))
      (should-contain "Welcome to Tic Tac Toe!" body)
      (should-contain "value=\"3x3\"" body)
      (should-contain "value=\"4x4\"" body)
      (should-contain "Set-Cookie" (:headers response))))

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