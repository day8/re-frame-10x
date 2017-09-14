(defproject day8.re-frame/trace "0.1.7-SNAPSHOT"
  :description "Tracing and developer tools for re-frame apps"
  :url         "https://github.com/Day8/re-frame-trace"
  :license     {:name "MIT"}
  :dependencies [[org.clojure/clojure        "1.8.0"]
                 [org.clojure/clojurescript  "1.9.227"]
                 [reagent                    "0.6.0"]
                 [re-frame                   "0.9.0"]
                 [cljsjs/d3                  "4.2.2-0"]
                 [binaryage/devtools         "0.9.4"]]
  :plugins [[lein-less "1.7.5"]]
  :deploy-repositories {"releases" :clojars
                        "snapshots" :clojars}

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["less" "once"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]]

  :figwheel {:css-dirs ["resources/day8/re_frame/trace"]}

  :less {:source-paths ["resources/day8/re_frame/trace"]
         :target-path  "resources/day8/re_frame/trace"}

  :profiles {:dev {:dependencies [[binaryage/dirac "RELEASE"]]}})
