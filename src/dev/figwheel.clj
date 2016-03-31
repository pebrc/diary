(require '[figwheel-sidecar.repl :as r]
         '[figwheel-sidecar.repl-api :as ra]
         '[diary.api.server :refer :all]
         '[com.stuartsierra.component :as component])


(def figwheel-config {:figwheel-options { :css-dirs ["resources/public/css"]
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



(defrecord Figwheel [config config-factory datomic]
  component/Lifecycle
  (start [component]
    (let [config (config-factory datomic)]
      (ra/start-figwheel! (config-factory datomic)))
    (assoc component :config  config))
  (stop [component]
    ;; you may want to restart other components but not Figwheel
    ;; consider commenting out this next line if that is the case
    (ra/stop-figwheel!)
        component))

(def system
  (atom
   (component/system-map
    :datomic (diary.datomic/new-database "datomic:mem://diary")
    :figwheel  (component/using  (map->Figwheel {:config-factory (fn [datomic]
                                                                   (assoc-in
                                                                    figwheel-config
                                                                    [:figwheel-options :ring-handler]
                                                                    (diary.api.server/handler
                                                                     (:connection datomic))))})
                                 [:datomic]))))

(defn start []
  (swap! system component/start))

(defn stop []
  (swap! system component/stop))

(defn reload []
  (stop)
  (start))

(defn repl []
    (ra/cljs-repl))
