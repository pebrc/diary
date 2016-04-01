(ns diary.ui.om
  (:require
   [om.next :as om :refer-macros [defui]]
   [om.dom :as dom]))

(def ESCAPE_KEY 27)
(def ENTER_KEY 13)

(defn submit [c k tf]
  (fn [e]
    (let [new-entity  (om/get-state c k)]
      (om/update-state! c dissoc k )
      (om/transact! c (tf new-entity))
      (doto e (.preventDefault) (.stopPropagation)))))


(defn change [c k-or-ks]
  (fn [e]
    (om/update-state! c assoc-in
                      k-or-ks (.. e -target -value))))



(defn input [c k-or-ks attrs]
  (let [on-change  (change c  k-or-ks)
        defaults {:value (om/get-state c k-or-ks)
                  :onChange on-change
                  }]
    (dom/input (clj->js (merge defaults attrs)))))




