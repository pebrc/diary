(ns diary.core
  (:require [liberator.core :refer [resource defresource]]
            [liberator.dev :refer [wrap-trace]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY]]
            [compojure.coercions :refer [as-int]]
            [diary.datomic :as db]))


(defn parse-body [req key]
  (if-let [body (get-in req [:request :body])]
    [false {key (slurp body)}]
    [false]))


(def common-properties
  {:available-media-types ["application/json"]})

(defresource entry [id]
  common-properties
  :allowed-methods [:get :put]
  :exists? (fn [_] (let [e (db/by-id id)]
                    (if-not (= 1 (count e))
                      {::entry e})))
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
           (let [body (slurp (get-in ctx [:request :body]))
                 res (db/create {:diary.entry/text body})]
             {::id (:id res)}))
  :handle-ok (prn-str (into [] (db/list-entries))))


(defroutes app
  (ANY ["/entries/:id"] [id :<< as-int ] (entry id))
  (ANY "/entries" [] entry-list))

(def handler 
  (-> app 
      (wrap-trace :header :ui)))
