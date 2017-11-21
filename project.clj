(defproject day8.re-frame/trace "0.1.13-SNAPSHOT"
  :description "Tracing and developer tools for re-frame apps"
  :url         "https://github.com/Day8/re-frame-trace"
  :license     {:name "MIT"}
  :dependencies [[org.clojure/clojure        "1.8.0"]
                 [org.clojure/clojurescript "1.9.671"]
                 [reagent                    "0.6.0" :scope "provided"]
                 [re-frame "0.10.2" :scope "provided"]
                 [cljsjs/d3                  "4.3.0-5"]
                 [binaryage/devtools         "0.9.4"]]
  :plugins [[lein-less "1.7.5"]
            [thomasa/mranderson "0.4.7"]]
  :deploy-repositories {"releases" :clojars
                        "snapshots" :clojars}

  ;:source-paths ["target/srcdeps"]

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

  :profiles {:dev        {:dependencies [[binaryage/dirac "RELEASE"]]}
             :mranderson {:dependencies [^:source-dep [re-frame "0.10.2" :scope "provided"
                                                       :exclusions [org.clojure/clojurescript
                                                                    reagent
                                                                    cljsjs/react
                                                                    cljsjs/react-dom
                                                                    cljsjs/react-dom-server
                                                                    org.clojure/tools.logging
                                                                    net.cgrand/macrovich]]]}})
