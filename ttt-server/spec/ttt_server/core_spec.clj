(ns ttt-server.core-spec
  (:require [speclj.core :refer :all]
            [ttt-server.core :as core])
  (:import (server HttpServer)))

(describe "TTT Core"

  (it "registers routes"
    (let [handlers    (atom {})
          mock-server (proxy [HttpServer] [0 0 nil]
                        (addHandler [_ path handler]
                          (swap! handlers assoc path handler)))]
      (core/register-routes mock-server)
      (should-contain "/" (keys @handlers))
      (should-contain "/move" (keys @handlers))
      (should-contain "/reset" (keys @handlers))))
  )