(ns diary.datomic
  (:require [datomic.api :as d]))

(def db-uri "datomic:mem://diary")

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

(defn create-db [uri schema]
  (let [created (d/create-database uri)
        conn (d/connect db-uri)]
    (when created
      @(d/transact conn schema))
    conn))

(def conn (create-db db-uri schema))


(defn upsert [entry]
  (d/transact conn [entry]))

(defn create [entry]
  (upsert (assoc entry :db/id (d/tempid :db.part/user))))


(defn by-id [id]
  (d/pull (d/db conn) '[*] id))

(defn list-entries []
  (let [db (d/db conn)]
    (->>  (d/q '[:find [?id ...]
                :where [?id :diary.entry/text]] (d/db conn))
          (map by-id))))
