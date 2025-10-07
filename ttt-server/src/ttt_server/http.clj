(ns ttt-server.http
  (:import (handlers RequestHandler)
           (server Request Response)))

(defn set-headers [^Response resp headers]
  (doseq [[k v] headers]
    (.addHeader resp k v)))

(defn ->Response [resp-map]
  (doto (Response.)
    (.setStatusCode (:status resp-map))
    (.setBody (:body resp-map))
    (set-headers (:headers resp-map))))

(defn ->request-handler [handler-fn]
  (proxy [RequestHandler] []
    (handle [^Request req]
      (-> req handler-fn ->Response))))