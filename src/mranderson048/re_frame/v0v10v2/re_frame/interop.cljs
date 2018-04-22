(ns mranderson048.re-frame.v0v10v2.re-frame.interop
  (:require [goog.async.nextTick]
            [mranderson048.reagent.v0v7v0.reagent.core]
            [mranderson048.reagent.v0v7v0.reagent.ratom]))

(def next-tick goog.async.nextTick)

(def empty-queue #queue [])

(def after-render mranderson048.reagent.v0v7v0.reagent.core/after-render)

;; Make sure the Google Closure compiler sees this as a boolean constant,
;; otherwise Dead Code Elimination won't happen in `:advanced` builds.
;; Type hints have been liberally sprinkled.
;; https://developers.google.com/closure/compiler/docs/js-for-compiler
(def ^boolean debug-enabled? "@define {boolean}" ^boolean js/goog.DEBUG)

(defn ratom [x]
  (mranderson048.reagent.v0v7v0.reagent.core/atom x))

(defn ratom? [x]
  (satisfies? mranderson048.reagent.v0v7v0.reagent.ratom/IReactiveAtom x))

(defn deref? [x]
  (satisfies? IDeref x))


(defn make-reaction [f]
  (mranderson048.reagent.v0v7v0.reagent.ratom/make-reaction f))

(defn add-on-dispose! [a-ratom f]
  (mranderson048.reagent.v0v7v0.reagent.ratom/add-on-dispose! a-ratom f))

(defn dispose! [a-ratom]
	(mranderson048.reagent.v0v7v0.reagent.ratom/dispose! a-ratom))

(defn set-timeout! [f ms]
  (js/setTimeout f ms))

(defn now []
  (if (exists? js/performance.now)
    (js/performance.now)
    (js/Date.now)))

(defn reagent-id
  "Produces an id for reactive Reagent values
  e.g. reactions, ratoms, cursors."
  [reactive-val]
  (when (implements? mranderson048.reagent.v0v7v0.reagent.ratom/IReactiveAtom reactive-val)
    (str (condp instance? reactive-val
           mranderson048.reagent.v0v7v0.reagent.ratom/RAtom "ra"
           mranderson048.reagent.v0v7v0.reagent.ratom/RCursor "rc"
           mranderson048.reagent.v0v7v0.reagent.ratom/Reaction "rx"
           mranderson048.reagent.v0v7v0.reagent.ratom/Track "tr"
           "other")
         (hash reactive-val))))
