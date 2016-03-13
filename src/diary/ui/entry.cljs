(ns diary.ui.entry
  (:require
    [clojure.string :as string]
    [om.next :as om :refer-macros [defui]]
    [om.dom :as dom]
    [diary.ui.util :as u]) )

(def ESCAPE_KEY 27)
(def ENTER_KEY 13)


;;------- event handler ----

(defn submit [c {:keys [db/id diary.entry/text] :as props} e]
  (let [edit-text (string/trim (or (om/get-state c :edit-text) ""))]
    (om/transact! c
                  (cond-> '[(entry/cancel-edit)]
                    (and (not (string/blank? edit-text))
                         (not= edit-text text))
                    (into
                     `[(entry/update {:db/id ~id :diary.entry/text ~edit-text})
                       [:entries/by-id ~id]])))
    (doto e (.preventDefault) (.stopPropagation))))

(defn edit [c {:keys [db/id diary.entry/text] :as props}]
  (om/transact! c `[(entry/edit {:db/id ~id}) :entries/list])
  (om/update-state! c merge { :edit-text text}))

(defn change [c e]
  (om/update-state! c assoc
                    :edit-text (.. e -target -value)))

(defn key-down [c {:keys [diary.entry/text] :as props} e]
  (condp == (.-keyCode e)
    ESCAPE_KEY
    (do
        (om/transact! c '[(entry/cancel-edit)])
        (om/update-state! c assoc :edit-text text)
        (doto e (.preventDefault) (.stopPropagation)))
    ENTER_KEY
    (submit c props e)
    nil))


;; ---- rendering ---------
(defn reader-view [c {:keys [diary.entry/text] :as props}]
  (dom/div #js {:className "view"
                :onDoubleClick (fn [e] (edit c props))}
           text))

(defn edit-field [c props]
  (dom/div #js{:className "edit"}
           (dom/textarea
            #js {:value (om/get-state c :edit-text) 
                 :ref "editField"
                 :rows 10
                 :onKeyDown #(key-down c props %)
                 :onKeyUp #(print (.-keyCode %))
                 :onBlur #(submit c props %)
                 :onChange #(change c %)
                 })))

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
                class (cond-> ""
                        editing (str "editing"))]
            (dom/li #js{:className class}
                    (dom/h2 nil  (str (u/format-date "EEEE HH:mm"  date) ":"))
                    (reader-view this props)
                    (edit-field this props))
            )))

(def item (om/factory Entry {:keyfn :db/id}))
