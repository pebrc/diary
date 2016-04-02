(ns diary.api.parser
  (:require [diary.datomic :as d]))

;;; READS
(defmulti readfn (fn [env k params] k))

(defmethod readfn :default
  [_ k _]
  {:value {:error (str "no read handler for key" k)}})

(defmethod readfn :entries/by-id
  [{:keys [conn]} _ {:keys [id]}]
  {:value (d/by-id conn id)})

(defmethod readfn :entries/list
  [{:keys [conn]} _ _]
  {:value (d/list-entries conn)})

;;; MUTATIONS
(defmulti mutatefn (fn [env k params] k))

(defmethod mutatefn :default
  [_ k _]
  {:value {:error (str "no mutation handler for key " k)}})

(defmethod mutatefn 'entry/create
  [{:keys [conn]} k entry]
  (let [_ (println entry)]
    {:value {:keys [[:entries/list]]}
     :action (fn [] (d/create conn entry))}))

(defmethod mutatefn 'entry/update
  [{:keys [conn]} k {:keys [db/id] :as entry}]
  {:value {:keys [[:entries/by-id id]]}
   :action (fn [] (d/upsert conn entry))})
