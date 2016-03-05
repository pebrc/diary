(ns diary.api.parser
  (:require [diary.datomic :as d]))

;;; READS
(defmulti readfn (fn [env k params] k))

(defmethod readfn :default
  [_ k _]
  {:value {:error (str "no read handler for key" k)}})

(defmethod readfn :entries/by-id
  [_ _ {:keys [id]}]
  {:value (d/by-id id)})

(defmethod readfn :entries/list
  [_ _ _]
  {:value (d/list-entries)})

;;; MUTATIONS
(defmulti mutatefn (fn [env k params] k))

(defmethod mutatefn :default
  [_ k _]
  {:value {:error (str "no mutation handler for key" k)}})

(defmethod mutatefn 'entry/create
  [_ k entry]
  (let [_ (println entry)]
    {:value {:keys [[:entries/list]]}
     :action (fn [] (d/create entry))}))
