(ns diary.datomic
  (:require [datomic.api :as d]
            [com.stuartsierra.component :as component]))


(def schema
  [{:db/id (d/tempid :db.part/db)
    :db/ident :diary.entry/text
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}
   {:db/id (d/tempid :db.part/db)
    :db/ident :diary.entry/date
    :db/valueType :db.type/instant
    :db/cardinality :db.cardinality/one
    :db.install/_attribute :db.part/db}])



(defn upsert [conn entry]
  (d/transact conn [entry]))

(defn create [conn entry]
  (let [ res @(upsert conn (assoc entry :db/id (d/tempid :db.part/user)))
        _ (println res)]
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
   schema
   []
   nil))
