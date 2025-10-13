(ns ttt-server.http
  (:import (handlers RequestHandler)
           (server Request Response)))

(defn set-headers [^Response response headers]
  (doseq [[k v] headers]
    (.addHeader response k v)))

(defn ->Response [response]
  (doto (Response.)
    (.setStatusCode (:status response))
    (.setBody (:body response))
    (set-headers (:headers response))))

(defn Request->map [^Request request]
  {:method     (.getMethod request)
   :path       (.getPath request)
   :protocol   (.getProtocol request)
   :session-id (.getSessionId request)
   :raw-body   (.getRawBody request)
   :params     (update-keys (.getParams request) keyword)
   :headers    (.getHeaders request)
   :cookies    (.getCookies request)})

(defn ->request-handler [handler-fn database]
  (proxy [RequestHandler] []
    (handle [^Request request]
      (let [request-map (-> request Request->map (assoc :database database))]
        (-> request-map handler-fn ->Response)))))