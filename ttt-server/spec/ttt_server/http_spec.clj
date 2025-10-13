(ns ttt-server.http-spec
  (:require [speclj.core :refer :all]
            [ttt-server.handlers :as handlers]
            [ttt-server.helpers :as helper]
            [ttt-server.http :as http]
            [ttt-server.sessions :as session])
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

  (context "Routing"

    (it "renders the board correctly on GET /"
      (let [session-id (helper/new-session)]
        (helper/handler-apply-moves session-id [[0 0]])
        (let [response (helper/get-index session-id)
              body     (.getBody response)
              buttons  (re-seq #"<button[^>]*class=\"square\"" body)]
          (should= 200 (.getStatusCode response))
          (should-contain "<h1>Tic Tac Toe</h1>" body)
          (should-contain "id=\"board\"" body)
          (should= 9 (count buttons))
          (should-contain "onsubmit=\"return false;\"" body))))

    (it "updates board after a move"
      (let [session-id (helper/new-session)]
        (helper/handler-apply-moves session-id [[0 0]])
        (let [body (.getBody (helper/get-index session-id))]
          (should-contain "<button class=\"square\" type=\"submit\">X</button>" body)
          (should-contain "Turn: :O" body))))

    (it "prevents moves on occupied squares"
      (let [session-id (helper/new-session)]
        (let [handler (http/->request-handler handlers/move-handler :postgres)]
          (.handle handler (helper/post-move session-id 0 0))
          (.handle handler (helper/post-move session-id 0 0))
          (let [body (.getBody (helper/get-index session-id))]
            (should-contain "<button class=\"square\" type=\"submit\">X</button>" body)))))

    (it "declares winners and stops extra moves"
      (let [session-id (helper/new-session)]
        (helper/handler-apply-moves session-id [[0 0] [1 0] [0 1] [1 1] [0 2]])
        (let [response (helper/get-index session-id)
              body     (.getBody response)
              handler  (http/->request-handler handlers/move-handler :postgres)]
          (.handle handler (helper/post-move session-id 2 2))
          (should-contain "Winner: :X" body)
          (should= [[:X :X :X] [:O :O nil] [nil nil nil]]
                   (get-in @session/sessions [session-id :board])))))

    (it "resets game and returns 302 redirect"
      (let [session-id (helper/new-session)]
        (helper/handler-apply-moves session-id [[0 0]])
        (let [reset-request (doto (Request. "POST" "/reset" "HTTP/1.1")
                              (.addHeader "Cookie" (str "sessionId=" session-id)))
              handler       (http/->request-handler handlers/reset-handler :postgres)
              first-id      (get-in @session/sessions [session-id :game-id])
              response      (.handle handler reset-request)
              {:keys [board current-token winner draw turn-count game-id]} (get @session/sessions session-id)]
          (should= helper/nil-board board)
          (should= :X current-token)
          (should-be-nil winner)
          (should-not draw)
          (should= 0 turn-count)
          (should-not= first-id game-id)
          (should= 302 (.getStatusCode response))
          (should= "/" (get (.getHeaders response) "Location")))))
    )
  )