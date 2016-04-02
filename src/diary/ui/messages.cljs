(ns diary.ui.messages
  (:require
   [om.next :as om :refer-macros [defui]]
   [om.dom :as dom]))


(defui Errors
  static om/IQuery
  (query [this]
         [:error])
  Object
  (componentDidUpdate [this prev-props prev-state]
                     (let [props (om/props this)
                        error (:error props)]
                       (if error 
                         (js/setTimeout #(om/transact! this '[(error/ack)]) 5000))))
  (render [this]
          (let [props (om/props this)
                error (:error props)]
            (if error
              (dom/div #js {:className "error message"}
                       (dom/h3 nil "An error occurred:")
                       (dom/p nil error))))))

(def errors (om/factory Errors))
