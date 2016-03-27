(require '[figwheel-sidecar.repl :as r]
         '[figwheel-sidecar.repl-api :as ra]
         '[diary.api.server :refer :all]
         '[com.stuartsierra.component :as component])


(def figwheel-config {:figwheel-options
                      {:ring-handler diary.api.server/handler
                       :nrepl-port 4000 }
                      :build-ids ["dev"]
                      :all-builds
                      [{:id "dev"
                                    :source-paths ["src"]
                                    :figwheel true
                                    :compiler {:main "diary.ui"
                                               :asset-path "js"
                                               :output-to "resources/public/js/main.js"
                                               :output-dir "resources/public/js"
                                               :verbose true}
                                    }]})



(defrecord Figwheel []
  component/Lifecycle
  (start [config]
    (ra/start-figwheel! config)
    config)
  (stop [config]
    ;; you may want to restart other components but not Figwheel
    ;; consider commenting out this next line if that is the case
    (ra/stop-figwheel!)
        config))

(def system
  (atom
   (component/system-map
    :datomic (diary.datomic/new-database "datomic:mem://diary")
    :figwheel   (map->Figwheel figwheel-config))))

(defn start []
  (swap! system component/start))

(defn stop []
  (swap! system component/stop))

(defn reload []
  (stop)
  (start))

(defn repl []
    (ra/cljs-repl))
