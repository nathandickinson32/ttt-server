(ns ttt-server.handlers.fallback-handler-spec
  (:require [speclj.core :refer :all]
            [ttt-server.handlers.fallback-handler :as fallback]))

(describe "fallback handler"

  (it "fallback handler"
    (let [response (fallback/fallback-handler nil)]
      (should= 404 (:status response))
      (should= "<h1>Route Not Found</h1>" (:body response))))
  )
