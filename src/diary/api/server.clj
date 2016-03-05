(ns diary.api.server
  (:require [om.next.server :as om]
            [diary.api.parser :as parser]
            [diary.middleware :refer [wrap-transit-body
                                      wrap-transit-response
                                      wrap-transit-params]]
            [compojure.core :refer [defroutes ANY]]
            [compojure.coercions :refer [as-int]]))


(defn ring-response [body & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/transit+json"}
   :body body})

(defn api [req]
  (let [_ (println (str "request: " (:body req)))
        resp ((om/parser {:read parser/readfn :mutate parser/mutatefn}) {} (:body req))
        _ (println "response" resp)]
    (ring-response resp)))


(defroutes app
  (ANY "/api" [] api))


(def handler
  (-> app
      (wrap-transit-body)
      (wrap-transit-response)))


(comment
  ;;testing
  (def parse (om/parser {:read parser/readfn :mutate parser/mutatefn}))
  (parse {} '[(entry/create {:diary.entry/text "asdf"})])
  )
