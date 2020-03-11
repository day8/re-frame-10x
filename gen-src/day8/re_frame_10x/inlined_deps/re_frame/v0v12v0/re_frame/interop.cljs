(ns day8.re-frame-10x.inlined-deps.re-frame.v0v12v0.re-frame.interop
  (:require [goog.async.nextTick]
            [day8.re-frame-10x.inlined-deps.reagent.v0v10v0.reagent.core :as reagent]
            [day8.re-frame-10x.inlined-deps.reagent.v0v10v0.reagent.ratom :as ratom]))

(def next-tick goog.async.nextTick)

(def empty-queue #queue [])

(def after-render reagent/after-render)

;; Make sure the Google Closure compiler sees this as a boolean constant,
;; otherwise Dead Code Elimination won't happen in `:advanced` builds.
;; Type hints have been liberally sprinkled.
;; https://developers.google.com/closure/compiler/docs/js-for-compiler
(def ^boolean debug-enabled? "@define {boolean}" ^boolean goog/DEBUG)

(defn ratom [x]
  (reagent/atom x))

(defn ratom? [x]
  (satisfies? reagent.ratom/IReactiveAtom x))

(defn deref? [x]
  (satisfies? IDeref x))


(defn make-reaction [f]
  (ratom/make-reaction f))

(defn add-on-dispose! [a-ratom f]
  (ratom/add-on-dispose! a-ratom f))

(defn dispose! [a-ratom]
  (ratom/dispose! a-ratom))

(defn set-timeout! [f ms]
  (js/setTimeout f ms))

(defn now []
  (if (and
       (exists? js/performance)
       (exists? js/performance.now))
    (js/performance.now)
    (js/Date.now)))

(defn reagent-id
  "Produces an id for reactive Reagent values
  e.g. reactions, ratoms, cursors."
  [reactive-val]
  (when (implements? ratom/IReactiveAtom reactive-val)
    (str (condp instance? reactive-val
           ratom/RAtom "ra"
           ratom/RCursor "rc"
           ratom/Reaction "rx"
           ratom/Track "tr"
           "other")
         (hash reactive-val))))
