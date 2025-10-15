(ns ttt-server.handlers.fallback-handler
  (:require [hiccup2.core :as h]))

(defn fallback-handler [_]
  {:status 404
   :body   (str (h/html [:h1 "Route Not Found"]))})