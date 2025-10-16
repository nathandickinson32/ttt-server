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

  (context "Request->map"

    (it "converts a Request object into a Clojure map"
      (let [request (doto (Request. "POST" "/submit" "HTTP/1.1")
                      (.setSessionId "abc123")
                      (.setRawBody (.getBytes "data=example"))
                      (.setParams {"name" "Nathan" "age" "30"})
                      (.setHeaders {"Content-Type" "application/json"})
                      (.setCookies {"token" "cookie123"}))
            result  (http/Request->map request)]
        (should= "POST" (:method result))
        (should= "/submit" (:path result))
        (should= "HTTP/1.1" (:protocol result))
        (should= "abc123" (:session-id result))
        (should= "data=example" (String. ^bytes (:raw-body result)))
        (should= {:name "Nathan" :age "30"} (:params result))
        (should= {"Content-Type" "application/json"} (:headers result))
        (should= {"token" "cookie123"} (:cookies result))))
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

    (it "converts a Request into a Response with handler-fn and adds database to request-map"
      (let [handler-fn      (fn [request-map]
                              (should= :postgres (:database request-map))
                              {:status  201
                               :body    (str "Handled " (:path request-map))
                               :headers {"X-Test" "Passed"}})
            request-handler (http/->request-handler handler-fn :postgres)
            request         (doto (Request. "GET" "/path" "HTTP/1.1")
                              (.setSessionId "s1")
                              (.setParams {"id" "42"}))
            response        (.handle request-handler request)]
        (should= 201 (.getStatusCode response))
        (should= "Handled /path" (.getBody response))
        (should= "Passed" (.get (.getHeaders response) "X-Test"))))
    )
  )