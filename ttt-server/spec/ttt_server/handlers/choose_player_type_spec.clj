(ns ttt-server.handlers.choose-player-type-spec
  (:require [speclj.core :refer :all]
            [ttt-server.handlers.choose-player-type :as choose-player]))

(describe "Player Selection Pages"

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
  )
