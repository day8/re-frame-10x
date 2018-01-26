(defproject day8.re-frame/trace "0.1.16"
  :description "Tracing and developer tools for re-frame apps"
  :url "https://github.com/Day8/re-frame-trace"
  :license {:name "MIT"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.671"]
                 [reagent "0.8.0-alpha2" :scope "provided"]
                 [re-frame "0.10.3" :scope "provided"]
                 [binaryage/devtools "0.9.4"]
                 [garden "1.3.3"]
                 [cljsjs/react-flip-move "2.9.17-0"]]
  :plugins [[thomasa/mranderson "0.4.7"]
            [lein-less "RELEASE"]]
  :deploy-repositories {"releases"  :clojars
                        "snapshots" :clojars}

  ;:source-paths ["target/srcdeps"]

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
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
             :mranderson {:dependencies ^:replace [^:source-dep [re-frame "0.10.2"
                                                                 :exclusions [org.clojure/clojurescript
                                                                              cljsjs/react
                                                                              cljsjs/react-dom
                                                                              cljsjs/react-dom-server
                                                                              cljsjs/create-react-class
                                                                              org.clojure/tools.logging
                                                                              net.cgrand/macrovich]]
                                                   ^:source-dep [reagent "0.8.0-alpha2"
                                                                 :exclusions [org.clojure/clojurescript
                                                                              cljsjs/react
                                                                              cljsjs/react-dom
                                                                              cljsjs/react-dom-server
                                                                              cljsjs/create-react-class
                                                                              org.clojure/tools.logging
                                                                              net.cgrand/macrovich]]]}})
