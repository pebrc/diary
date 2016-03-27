(require '[diary.core :as diary])

(diary/start diary/default-config)
(println (str "Started diary server using " diary/default-config))


(.addShutdownHook (Runtim/getRuntime)
                  (Thread. #(do (diary/stop)
                                (println "Diary server stopping"))))

;; lein trampoline run -m clojure.main script/server.clj
