(defproject day8.re-frame/trace "0.0.1-SNAPSHOT"
  :description "Tracing and developer tools for re-frame apps"
  :url         "https://github.com/Day8/re-frame-trace"
  :license     {:name "MIT"}
  :dependencies [[org.clojure/clojure        "1.8.0"]
                 [org.clojure/clojurescript  "1.9.227"]
                 [reagent                    "0.6.0"]
                 [re-frame                   "0.8.1-SNAPSHOT"]
                 [cljsjs/d3 "4.2.2-0"]]
  :profiles {:dev {:dependencies [[binaryage/dirac "RELEASE"]]}})
