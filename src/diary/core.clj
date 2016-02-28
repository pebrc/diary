(ns diary.core
  (:require [liberator.core :refer [resource defresource]]
            [liberator.dev :refer [wrap-trace]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY]]
            [compojure.coercions :refer [as-int]]
            [diary.datomic :as db]
            [diary.middleware :refer [wrap-transit-body
                                      wrap-transit-response
                                      wrap-transit-params]]))


(defn parse-body [req key]
  (if-let [body (get-in req [:request :body])]
    [false {key (slurp body)}]
    [false]))


(def common-properties
  {:available-media-types ["application/transit+json"]})

(defresource entry [id]
  common-properties
  :allowed-methods [:get :put]
  :exists? (fn [_] (let [e (db/by-id id)]
                    (if-not (= 1 (count e))
                      {::entry {:entries/list [e]}})))
  :malformed? #(parse-body % ::data)
  :handle-ok ::entry
  :can-put-to-missing? false
  :put! #(db/upsert {:diary.entry/text  (::data %) :db/id id})
  )

(defresource entry-list
  common-properties
  :allowed-methods [:get :post]
  :post-redirect? (fn [ctx] {:location (format  "/entries/%s" (::id ctx))})
  :post! (fn [ctx]
           (let [body (get-in ctx [:request :body])
                 _ (println body)
                 res (db/create {:diary.entry/text (:text (second (first body)))})]
             {::id (:id res)}))
  :handle-ok {:entries/list (into [] (db/list-entries))})


(defroutes app
  (ANY ["/entries/:id"] [id :<< as-int ] (entry id))
  (ANY "/entries" [] entry-list))

(def handler 
  (-> app
      (wrap-transit-body)
      (wrap-transit-response)
      (wrap-trace :header :ui)))
