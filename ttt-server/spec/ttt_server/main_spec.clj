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
      (should-contain "/" (keys @handlers))
      (should-contain "/move" (keys @handlers))
      (should-contain "/reset" (keys @handlers))))
  )