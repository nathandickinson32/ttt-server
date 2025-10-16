(ns ttt-server.handlers.core)

(def game-styles (delay (slurp "resources/public/stylesheet.css")))

(defn head [title]
  [:head
   [:meta {:charset "UTF-8"}]
   [:title title]
   [:style @game-styles]])