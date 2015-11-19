(ns diary.core
  (:require [liberator.core :refer [resource defresource]]
            [liberator.dev :refer [wrap-trace]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes ANY]]))


(defn parse-body [req key]
  (let [body (slurp (get-in req [:request :body]))]
    [false {key body}]))

(def db (atom {}))

(def common-properties
  {:available-media-types ["application/json"]})

(defresource entry [id]
  common-properties
  :allowed-methods [:get :put]
  :exists? (fn [_] (let [e (get @db id)]
                    (if-not (nil? e)
                      {::entry e})))
  :malformed? #(parse-body % ::data)
  :handle-ok ::entry
  :put! #(swap! db assoc id (::data %)))

(defresource entry-list
  common-properties
  :allowed-methods [:get :post]
  :post! (fn [ctx]
           (let [id (gensym "post-")
                 body (slurp (get-in ctx [:request :body]))]
             (swap! db assoc id body)))
  :handle-ok (apply str (mapcat (fn [[id val]] ["id " id ": " val]) @db)))


(defroutes app
  (ANY ["/entries/:id"] [id :<< symbol ] (entry id))
  (ANY "/entries" [] entry-list))

(def handler 
  (-> app 
      (wrap-trace :header :ui) ))


