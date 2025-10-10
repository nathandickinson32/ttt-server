(ns ttt-server.game
  (:require [tic-tac-toe.board :as board]
            [tic-tac-toe.game :as game]))

(def starting-board
  (vec (repeat 3 (vec (repeat 3 nil)))))

(def default-board-size :3x3)

(defn invalid-move? [board move winner draw]
  (or winner draw (get-in board move)))

(defn flag-end-game [winner draw new-state]
  (assoc new-state :winner winner :draw draw))

(defn ->next-state [state move]
  (let [new-state (game/->new-state state move)
        board     (:board new-state)
        size      (:board-size state)
        winner    (cond (board/win? board :X size) :X
                        (board/win? board :O size) :O)
        draw      (board/full-board? board size)]
    (flag-end-game winner draw new-state)))

(defn update-game [{:keys [board winner draw] :as game} move]
  (if (invalid-move? board move winner draw)
    game
    (->next-state game move)))

(defn update-or-ensure-game [game move]
  (if (:board game)
    (update-game game move)
    {:board         starting-board
     :current-token :X
     :turn-count    0}))