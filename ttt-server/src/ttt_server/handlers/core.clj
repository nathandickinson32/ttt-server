(ns ttt-server.handlers.core)

(def game-styles (delay (slurp "resources/public/stylesheet.css")))