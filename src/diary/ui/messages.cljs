(ns diary.ui.messages
  (:require
   [om.next :as om :refer-macros [defui]]
   [om.dom :as dom]))


(defui Errors
  static om/IQuery
  (query [this]
         [:error :ref])
  Object
  (componentDidMount [this]
                     (let [ref (:ref (om/props this))]
                       (js/setTimeout #(om/transact! this `[(error/ack {:ref ~ref})]) 5000)))
  (render [this]
          (let [props (om/props this)
                {:keys [error ref]} props]
            (dom/div #js {:className "error message"}
                     (dom/h3 #js{:key ref} "An error occurred:")
                     (dom/p nil error)))))

(defn errors [es]
  (if (and es (not= :not-found es))
    (map  (om/factory Errors) es)))
