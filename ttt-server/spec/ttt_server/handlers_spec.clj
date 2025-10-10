(ns ttt-server.handlers-spec
  (:require [speclj.core :refer :all]
            [ttt-server.game :as game]
            [ttt-server.handlers :as handlers]
            [ttt-server.sessions :as session]
            [ttt-server.http :as http]
            [ttt-server.helpers :as helper])
  (:import (server Request)))

(describe "Tic Tac Toe Handlers"

  (it "fallback handler"
    (let [response (handlers/fallback-handler nil)]
      (should= 404 (:status response))
      (should= "<h1>Route Not Found</h1>" (:body response))))

  (it "resets the session"
    (reset! session/sessions {})
    (let [request  {:session-id "the-session-id"}
          response (handlers/reset-requested-game request)
          game     (session/find-game "the-session-id")]
      (should= 302 (:status response))
      (should= "" (:body response))
      (should= "/" (get-in response [:headers "Location"]))
      (should= "sessionId=the-session-id" (get-in response [:headers "Set-Cookie"]))
      (should= game/starting-board (:board game))
      (should-be uuid? (:game-id game))))

  (context "Routing"

    (it "renders the board correctly on GET /"
      (let [session-id (helper/new-session)]
        (helper/reset-session session-id)
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
        (helper/reset-session session-id)
        (helper/handler-apply-moves session-id [[0 0]])
        (let [body (.getBody (helper/get-index session-id))]
          (should-contain "<button class=\"square\" type=\"submit\">X</button>" body)
          (should-contain "Turn: :O" body))))

    (it "prevents moves on occupied squares"
      (let [session-id (helper/new-session)]
        (helper/reset-session session-id)
        (let [handler (http/->request-handler handlers/move-handler)]
          (.handle handler (helper/post-move session-id 0 0))
          (.handle handler (helper/post-move session-id 0 0))
          (let [body (.getBody (helper/get-index session-id))]
            (should-contain "<button class=\"square\" type=\"submit\">X</button>" body)))))

    (it "declares winners and stops extra moves"
      (let [session-id (helper/new-session)]
        (helper/reset-session session-id)
        (helper/handler-apply-moves session-id [[0 0] [1 0] [0 1] [1 1] [0 2]])
        (let [response (helper/get-index session-id)
              body     (.getBody response)
              handler  (http/->request-handler handlers/move-handler)]
          (.handle handler (helper/post-move session-id 2 2))
          (should-contain "Winner: :X" body)
          (should= [[:X :X :X] [:O :O nil] [nil nil nil]]
                   (get-in @session/sessions [session-id :board])))))

    (it "resets game and returns 302 redirect"
      (let [session-id (helper/new-session)]
        (helper/reset-session session-id)
        (helper/handler-apply-moves session-id [[0 0]])
        (let [reset-request (doto (Request. "POST" "/reset" "HTTP/1.1")
                              (.addHeader "Cookie" (str "sessionId=" session-id)))
              handler       (http/->request-handler handlers/reset-handler)
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