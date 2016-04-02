(ns diary.api.server
  (:require [clojure.walk :as walk]
            [om.next.server :as om]
            [diary.api.parser :as parser]
            [diary.middleware :refer [wrap-transit-body
                                      wrap-transit-response
                                      wrap-transit-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.util.response :as resp]
            [compojure.core :refer [defroutes routes ANY GET]]
            [compojure.coercions :refer [as-int]]
            [com.stuartsierra.component :as component]
            [aleph.http :as http]))

(defn ring-response [body & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/transit+json"}
   :body body})

(defn extract-cause [e errors]
  (let [cause (:cause  (Throwable->map  (.getCause e)))
        _ (swap! errors assoc 400 cause )]
    (assoc {} :error cause )))

(defn handle-errors [res]
  (let [errors (atom {})
        pretty (walk/postwalk (fn [x] (if (instance? Throwable x)
                                 (extract-cause x errors)
                                 x)) res)]
    [pretty (apply max 200 (keys @errors))]))

(defn api [req]
  (let [_ (println (str "request: " (:body req)))
        resp ((om/parser {:read parser/readfn :mutate parser/mutatefn}) {:conn (:datomic req) } (:body req))
        resp' (handle-errors resp)
        _ (println "response: " resp')]
    (apply ring-response resp')))


(defroutes app
  (routes
   (GET "/" [] (resp/redirect "/index.html"))
   (ANY "/api" [] api)))


(defn wrap-datomic [handler conn]
  (fn [req] (handler (assoc req :datomic conn))))

(defn log-request [handler]
  (fn [request]
    (let [_ (println request)]
      (handler request))))

(defn handler [conn] 
  (-> app
      (wrap-datomic conn)
      (wrap-transit-body)
      (wrap-transit-response)
      (log-request)))

 (def prod-handler (wrap-resource handler "public"))


;;=================================================================================
;; Component
(defrecord HTTPServer [server port handler datomic]
  component/Lifecycle
  (start [component]
    (let [server (http/start-server (handler (:connection datomic)) {:port port})]
      (assoc component :server server)))
  (stop [component]
    (.close server)))

(defn prod-server [port]
  (HTTPServer. nil port prod-handler nil))


