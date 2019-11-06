(defproject day8.re-frame/re-frame-10x "see :git-version below https://github.com/arrdem/lein-git-version"
  :description "Become 10x more productive when developing and debugging re-frame applications."
  :url "https://github.com/day8/re-frame-10x"
  :license {:name "MIT"}

  :min-lein-version "2.9.1"

  :git-version
  {:status-to-version
   (fn [{:keys [tag version branch ahead ahead? dirty?] :as git-status}]
     (if-not (string? tag)
       ;; If git-status is nil (i.e. IntelliJ reading project.clj) then return an empty version.
       "_"
       (if (and (not ahead?) (not dirty?))
         tag
         (let [[_ major minor patch suffix] (re-find #"v?(\d+)\.(\d+)\.(\d+)(-.+)?" tag)]
           (if (nil? major)
             ;; If tag is poorly formatted then return GIT-TAG-INVALID
             "GIT-TAG-INVALID"
             (let [patch' (try (Long/parseLong patch) (catch Throwable _ 0))
                   patch+ (inc patch')]
               (str major "." minor "." patch+ suffix "-" ahead "-SNAPSHOT")))))))}

  :dependencies [[org.clojure/clojure "1.10.1" :scope "provided"]
                 [org.clojure/clojurescript "1.10.520" :scope "provided"]
                 [reagent "0.8.1" :scope "provided"]
                 [re-frame "0.10.9" :scope "provided"]
                 [binaryage/devtools "0.9.10"]
                 [com.yahoo.platform.yui/yuicompressor "2.4.8" :exclusions [rhino/js]]
                 [zprint "0.5.1"]
                 [cljsjs/react-highlight "1.0.7-2" :exclusions [cljsjs/react]]
                 [cljsjs/create-react-class "15.6.3-1" :exclusions [cljsjs/react]]]

  :plugins [[me.arrdem/lein-git-version "2.0.3"]
            [thomasa/mranderson         "0.5.1"]
            [lein-less                  "RELEASE"]]

  :deploy-repositories [["clojars" {:sign-releases false
                                    :url           "https://clojars.org/repo"
                                    :username      :env/CLOJARS_USERNAME
                                    :password      :env/CLOJARS_PASSWORD}]]

  :source-paths ["src" "gen-src"]

  :release-tasks [["deploy" "clojars"]]

  :profiles {:dev        {:dependencies [[binaryage/dirac "RELEASE"]]}
             :mranderson {:mranderson {:project-prefix "day8.re-frame-10x.inlined-deps"}
                          :dependencies ^:replace [^:source-dep [re-frame "0.10.9"
                                                                 :exclusions [org.clojure/clojurescript
                                                                              cljsjs/react
                                                                              cljsjs/react-dom
                                                                              cljsjs/react-dom-server
                                                                              cljsjs/create-react-class
                                                                              org.clojure/tools.logging
                                                                              net.cgrand/macrovich]]
                                                   ^:source-dep [reagent "0.8.1"
                                                                 :exclusions [org.clojure/clojurescript
                                                                              cljsjs/react
                                                                              cljsjs/react-dom
                                                                              cljsjs/react-dom-server
                                                                              cljsjs/create-react-class
                                                                              org.clojure/tools.logging
                                                                              net.cgrand/macrovich]]
                                                   ; We need a source-dep on Garden, as there are breaking changes between
                                                   ; versions, and consuming projects can override this version of Garden.
                                                   ^:source-dep [garden "1.3.9"
                                                                 :exclusions [com.yahoo.platform.yui/yuicompressor]]]}})
