(ns ttt-server.game-spec
  (:require [speclj.core :refer :all]
            [ttt-server.game :as game]
            [ttt-server.helpers :as helper]))

(describe "Tic Tac Toe Game Logic"

  (context "Moves and Turns"

    (it "places X, switches to O, and prevents duplicate moves"
      (let [session-id (helper/new-session)]
        (helper/reset-session session-id)
        (helper/apply-moves session-id [[0 0]])
        (let [game-state (helper/get-game session-id)]
          (should= :O (:current-token game-state))
          (should= :X (get-in game-state [:board 0 0]))
          (should= 1 (:turn-count game-state)))
        (helper/apply-moves session-id [[0 0]])
        (let [game-state (helper/get-game session-id)]
          (should= :O (:current-token game-state))
          (should= 1 (:turn-count game-state)))))

    (it "flags X win"
      (let [session-id (helper/new-session)]
        (helper/reset-session session-id)
        (helper/apply-moves session-id [[0 0] [1 0] [0 1] [1 1] [0 2]])
        (should= :X (:winner (helper/get-game session-id)))
        (should= 5 (:turn-count (helper/get-game session-id)))))

    (it "flags O win"
      (let [session-id (helper/new-session)]
        (helper/reset-session session-id)
        (helper/apply-moves session-id [[0 0] [1 0] [0 1] [1 1] [2 2] [1 2]])
        (should= :O (:winner (helper/get-game session-id)))))

    (it "flags draw on full board"
      (let [session-id (helper/new-session)]
        (helper/reset-session session-id)
        (helper/apply-moves session-id [[1 1] [0 0] [1 0] [1 2] [0 2] [2 0] [0 1] [2 1] [2 2]])
        (should (:draw (helper/get-game session-id)))))

    (it "resets board"
      (let [session-id (helper/new-session)]
        (helper/reset-session session-id)
        (helper/apply-moves session-id [[0 0]])
        (helper/reset-session session-id)
        (let [{:keys [board current-token winner draw]} (helper/get-game session-id)]
          (should= helper/nil-board board)
          (should= :X current-token)
          (should-be-nil winner)
          (should-not draw))))
    )
  )