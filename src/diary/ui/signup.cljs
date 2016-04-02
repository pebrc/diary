(ns diary.ui.signup
  (:require
    [clojure.string :as string]
    [om.next :as om :refer-macros [defui]]
    [om.dom :as dom]
    [diary.ui.om :as o]
    [diary.ui.messages :as m]
    [diary.ui.util :as u]))


(defui SignUp
  static om/IQuery
  (query [this]
         `[{user/create ~(om/get-query m/Errors)}])
  Object
  (render [this]
          (let [props (om/props this)
                errors (get props 'user/create)]
            (dom/div #js{:className "signup"}
                     (m/errors errors)
                     (dom/h1 nil "Create an Account")
                     (dom/form #js{:onSubmit  (o/submit
                                               this
                                               :user
                                               (fn [e] `[(user/create ~e)])) }
                               (dom/ul nil
                                       (dom/li nil
                                               (dom/label #js{:htmlFor "firstNameField"}
                                                          "Firstname")
                                               (o/input this
                                                        [:user :firstname]
                                                        {:ref "firstNameField"
                                                         :name "firstNameField"
                                                         :required true} ))
                                       (dom/li nil
                                               (dom/label #js{:htmlFor "nameField"}
                                                          "Lastname")
                                               (o/input this
                                                        [:user :lastname]
                                                        {:ref "nameField"
                                                         :required true} ))
                                       (dom/li nil
                                               (dom/label #js{:htmlFor "passwordField"}
                                                          "Password")
                                               (o/input this
                                                        [:user :password]
                                                        {:ref "passwordField"
                                                         :type "password"
                                                         :required true}))
                                       (dom/li nil
                                               (dom/button #js{:type "submit"
                                                               :className "submit"}
                                                           "Create an Account"))))))
          ))
