(ns diary.datomic
  (:require [datomic.api :as d]
            [com.stuartsierra.component :as component]
            [clojure.java.io :as io]
            [manifold.deferred :as m])
  (:import datomic.Util
           java.util.concurrent.ExecutionException))

(defn extract-cause [e]
  (hash-map :error (:cause  (Throwable->map  (.getCause e))) ))


(defn upsert [conn entry]
  (d/transact conn [entry]))

(defn create [conn entry]
  (let [;;res ;; @(-> (m/->deferred
            ;;      (upsert conn (assoc entry :db/id (d/tempid :db.part/user))))
            ;;      (m/catch ExecutionException extract-cause))
        res @(upsert conn (assoc entry :db/id (d/tempid :db.part/user)))
        _ (println "datomic create" res)]
    res))


(defn by-id [conn id]
  (d/pull (d/db conn) '[*] id))

(defn list-entries [conn]
  (let [db (d/db conn)]
    (->>  (d/q '[:find [?id ...]
                :where [?id :diary.entry/text]] (d/db conn))
          (map (partial  by-id conn))
          (vec))))

(defrecord DatomicDatabase [uri schema initial-data connection]
  component/Lifecycle
  (start [component]
    (d/create-database uri)
    (let [c (d/connect uri)]
      @(d/transact c schema)
      (assoc component :connection c)))
  (stop [component]
    component))

(defn new-database [db-uri]
  (DatomicDatabase.
   db-uri
   (first (Util/readAll (io/reader (io/resource "data/schema.edn"))))
   []
   nil))
