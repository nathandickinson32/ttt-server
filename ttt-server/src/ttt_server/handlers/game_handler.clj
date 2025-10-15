(ns ttt-server.handlers.game-handler
  (:require [hiccup2.core :as h]
            [tic-tac-toe.board :as board]
            [tic-tac-toe.player-types :as player-types]
            [ttt-server.game :as game]
            [ttt-server.handlers.core :as core]
            [ttt-server.sessions :as session]))

(defn ->button-square [board winner draw row col]
  (let [move     (get-in board [row col])
        disable? (or move winner draw)]
    [:form {:method   "POST"
            :action   "/move"
            :style    "display:inline;"
            :onsubmit (when disable? "return false;")}
     [:input {:name "row" :type "hidden" :value row}]
     [:input {:name "col" :type "hidden" :value col}]
     [:button.square {:type "submit"} (or move "")]]))

(defn ->button-grid [board winner draw]
  (let [size (count board)]
    (for [row (range size)
          col (range size)]
      (->button-square board winner draw row col))))

(defn ->status-message [current-token winner draw]
  (cond
    winner (str "Winner: " winner)
    draw "Draw!"
    :else (str "Turn: " current-token)))

(defn ->game-handler-body [game]
  (let [{:keys [current-token winner draw board]} game
        board  (or board game/starting-board)
        status (->status-message current-token winner draw)]
    [:html
     [:head
      [:meta {:charset "UTF-8"}]
      [:title "Tic Tac Toe"]
      [:style @core/game-styles]]
     [:body
      [:h1 "Tic Tac Toe"]
      [:div#board {:style (str "--board-size:" (count board) ";")}
       (->button-grid board winner draw)]
      [:div#status status]
      [:form {:method "POST" :action "/reset"}
       [:button.reset {:type "submit"} "Reset"]]]]))

(defn parse-move [params]
  [(Integer/parseInt (get params :row))
   (Integer/parseInt (get params :col))])

(defn ->human-move [game params]
  (if params (game/update-or-ensure-game game (parse-move params))
             game))

(defn next-non-human-state [game]
  (let [ended? (board/end-game? (:board game) (:board-size game))
        human? (= (get game (:current-token game)) :human)]
    (if (or ended? human?)
      game
      (-> game
          (game/update-or-ensure-game (player-types/->player-move game))
          (next-non-human-state)))))

(defn game-handler [session-id game]
  {:status  200
   :headers {"Set-Cookie" (str "sessionId=" session-id)}
   :body    (-> game ->game-handler-body h/html str)})

(defn handle-move [session-id game params]
  (let [after-human (->human-move game params)
        next-state  (next-non-human-state after-human)]
    (session/set-session session-id next-state)
    (game-handler session-id next-state)))

(defn move-handler [request]
  (let [params (:params request)]
    (session/resume-or-new-session
      request
      (fn [session-id game]
        (handle-move session-id game params)))))

(defn initialize-game-state [game {:keys [board-size x-type o-type first-player]}]
  (let [size (if (= board-size :4x4) 4 3)]
    (assoc game
      :board-size board-size
      :board (vec (repeat size (vec (repeat size nil))))
      :X x-type
      :O o-type
      :current-token first-player
      :turn-count 0)))

(defn start-game [session-id game config]
  (let [new-game   (initialize-game-state game config)
        next-state (next-non-human-state new-game)]
    (session/set-session session-id next-state)
    (game-handler session-id next-state)))

(defn ->start-game-config [params]
  {:board-size   (if (= "4x4" (get params :board-size)) :4x4 :3x3)
   :x-type       (keyword (get params :player-x-type))
   :o-type       (keyword (get params :player-o-type))
   :first-player (keyword (get params :first-player))})

(defn game-request-handler [request]
  (let [game-config (->start-game-config (:params request))]
    (session/resume-or-new-session
      request
      (fn [session-id game]
        (start-game session-id game game-config)))))