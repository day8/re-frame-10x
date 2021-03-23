(defproject todomvc-re-frame "lein-git-inject/version"
  :dependencies [[org.clojure/clojure                  "1.10.3"]
                 [org.clojure/clojurescript            "1.10.773"
                  :exclusions [com.google.javascript/closure-compiler-unshaded
                               org.clojure/google-closure-library
                               org.clojure/google-closure-library-third-party]]
                 [thheller/shadow-cljs                 "2.11.24"]
                 [reagent                              "1.0.0"]
                 [re-frame                             "1.2.0"]
                 [day8.re-frame/tracing                "0.6.0"]
                 [com.yahoo.platform.yui/yuicompressor "2.4.8"
                  :exclusions [rhino/js]]
                 [zprint                               "1.0.1"]
                 [superstructor/re-highlight           "0.0.1"]
                 [secretary                            "1.2.3"]]

  :plugins      [[day8/lein-git-inject "0.0.14"]
                 [lein-shadow          "0.3.1"]
                 [lein-ancient         "0.6.15"]]

  :middleware   [leiningen.git-inject/middleware]

  :profiles {:dev  {:dependencies [[binaryage/devtools "1.0.2"]]}}

  :source-paths ["src" "../../src" "../../gen-src"]

  :clean-targets ^{:protect false} [:target-path
                                    "shadow-cljs.edn"
                                    "node_modules"
                                    "resources/public/js"]

  :shadow-cljs {:nrepl  {:port 8777}

                :builds {:app {:target     :browser
                               :output-dir "resources/public/js"
                                 :modules    {:todomvc {:init-fn  todomvc.core/main
                                                        :preloads [day8.re-frame-10x.preload]}}
                               :dev        {:compiler-options {:closure-defines {re-frame.trace.trace-enabled?        true
                                                                                 day8.re-frame-10x.debug?             true
                                                                                 day8.re-frame.tracing.trace-enabled? true}
                                                               :external-config  {:devtools/config {:features-to-install [:formatters :hints]}}}}
                               :devtools   {:http-root "resources/public"
                                            :http-port 8280}}}}

  :aliases {"watch" ["with-profile" "dev" "shadow" "watch" "app"]})
