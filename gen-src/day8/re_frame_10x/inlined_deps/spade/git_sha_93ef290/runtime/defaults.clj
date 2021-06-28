(ns day8.re-frame-10x.inlined-deps.spade.git-sha-93ef290.runtime.defaults
  (:require [day8.re-frame-10x.inlined-deps.spade.git-sha-93ef290.container.atom :refer [->AtomStyleContainer]]))

(defonce shared-styles-atom (atom nil))

(defn create-container []
  (->AtomStyleContainer shared-styles-atom))
