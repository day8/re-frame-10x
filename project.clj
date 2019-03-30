(defproject day8.re-frame/re-frame-10x "0.4.0"
  :description "Become 10x more productive when developing and debugging re-frame applications."
  :url "https://github.com/Day8/re-frame-10x"
  :license {:name "MIT"}
  :min-lein-version "2.9.1"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.238"]
                 [reagent "0.8.0" :scope "provided"]
                 [re-frame "0.10.6" :scope "provided"]
                 [binaryage/devtools "0.9.10"]
                 [com.yahoo.platform.yui/yuicompressor "2.4.8" :exclusions [rhino/js]]
                 [zprint "0.4.15"]
                 [cljsjs/react-flip-move "3.0.1-1"]
                 [cljsjs/react-highlight "1.0.7-2" :exclusions [cljsjs/react]]
                 [cljsjs/create-react-class "15.6.2-0" :exclusions [cljsjs/react]]
                 ;[expound "0.4.0"]
                 ]
  :plugins [[thomasa/mranderson "0.5.1-20190329.105340-3"]
            [lein-less "RELEASE"]]
  :deploy-repositories {"releases"  {:sign-releases false :url "https://clojars.org/repo"}
                        "snapshots" {:sign-releases false :url "https://clojars.org/repo"}}

  :source-paths ["src" "gen-src"]

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "--no-sign"]
                  ["deploy"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]]

  :profiles {:dev        {:dependencies [[binaryage/dirac "RELEASE"]]}
             :mranderson {:mranderson {:project-prefix "day8.re-frame-10x.inlined-deps"}
                          :dependencies ^:replace [^:source-dep [re-frame "0.10.6"
                                                                 :exclusions [org.clojure/clojurescript
                                                                              cljsjs/react
                                                                              cljsjs/react-dom
                                                                              cljsjs/react-dom-server
                                                                              cljsjs/create-react-class
                                                                              org.clojure/tools.logging
                                                                              net.cgrand/macrovich]]
                                                   ^:source-dep [reagent "0.8.0"
                                                                 :exclusions [org.clojure/clojurescript
                                                                              cljsjs/react
                                                                              cljsjs/react-dom
                                                                              cljsjs/react-dom-server
                                                                              cljsjs/create-react-class
                                                                              org.clojure/tools.logging
                                                                              net.cgrand/macrovich]]
                                                   ; We need a source-dep on Garden, as there are breaking changes between
                                                   ; versions, and consuming projects can override this version of Garden.
                                                   ^:source-dep [garden "1.3.3"
                                                                 :exclusions [com.yahoo.platform.yui/yuicompressor]]]}})
