(defproject diary "0.1.0-SNAPSHOT"

  :min-lein-version "2.5.3"
  :plugins [[lein-ring "0.9.7"]
            [lein-figwheel "0.5.0-5"]
            [lein-cljsbuild "1.1.3"]]
  :description "A simple diary application. Purely educational based on the om.next todomvc example"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.8.40"]
                 [com.datomic/datomic-free "0.9.5327" :exclusions [joda-time]]
                 [com.cognitect/transit-clj "0.8.285"]
                 [com.cognitect/transit-cljs "0.8.237"]
                 [org.omcljs/om "1.0.0-alpha22"]
                 [secretary "1.2.3"]
                 [com.stuartsierra/component "0.3.1"]
                 [manifold "0.1.3"]
                 [aleph "0.4.1-beta4"]
                 [compojure "1.4.0"]
                 [ring/ring-core "1.4.0"]]
  :clean-targets [:target-path "out"]
  :cljsbuild {
    :builds [{
              :source-paths ["src"]
              :figwheel false
              :compiler {:main "diary.ui"
                         :asset-path "js"
                         :output-to "resources/public/js/main.js"
                         :output-dir "resources/public/js"
                         :optimizations :whitespace
                         :pretty-print true}
              }]}
  
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[figwheel-sidecar "0.5.0-1"]
                                  [com.cemerick/piggieback "0.2.1"]]}}
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]})
