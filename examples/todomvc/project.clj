(defproject todomvc-re-frame "0.10.5"
  :dependencies [[org.clojure/clojure        "1.10.0"]
                 [org.clojure/clojurescript  "1.10.439"]
                 [reagent "0.8.1"]
                 [re-frame "0.10.5"]
                 [day8.re-frame/tracing "0.5.1"]
                 [day8.re-frame/re-frame-10x "0.3.8-SNAPSHOT"]
                 [secretary "1.2.3"]]


  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-figwheel  "0.5.18"]]

  :hooks [leiningen.cljsbuild]

  :profiles {:dev  {:dependencies [[binaryage/devtools "0.9.10"]]
                    :cljsbuild
                    {:builds {:client {:compiler {:asset-path           "js"
                                                  :optimizations        :none
                                                  :source-map           true
                                                  :closure-defines {"re_frame.trace.trace_enabled_QMARK_" true
                                                                    "day8.re_frame_10x.debug_QMARK_" true
                                                                    "day8.re_frame.tracing.trace_enabled_QMARK_" true}
                                                  :preloads             [day8.re-frame-10x.preload]
                                                  :source-map-timestamp true
                                                  :main                 "todomvc.core"}
                                       :figwheel {:on-jsload "todomvc.core/main"}}}}}

             :prod {:cljsbuild
                    {:builds {:client {:compiler {:optimizations :advanced
                                                  :elide-asserts true
                                                  :pretty-print  false}}}}}}

  :figwheel {:server-port 3450
             :repl        false}


  :clean-targets ^{:protect false} ["resources/public/js" "target"]

  :cljsbuild {:builds {:client {:source-paths ["src" "../../src" "../../gen-src"]
                                :compiler     {:output-dir "resources/public/js"
                                               :output-to  "resources/public/js/client.js"}}}})
