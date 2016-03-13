(ns diary.ui.entry
  (:require
    [om.next :as om :refer-macros [defui]]
    [om.dom :as dom]
    [diary.ui.util :as u]) )

(defn edit [c {:keys [db/id] :as props}]
  (om/transact! c `[(entry/edit {:db/id ~id})]))

(defn reader-view [c {:keys [diary.entry/text] :as props}]
  (dom/div #js {:className "view"
                :onDoubleClick (fn [e] (edit c props))}
           text))

(defn edit-field [c props]
  (dom/textarea
   #js {:value (om/get-state c :edit-text)
        :className "edit"
        }))

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
