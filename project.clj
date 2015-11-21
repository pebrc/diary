(defproject diary "0.1.0-SNAPSHOT"
  :plugins [[lein-ring "0.9.7"]]
  :description "A simple diary application"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0-RC2"]
                 [com.datomic/datomic-free "0.9.5327" :exclusions [joda-time]]
                 [liberator "0.14.0"]
                 [compojure "1.4.0"]
                 [ring/ring-core "1.4.0"]]
  :ring {:handler diary.core/handler
         :nrepl {:start? true :port 4000}}
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
