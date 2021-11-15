(defproject    day8.re-frame/re-frame-10x "lein-git-inject/version"
  :description "Become 10x more productive when developing and debugging re-frame applications."
  :url         "https://github.com/day8/re-frame-10x"
  :license     {:name "MIT"}

  :min-lein-version "2.9.1"

  :dependencies [[org.clojure/clojure                  "1.10.3"   :scope "provided"]
                 [org.clojure/clojurescript            "1.10.879" :scope "provided"]
                 [binaryage/devtools                   "1.0.4"]
                 [com.yahoo.platform.yui/yuicompressor "2.4.8"
                  :exclusions [rhino/js]]
                 [zprint                               "1.0.1"]
                 [superstructor/re-highlight           "1.1.0"]
                 ;; re-highlight only has a transitive dependency on highlight.js for
                 ;; shadow-cljs builds, so we need to declare a dependency on cljsjs/highlight
                 ;; for 10x to support other build systems.
                 [cljsjs/highlight                     "10.3.1-0"]]

  :plugins      [[day8/lein-git-inject "0.0.15"]
                 [lein-less            "RELEASE"]]

  :middleware   [leiningen.git-inject/middleware]

  :git-inject   {:version-pattern #"(\d+\.\d+\.\d+.*)"}

  :deploy-repositories [["clojars" {:sign-releases false
                                    :url           "https://clojars.org/repo"
                                    :username      :env/CLOJARS_USERNAME
                                    :password      :env/CLOJARS_TOKEN}]]

  :source-paths ["src" "gen-src"]

  :release-tasks [["deploy" "clojars"]]

  :profiles {:dev        {:dependencies [[binaryage/dirac "RELEASE"]
                                         [metosin/malli   "0.5.1"]
                                         [clj-kondo       "RELEASE"]]
                          :plugins      [[com.github.liquidz/antq "RELEASE"]
                                         [thomasa/mranderson      "0.5.3"]
                                         [lein-count              "1.0.9"]
                                         [lein-pprint             "1.3.2"]]
                          :antq         {}
                          :aliases      {"lint" ["run" "-m" "clj-kondo.main"
                                                 "--lint"
                                                 "src"  ; ~#(clojure.string/join ";" (leiningen.core.classpath/get-classpath %))
                                                 "--config"
                                                 ".clj-kondo/config.edn"
                                                 "--parallel"
                                                 "--copy-configs"]}}
             :mranderson {:mranderson {:project-prefix "day8.re-frame-10x.inlined-deps"}
                          :dependencies ^:replace [^:source-dep [re-frame "1.1.2"
                                                                 :exclusions [reagent
                                                                              net.cgrand/macrovich
                                                                              org.clojure/tools.logging]]
                                                   ^:source-dep [reagent "1.0.0"
                                                                 :exclusions [cljsjs/react
                                                                              cljsjs/react-dom
                                                                              cljsjs/react-dom-server]]
                                                   ; We need a source-dep on Garden, as there are breaking changes between
                                                   ; versions, and consuming projects can override this version of Garden.
                                                   ^:source-dep [garden "1.3.10"
                                                                 :exclusions [com.yahoo.platform.yui/yuicompressor]]
                                                   ^:source-dep [net.dhleong/spade "1.1.0"
                                                                 :exclusions [org.clojure/clojure
                                                                              org.clojure/clojurescript
                                                                              garden]]]}})
