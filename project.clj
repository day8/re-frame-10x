(defproject    day8.re-frame/re-frame-10x "lein-git-inject/version"
  :description "Become 10x more productive when developing and debugging re-frame applications."
  :url         "https://github.com/day8/re-frame-10x"
  :license     {:name "MIT"}

  :min-lein-version "2.9.1"

  :dependencies [[org.clojure/clojure "1.10.1" :scope "provided"]
                 [org.clojure/clojurescript "1.10.597" :scope "provided"]
                 [reagent "0.10.0" :scope "provided"]
                 [re-frame "0.11.0" :scope "provided"]
                 [binaryage/devtools "1.0.0"]
                 [com.yahoo.platform.yui/yuicompressor "2.4.8" :exclusions [rhino/js]]
                 [zprint "0.5.1"]
                 [cljsjs/react-highlight "1.0.7-2" :exclusions [cljsjs/react]]]

  :plugins      [[day8/lein-git-inject "0.0.11"]
                 [thomasa/mranderson   "0.5.1"]
                 [lein-less            "RELEASE"]]

  :middleware   [leiningen.git-inject/middleware]

  :git-inject   {:version-pattern #"(\d+\.\d+\.\d+)"}

  :deploy-repositories [["clojars" {:sign-releases false
                                    :url           "https://clojars.org/repo"
                                    :username      :env/CLOJARS_USERNAME
                                    :password      :env/CLOJARS_PASSWORD}]]

  :source-paths ["src" "gen-src"]

  :release-tasks [["deploy" "clojars"]]

  :profiles {:dev        {:dependencies [[binaryage/dirac "RELEASE"]]}
             :mranderson {:mranderson {:project-prefix "day8.re-frame-10x.inlined-deps"}
                          :dependencies ^:replace [^:source-dep [re-frame "0.11.0"
                                                                 :exclusions [reagent
                                                                              net.cgrand/macrovich
                                                                              org.clojure/tools.logging]]
                                                   ^:source-dep [reagent "0.9.1"
                                                                 :exclusions [cljsjs/react
                                                                              cljsjs/react-dom
                                                                              cljsjs/react-dom-server]]
                                                   ; We need a source-dep on Garden, as there are breaking changes between
                                                   ; versions, and consuming projects can override this version of Garden.
                                                   ^:source-dep [garden "1.3.9"
                                                                 :exclusions [com.yahoo.platform.yui/yuicompressor]]]}})
