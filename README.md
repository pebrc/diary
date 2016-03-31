# diary

purely educational. based on the om.next todomvc example.

#dev mode
``` 
lein repl ;; or
M-x cider-jack-in
```

then evaluate `dev/figwheel.clj`
and use `(start)`, `(stop)`, `(reload)` or `(repl)` as needed.

#'prod' mode
```
lein trampoline run -m clojure.main script/server.clj
```
