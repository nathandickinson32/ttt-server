(ns ttt-server.http-spec
  (:require [speclj.core :refer :all]
            [ttt-server.http :as http])
  (:import (server Request Response)))

(describe "ttt-server.http"

  (context "set-headers"

    (it "adds headers to a Response"
      (let [resp (Response.)]
        (http/set-headers resp {"Content-Type" "text/html"
                                "Location"     "/home"})
        (should= "text/html" (.get (.getHeaders resp) "Content-Type"))
        (should= "/home" (.get (.getHeaders resp) "Location"))))

    (it "does nothing when headers map is empty"
      (let [resp (Response.)]
        (http/set-headers resp {})
        (should= 1 (.size (.getHeaders resp)))))
    )

  (context "->Response"

    (it "converts a response map to a Response object"
      (let [resp-map {:status  302
                      :body    "Test Body"
                      :headers {"Test Header" "/test-path"}}
            resp     (http/->Response resp-map)]
        (should= 302 (.getStatusCode resp))
        (should= "Test Body" (.getBody resp))
        (should= "/test-path" (.get (.getHeaders resp) "Test Header"))))

    (it "handles missing headers"
      (let [resp-map {:status 200 :body "OK"}
            resp     (http/->Response resp-map)]
        (should= 200 (.getStatusCode resp))
        (should= "OK" (.getBody resp))
        (should= 1 (.size (.getHeaders resp)))))
    )

  (context "->request-handler"

    (it "wraps a handler function into a RequestHandler returning a Response"
      (let [handler-fn      (fn [_]
                              {:status  200
                               :body    "Hello"
                               :headers {"Content-Type" "text/plain"}})
            request-handler (http/->request-handler handler-fn :postgres)
            req             (Request. "GET" "/" "HTTP/1.1")
            resp            (.handle request-handler req)]
        (should= 200 (.getStatusCode resp))
        (should= "Hello" (.getBody resp))
        (should= "text/plain" (.get (.getHeaders resp) "Content-Type"))))
    )
  )