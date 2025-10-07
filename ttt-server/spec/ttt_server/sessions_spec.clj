(ns ttt-server.sessions-spec
  (:require [speclj.core :refer :all]
            [ttt-server.game :as game]
            [ttt-server.helpers :as helper]))

(describe "Tic Tac Toe Sessions"

  (context "Starting State"

    (it "starts with empty board and default state"
      (let [session-id (helper/new-session)]
        (helper/reset-session session-id)
        (let [{:keys [board current-token X O board-size turn-count]} (helper/get-game session-id)]
          (should= helper/nil-board board)
          (should= :X current-token)
          (should= :human X)
          (should= :human O)
          (should= game/default-board-size board-size)
          (should= 0 turn-count))))
    )

  (context "Independent Sessions"

    (it "starts with empty boards for each session"
      (let [session1 (helper/new-session)
            session2 (helper/new-session)]
        (helper/reset-session session1)
        (helper/reset-session session2)
        (helper/apply-moves session1 [[0 0]])
        (should-be-nil (get-in (helper/get-game session2) [:board 0 0]))
        (should= :X (get-in (helper/get-game session1) [:board 0 0]))))

    (it "keeps moves separate across sessions"
      (let [session1 (helper/new-session)
            session2 (helper/new-session)]
        (helper/reset-session session1)
        (helper/reset-session session2)
        (helper/apply-moves session1 [[0 0]])
        (helper/apply-moves session2 [[1 1]])
        (should= :X (get-in (helper/get-game session1) [:board 0 0]))
        (should-be-nil (get-in (helper/get-game session1) [:board 1 1]))
        (should= :X (get-in (helper/get-game session2) [:board 1 1]))
        (should-be-nil (get-in (helper/get-game session2) [:board 0 0]))))
    )
  )