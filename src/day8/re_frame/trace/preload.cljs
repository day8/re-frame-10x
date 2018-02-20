(ns day8.re-frame.trace.preload
  (:require [day8.re-frame.trace :as trace]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]))


;; Use this namespace with the :preloads compiler option to perform the necessary setup for enabling tracing:
;; {:compiler {:preloads [day8.re-frame.trace.preload] ...}}
(js/console.warn "re-frame-trace has been renamed to re-frame-10x: (https://clojars.org/day8.re-frame/re-frame-10x). Update to newer versions of this library by using the `day8.re-frame/re-frame-10x` artifact ID. Thanks!")
(rf/clear-subscription-cache!)
(trace/init-db!)
(defonce _ (trace/init-tracing!))
(trace/inject-devtools!)
