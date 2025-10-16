(ns ttt-server.main-spec
  (:require [speclj.core :refer :all]
            [ttt-server.main :as sut])
  (:import (server HttpServer)))

(describe "TTT Core"

  (it "registers routes"
    (let [handlers    (atom {})
          mock-server (proxy [HttpServer] [0 0 nil]
                        (addHandler [_ path handler]
                          (swap! handlers assoc path handler)))]
      (sut/register-routes mock-server :postgres)
      (should-contain "/" @handlers)
      (should-contain "/start" @handlers)
      (should-contain "/move" @handlers)
      (should-contain "/reset" @handlers)
      (should-contain "/select-player-x" @handlers)
      (should-contain "/select-player-o" @handlers)
      (should-contain "/choose-first-player" @handlers)))
  )