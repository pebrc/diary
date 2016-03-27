(ns diary.system
  (:require [com.stuartsierra.component :as component]
            [diary.api.server]))

(defn prod-system [config-options]
  (let [{:keys [port db-uri]} config-options]
    (component/system-map
     :datomic (diary.datomic/new-database db-uri)
     :webserver (component/using
                 (diary.api.server/prod-server port)
                 [:datomic]))))
