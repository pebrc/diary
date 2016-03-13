(ns diary.ui
  (:require [clojure.string :as string]
            [goog.dom :as gdom]
            [goog.log :as glog]
            [goog.debug :as debug]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cognitect.transit :as t]
            [diary.ui.util :as u])
  (:import [goog.net XhrIo]))

(enable-console-print!)

(defn handle-response [cb]
  (fn [e]
    (let [res (.. e -target)]
      (if (.isSuccess res)
        (cb (t/read (t/reader :json) (.getResponseText res)))
        (cb {:app/title (.getResponseText res)})))))

(defn api-post [url]
  (fn [{:keys [remote] :as ctx} cb]
    (.send XhrIo url
           (handle-response cb)
           "POST" (t/write (t/writer :json) remote)
           #js {"Content-Type" "application/transit+json"})))

(def app-state
  (atom
    {:app/title "Diary"
     :entries/list []}))

(def ESCAPE_KEY 27)
(def ENTER_KEY 13)


(defn submit [c {:keys [db/id]} e]
  (let [edited-text (string/trim (or (om/get-state c :edit-text) ""))
        submit-id (or id :temp)
        now (js/Date.)]
    (om/transact! c `[(entry/create {:diary.entry/text ~edited-text :diary.entry/date ~now}) :entries/list])))

(defn edit [c {:keys [db/id] :as props}]
  (om/transact! c `[(entry/edit {:db/id ~id})]))

(defn keydown [c props e]
  (condp == (.-keyCode e)
    ESCAPE_KEY  (do
                  (om/update-state! c assoc :edit-text "")
                  (doto e (.preventDefault) (.stopPropagation)))
    ENTER_KEY (submit c props e) 
    nil))

(defn change [c e]
  (om/update-state! c assoc :edit-text (.. e -target -value)))

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

(defn reader-view [c {:keys [diary.entry/text] :as props}]
  (dom/div #js {:className "view"
                :onDoubleClick (fn [e] (edit c props))}
                         text))

(defn edit-field [c props]
  (dom/input
   #js {:value (om/get-state c :edit-text)
        :className "edit"}))

(defui Entry
  static om/Ident
  (ident [this {:keys [db/id]}]
         [:entries/by-id id])
  static om/IQuery
  (query [this]
         [:db/id :diary.entry/text :entry/editing])
  Object
  (render [this]
          (let [props (om/props this)
                {:keys [diary.entry/date entry/editing]} props
                _ (print props)
                class (cond-> ""
                        editing (str "editing"))]
            (dom/li #js{:className class}
                    (dom/h2 nil  (str (u/format-date "EEEE HH:mm"  date) ":"))
                    (reader-view this props)
                    (edit-field this props))
            )))

(def item (om/factory Entry {:keyfn :db/id}))

(defui Diary
  static om/IQuery
  (query [this]
         `[:app/title {:entries/list ~(om/get-query Entry)} ])
  Object
  (render [this]
          (let [props (om/props this)
                {:keys [app/title entries/list]} props]
      (dom/div nil
        (dom/h1 nil title)
        (apply dom/ul #js{:id "entries"}
          (map item list))
        (dom/input #js {:ref "new-entry-input"
                        :id "new-entry"
                        :placeholder "What happened today?"
                        :value (om/get-state this :edit-text)
                        :onChange #(change this %)
                        :onKeyDown #(keydown this props %)})))))

(def reconciler
  (om/reconciler
   {:state app-state
    :normalize true
    :send (api-post "http://localhost:3449/api")
    :parser (om/parser {:read read
                         :mutate mutate})}))

(defn log-state-change [k r old new]
  (print new))

(add-watch app-state :debug-watch log-state-change)


(om/add-root! reconciler
  Diary (gdom/getElement "app"))



