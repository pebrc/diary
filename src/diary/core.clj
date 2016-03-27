(ns diary.core
  (:require [com.stuartsierra.component :as component]
            [diary.system :as system]))


(def current-system (atom nil))


(defn swap-system [old new]
  (when-not (nil? old)
    (do
      (println "stoppping existing component")
      (component/stop old)))
  (component/start new));;swap should be side effect free ...

;;===============================================================
;; prod


(def default-config {:db-uri "datomic:mem://diary"
                      :port 3449})


(defn start [config]
  (let [s (system/prod-system config)]
    (swap! current-system swap-system s)))


(defn stop []
  (swap! current-system component/stop))

