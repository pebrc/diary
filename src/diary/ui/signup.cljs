(ns diary.ui.signup
  (:require
    [clojure.string :as string]
    [om.next :as om :refer-macros [defui]]
    [om.dom :as dom]
    [diary.ui.util :as u]))


(defui SignUp
  Object
  (render [this]
          (dom/div #js{:className "signup"}
                   (dom/h1 nil "Create an Account")
                   (dom/form nil
                             (dom/ul nil
                                     (dom/li nil
                                             (dom/label #js{:htmlFor "firstNameField"} "Firstname")
                                             (dom/input #js{:ref "firstNameField"
                                                            :name "firstNameField"
                                                            :required true} ))
                                     (dom/li nil
                                             (dom/label #js{:htmlFor "nameField"} "Lastname")
                                             (dom/input #js{:ref "nameField"
                                                            :required true} ))
                                     (dom/li nil
                                             (dom/label #js{:htmlFor "passwordField"} "Password")
                                             (dom/input #js{:ref "passwordField"
                                                            :type "password"
                                                            :required true}))
                                     (dom/li nil
                                             (dom/button #js{:type "submit"
                                                             :className "submit"} "Create an Account")))))
          ))
