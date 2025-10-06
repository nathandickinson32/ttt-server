(ns ttt-server.core-spec
  (:require [speclj.core :refer :all]
            [ttt-server.core :as sut])
  (:import (server HttpServer Request)))

(def nil-board
  [[nil nil nil] [nil nil nil] [nil nil nil]])

(defn reset-and-move [& moves]
  (sut/reset-game!)
  (doseq [move moves]
    (sut/make-move! move)))

(defn post-move [row col]
  (doto (Request. "POST" "/move" "HTTP/1.1")
    (.setParams {"row" (str row) "col" (str col)})))

(defn get-index []
  (.handle (sut/->request-handler sut/index-handler)
           (Request. "GET" "/" "HTTP/1.1")))

(describe "Request Handling"

  (context "Starting State"

    (it "starts with empty board and default state"
      (sut/reset-game!)
      (let [{:keys [board current-token X O board-size turn-count]} @sut/state]
        (should= nil-board board)
        (should= :X current-token)
        (should= :human X)
        (should= :human O)
        (should= :3x3 board-size)
        (should= 0 turn-count)))
    )

  (context "Making Moves"

    (it "places X, switches to O, prevents duplicate moves"
      (reset-and-move [0 0] [0 0])
      (should= :O (:current-token @sut/state))
      (should= :X (get-in @sut/state [:board 0 0]))
      (should= 1 (:turn-count @sut/state)))

    (it "places moves correctly for X and O"
      (reset-and-move [0 0] [0 1])
      (should= :X (:current-token @sut/state))
      (should= :X (get-in @sut/state [:board 0 0]))
      (should= :O (get-in @sut/state [:board 0 1]))
      (should= 2 (:turn-count @sut/state)))

    (it "detects X win"
      (reset-and-move [0 0] [1 0] [0 1] [1 1] [0 2])
      (should= :X (:winner @sut/state))
      (should= 5 (:turn-count @sut/state)))

    (it "detects O win"
      (reset-and-move [0 0] [1 0] [0 1] [1 1] [2 2] [1 2])
      (should= :O (:winner @sut/state)))

    (it "detects draw on full board"
      (reset-and-move [1 1] [0 0] [1 0] [1 2] [0 2] [2 0] [0 1] [2 1] [2 2])
      (should (:draw @sut/state)))

    (it "resets board properly"
      (sut/make-move! [0 0])
      (sut/reset-game!)
      (let [{:keys [board current-token winner draw]} @sut/state]
        (should= nil-board board)
        (should= :X current-token)
        (should-be-nil winner)
        (should-not draw)))
    )

  (context "Route Registration"

    (it "registers GET /, POST /move, POST /reset"
      (let [handlers    (atom {})
            mock-server (proxy [HttpServer] [0 10 nil]
                          (addHandler [method path handler]
                            (swap! handlers assoc [method path] handler)))]
        (sut/register-routes mock-server)
        (should-contain ["GET" "/"] (keys @handlers))
        (should-contain ["POST" "/move"] (keys @handlers))
        (should-contain ["POST" "/reset"] (keys @handlers))))
    )

  (context "Routing"

    (it "displays fallbackHandler for unknown routes"
      (sut/reset-game!)
      (let [request  (Request. "GET" "/test" "HTTP/1.1")
            handler  (sut/->request-handler sut/fallback-handler)
            response (.handle handler request)]
        (should= 404 (.getStatusCode response))
        (should-contain "Route Not Found" (.getBody response))))

    (it "renders the board correctly on GET /"
      (reset-and-move [0 0])
      (let [response (get-index)
            body     (.getBody response)
            buttons  (re-seq #"<button[^>]*class=\"square\"" body)]
        (should= 200 (.getStatusCode response))
        (should-contain "<h1>Tic Tac Toe</h1>" body)
        (should-contain "id=\"board\"" body)
        (should= 9 (count buttons))
        (should-contain "onsubmit=\"return false;\"" body)))

    (it "updates board after a move"
      (sut/reset-game!)
      (let [move-request (post-move 0 0)
            handler      (sut/->request-handler sut/move-handler)
            body         (.getBody (.handle handler move-request))]
        (should-contain "<button class=\"square\" type=\"submit\">X</button>" body)
        (should-contain "Turn: :O" body)))

    (it "prevents moves on occupied squares"
      (sut/reset-game!)
      (let [handler (sut/->request-handler sut/move-handler)
            req     (post-move 0 0)]
        (.handle handler req)
        (.handle handler req)
        (let [body (.getBody (get-index))]
          (should-contain "<button class=\"square\" type=\"submit\">X</button>" body))))

    (it "declares winners and stops extra moves"
      (let [handler (sut/->request-handler sut/move-handler)
            moves   [[0 0] [1 0] [0 1] [1 1] [0 2]]]
        (apply reset-and-move moves)
        (let [response   (get-index)
              body       (.getBody response)
              extra-move (post-move 2 2)
              {:keys [board]} @sut/state]
          (should-contain "Winner: :X" body)
          (.handle handler extra-move)
          (should= [[:X :X :X]
                    [:O :O nil]
                    [nil nil nil]] board))))

    (it "resets game and returns 302 redirect"
      (sut/reset-game!)
      (let [move-request  (post-move 0 0)
            reset-request (Request. "POST" "/reset" "HTTP/1.1")
            handler       (sut/->request-handler sut/reset-handler)
            first-id      (:game-id @sut/state)]
        (.handle (sut/->request-handler sut/move-handler) move-request)
        (let [response (.handle handler reset-request)
              {:keys [board current-token winner draw turn-count game-id]} @sut/state]
          (should= nil-board board)
          (should= :X current-token)
          (should-be-nil winner)
          (should-not draw)
          (should= 0 turn-count)
          (should-not= first-id game-id)
          (should= 302 (.getStatusCode response))
          (should= "/" (get (.getHeaders response) "Location")))))
    )
  )