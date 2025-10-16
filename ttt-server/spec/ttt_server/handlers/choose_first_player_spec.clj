(ns ttt-server.handlers.choose-first-player-spec
  (:require [speclj.core :refer :all]
            [ttt-server.handlers.choose-first-player :as first-player]))

(describe "Choose first player handler"

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