(ns diary.ui
  (:require [clojure.string :as string]
            [goog.dom :as gdom]
            [goog.log :as glog]
            [goog.debug :as debug]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom])
  (:import [goog.net XhrIo]))


(defn api-post [url]
  (fn [{:keys [remote] :as ctx} cb]
    (.send XhrIo url
           (fn [e]
             (print (.getResponseText (.. e -target)))
             (cb {:entries/list [{:date (js/Date.) :text "posted"}]}))
           "POST" remote
           #js {"Content-Type" "application/json"})))


(def app-state
  (atom
    {:app/title "Diary"
     :entries/list
     [{:date #inst "2015-11-21T00:00" :text "yadda yadda"}
      {:date #inst "2015-11-23T00:00" :text "yadda yadda"}
      {:date #inst "2015-11-24T00:00" :text "yadda yadda"}
      {:date #inst "2015-11-25T00:00" :text "yadda yadda"}
      {:date #inst "2015-11-28T00:00" :text "yadda yadda"}]}))

(def ESCAPE_KEY 27)
(def ENTER_KEY 13)


(defn submit [c {:keys [db/id]} e]
  (let [edited-text (string/trim (or (om/get-state c :edit-text) ""))
        submit-id (or id :temp)
        now (js/Date.)]
    (om/transact! c `[(entry/create {:date ~now :text ~edited-text})] )))

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
  {:value [:entries/list]
   :remote true
   :action 
   (fn []
     (swap! state update-in [:entries/list] conj new-entry))
   })

(defmulti read (fn [env key params] key))

(defmethod read :default
  [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value}
      {:value :not-found})))

(defmethod read :entries/list
  [{:keys [state] :as env} key {:keys [start end]}]
  (let [entries (:entries/list @state)]
    {:value (subvec entries start (min end (count entries)))}))

(defui Diary
  static om/IQueryParams
  (params [this]
    {:start 0 :end 100})
  static om/IQuery
  (query [this]
    '[:app/title (:entries/list {:start ?start :end ?end})])
  Object
  (render [this]
          (let [props (om/props this)
                {:keys [app/title entries/list]} props]
      (dom/div nil
        (dom/h2 nil title)
        (apply dom/ul nil
          (map
            (fn [{:keys [date text]}]
              (dom/li nil (str date ": " text)))
            list))
        (dom/input #js {:ref "new-entry-input"
                        :id "new-entry"
                        :placeholder "What happened today?"
                        :value (om/get-state this :edit-text)
                        :onChange #(change this %)
                        :onKeyDown #(keydown this props %)})))))

(def reconciler
  (om/reconciler
   {:state app-state
    :send (api-post "http://localhost:3449/entries")
    :parser (om/parser {:read read
                         :mutate mutate})}))

(defn log-state-change [k r old new]
  (print new))

(add-watch app-state :debug-watch log-state-change)


(om/add-root! reconciler
  Diary (gdom/getElement "app"))

(enable-console-print!)

