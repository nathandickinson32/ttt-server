(ns ttt-server.core
  (:gen-class)
  (:require [hiccup2.core :as h])
  (:import [server HttpServer Request Response]
           [handlers RequestHandler HelloHandler]))

(defn html-response [html]
  (doto (Response.)
    (.setStatusCode 200)
    (.setStatusMessage "OK")
    (.addHeader "Content-Type" "text/html")
    (.setBody html)))

(def status-messages
  {200 "OK"
   404 "Not Found"})

(defn set-headers [response headers]
  (doseq [[k v] headers]
    (.addHeader response k v)))

(defn Request->map [^Request request]
  {:method (.getMethod request)
   :params (.getParams request)})

(defn ->Response [response]
  (doto (Response.)
    (.setStatusCode (:status response))
    (.setStatusMessage (get status-messages (:status response)))
    (.setBody (:body response))
    (set-headers (:headers response))))

(defn ->request-handler [handler]
  (proxy [RequestHandler] []
    (handle [^Request request]
      (-> request Request->map handler ->Response))))

(defn fallback-handler-2 [_request]
  {:status 404
   :body   "Route not found"})

(defn fallback-handler []
  (proxy [RequestHandler] []
    (handle [_request]
      (doto (Response.)
        (.setStatusCode 404)
        (.setStatusMessage "Not Found")
        (.setBody "Route not found")))))

(defn index-handler-2 [_request]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (str (h/html [:h1 "Welcome to Tic Tac Toe"]))})

(defn index-handler []
  (proxy [RequestHandler] []
    (handle [_request]
      (html-response "<h1>Welcome to Tic Tac Toe</h1>"))))

(defn register-routes [^HttpServer server]
  (.addHandler server "GET" "/"
               (->request-handler index-handler-2)
               ;(index-handler)
               ))

(defn -main [& _]
  (let [
        ;fallback (fallback-handler)
        fallback (->request-handler fallback-handler-2)
        server   (HttpServer. 8081 fallback)]
    (register-routes server)
    (println "TTT server running on http://localhost:8081")
    (.start server)))