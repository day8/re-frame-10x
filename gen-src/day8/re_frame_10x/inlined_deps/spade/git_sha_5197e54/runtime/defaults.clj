(ns day8.re-frame-10x.inlined-deps.spade.git-sha-5197e54.runtime.defaults
  (:require [day8.re-frame-10x.inlined-deps.spade.git-sha-5197e54.container.atom :as atom-container]))

(defonce shared-styles-atom (atom nil))
(defonce shared-styles-info-atom (atom nil))

(defn create-container []
  (atom-container/create-container shared-styles-atom shared-styles-info-atom))
