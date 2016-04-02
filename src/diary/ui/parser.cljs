(ns diary.ui.parser
  (:require [om.next :as om]))

;; ========== MUTATIONS ========
(defmulti mutate om/dispatch)

(defmethod mutate :default
  [env key params]
  {:remote true
   :value [:entries/list]})

(defmethod mutate 'entry/create
  [{:keys [state]} _  new-entry]
  {:remote true})

(defmethod mutate 'entry/edit
  [{:keys [state]} _ {:keys [db/id]}]
  {:action
   (fn []
     (swap! state assoc :entry/editing [:entries/by-id id]))})

(defmethod mutate 'entry/cancel-edit
  [{:keys [state]} _ _]
  {:action
   (fn []
     (swap! state dissoc :entry/editing))})

(defmethod mutate 'entry/update
  [{:keys [state ref]} _  new-props]
  {:remote true
   :action ;; Optimisitc update as in todo mvc
   (fn []
     (let [_ (print (str "ref: " ref "new: " new-props))]
       (swap! state update-in ref merge new-props)))
   })

(defmethod mutate 'error/ack
  [{:keys [state]} _ ]
  {:action
   (fn []
     (swap! state dissoc 'user/create))}) ;; for now...

;;=========READS ===============
(defn join [st ref]
  (cond-> (get-in st ref)
    (= (:entry/editing st) ref) (assoc :entry/editing true)))

(defn get-entries [state key]
  (let [st @state]
    (into [] (map #(join st %)) (get st key))))



(defmulti read (fn [env key params] key))

(defmethod read :default
  [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value}
      {:value :not-found})))

(defmethod read :entries/list
  [{:keys [state] :as env} key {:keys [start end]}]
  (let [entries (get-entries state key)]
    {:remote true
     :value entries}))

