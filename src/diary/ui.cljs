(ns diary.ui
  (:require [clojure.string :as string]
            [goog.dom :as gdom]
            [goog.log :as glog]
            [goog.debug :as debug]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [cognitect.transit :as t]
            [diary.ui.entry :as e]
            [diary.ui.parser :as p])
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
    (om/update-state! c assoc :edit-text "")
    (om/transact! c `[(entry/create {:diary.entry/text ~edited-text :diary.entry/date ~now}) :entries/list])
    (doto e (.preventDefault) (.stopPropagation))))



(defn keydown [c props e]
  (condp == (.-keyCode e)
    ESCAPE_KEY  (do
                  (om/update-state! c assoc :edit-text "")
                  (doto e (.preventDefault) (.stopPropagation)))
    ENTER_KEY (submit c props e) 
    nil))

(defn change [c e]
  (om/update-state! c assoc :edit-text (.. e -target -value)))



(defui Diary
  static om/IQuery
  (query [this]
         `[:app/title {:entries/list ~(om/get-query e/Entry)} ])
  Object
  (render [this]
          (let [props (om/props this)
                {:keys [app/title entries/list]} props]
      (dom/div nil
               (dom/h1 nil title)
               (dom/textarea
                #js {:ref "new-entry-input"
                     :id "new-entry"
                     :placeholder "What happened today?"
                     :value (om/get-state this :edit-text)
                     :onChange #(change this %)
                     :onKeyDown #(keydown this props %)
                     :rows 10})
               (apply dom/ul #js{:id "entries"}
                      (map e/item list))))))

(def reconciler
  (om/reconciler
   {:state app-state
    :normalize true
    :send (api-post "http://localhost:3449/api")
    :parser (om/parser {:read p/read
                         :mutate p/mutate})}))

(defn log-state-change [k r old new]
  ;;(print new)
  )

(add-watch app-state :debug-watch log-state-change)


(om/add-root! reconciler
  Diary (gdom/getElement "app"))



