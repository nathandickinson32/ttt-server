(ns ttt-server.core
  (:gen-class)
  (:require [hiccup2.core :as h]
            [tic-tac-toe.board :as board]
            [tic-tac-toe.game :as game])
  (:import (java.util UUID)
           [server HttpServer Request Response]
           [handlers RequestHandler]))

(def starting-board
  (vec (repeat 3 (vec (repeat 3 nil)))))

(defonce state
         (atom {:game-id       (UUID/randomUUID)
                :board         starting-board
                :board-size    :3x3
                :current-token :X
                :X             :human
                :O             :human
                :turn-count    0
                :draw          false}))

(defn set-headers [^Response resp headers]
  (doseq [[k v] headers]
    (.addHeader resp k v)))

(defn ->Response [resp-map]
  (doto (Response.)
    (.setStatusCode (:status resp-map))
    (.setBody (:body resp-map))
    (set-headers (:headers resp-map))))

(defn Request->map [^Request req]
  {:method (.getMethod req)
   :params (.getParams req)})

(defn ->request-handler [handler-fn]
  (proxy [RequestHandler] []
    (handle [^Request req]
      (-> req Request->map handler-fn ->Response))))

(defn invalid-move? [board move]
  (or (:winner state)
      (:draw state)
      (get-in board move)))

(defn flag-end-game [winner draw new-state]
  (assoc new-state
    :winner winner
    :draw draw))

(defn ->next-state [state move]
  (let [new-state (game/->new-state state move)
        board     (:board new-state)
        size      (:board-size state)
        winner    (cond (board/win? board :X size) :X
                        (board/win? board :O size) :O)
        draw      (board/full-board? board size)]
    (flag-end-game winner draw new-state)))

(defn update-game-state [state move]
  (let [board (:board state)]
    (if (invalid-move? board move)
      state
      (->next-state state move))))

(defn make-move! [move]
  (swap! state update-game-state move))

(defn reset-game! []
  (swap! state assoc
         :game-id (UUID/randomUUID)
         :board starting-board
         :current-token :X
         :turn-count 0
         :draw false
         :winner nil))

(defn index-handler [_]
  (let [{:keys [board current-token winner draw]} @state
        status-msg (cond
                     winner (str "Winner: " winner)
                     draw "Draw!"
                     :else (str "Turn: " current-token))]
    {:status  200
     :headers {"Content-Type" "text/html"}
     :body
     (str (h/html
            [:html
             [:head
              [:meta {:charset "UTF-8"}]
              [:title "Tic Tac Toe"]
              [:style "
body { display: flex; flex-direction: column; align-items: center; font-family: sans-serif; }
#board { display: grid; grid-template-columns: repeat(3, 100px); grid-gap: 5px; margin-top: 20px; }
.square { display: flex; justify-content: center; align-items: center; font-size: 2em; background: #fff; border: 2px solid #333; width: 100px; height: 100px; text-align: center; cursor: pointer; }
button.reset { margin-top: 15px; padding: 8px 16px; font-size: 1em; }"]]
             [:body
              [:h1 "Tic Tac Toe"]
              [:div#board
               (mapcat
                 (fn [row]
                   (map
                     (fn [col]
                       (let [cell-value (get-in board [row col])
                             disable?   (or cell-value winner draw)]
                         [:form {:method   "POST" :action "/move" :style "display:inline;"
                                 :onsubmit (when disable? "return false;")}
                          [:input {:name "row" :type "hidden" :value row}]
                          [:input {:name "col" :type "hidden" :value col}]
                          [:button.square {:type "submit"} (or cell-value "")]]))
                     (range 3)))
                 (range 3))]

              [:div#status status-msg]
              [:form {:method "POST" :action "/reset"}
               [:button.reset {:type "submit"} "Reset"]]]]))}))

(defn move-handler [req-map]
  (let [params  (:params req-map)
        row-str (get params "row")
        col-str (get params "col")]
    (if (and row-str col-str)
      (let [row (Integer/parseInt row-str)
            col (Integer/parseInt col-str)]
        (make-move! [row col])))
    (index-handler req-map)))

(defn reset-handler [_]
  (reset-game!)
  {:status  302
   :headers {"Location" "/"}
   :body    ""})

(defn register-routes [^HttpServer server]
  (.addHandler server "GET" "/" (->request-handler index-handler))
  (.addHandler server "POST" "/move" (->request-handler move-handler))
  (.addHandler server "POST" "/reset" (->request-handler reset-handler)))

(defn fallback-handler [_]
  {:status 404
   :body   (str (h/html [:h1 "Route Not Found"]))})

(defn -main [& _]
  (let [fallback (->request-handler fallback-handler)
        server   (HttpServer. 8081 10 fallback)]
    (register-routes server)
    (println "TTT server running on http://localhost:8081")
    (.start server)))