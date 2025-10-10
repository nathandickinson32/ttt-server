(ns ttt-server.http
  (:import (handlers RequestHandler)
           (server Request Response)))

(defn set-headers [^Response response headers]
  (doseq [[k v] headers]
    (.addHeader response k v)))

(defn ->Response [resp-map]
  (doto (Response.)
    (.setStatusCode (:status resp-map))
    (.setBody (:body resp-map))
    (set-headers (:headers resp-map))))

(defn Request->map [^Request request]
  {:method     (.getMethod request)
   :params     (update-keys (.getParams request) keyword)
   :headers    (.getHeaders request)
   :session-id (.getSessionId request)
   :raw-body   (.getRawBody request)
   })

(defn ->request-handler [handler-fn]
  (proxy [RequestHandler] []
    (handle [^Request request]
      (-> request Request->map handler-fn ->Response))))