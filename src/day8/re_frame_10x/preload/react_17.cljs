(ns day8.re-frame-10x.preload.react-17
  "Use this namespace with the :preloads compiler option to perform the necessary setup for enabling
   re-frame-10x; e.g.

       {:compiler {:preloads [day8.re-frame-10x.preload] ...}}"
  (:require
   ["react" :as react]
   [clojure.string :as str]
   [day8.re-frame-10x                                            :as re-frame-10x]
   [day8.re-frame-10x.inlined-deps.reagent.v1v2v0.reagent.dom    :as rdom]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core :as rf]
   [day8.re-frame-10x.events                                     :as events]))

(let [react-major-version (first (str/split react/version #"\."))]
  (when-not (= "17" react-major-version)
    (js/console.warn "Re-frame-10x expects React 17, but React"
                     react-major-version
                     "is loaded. This causes deprecation warnings."
                     (when (= "18" react-major-version)
                       "To fix this, try declaring `day8.re-frame-10x.preload.react-18` in your shadow-cljs.edn (instead of `day8.re-frame-10x.preload`). See https://github.com/day8/re-frame-10x/#compatibility-matrix"))))

(re-frame-10x/patch!)

(rf/dispatch-sync [::events/init {:debug? re-frame-10x/debug?}])

(rf/clear-subscription-cache!)

(def shadow-root (re-frame-10x/create-shadow-root))

(rdom/render (re-frame-10x/create-style-container shadow-root)
             shadow-root)
