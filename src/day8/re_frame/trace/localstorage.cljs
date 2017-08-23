(ns day8.re-frame.trace.localstorage
  (:require [goog.storage.Storage :as Storage]
            [goog.storage.mechanism.HTML5LocalStorage :as html5localstore]
            [cljs.reader :as reader]))

(def mech (goog.storage.mechanism.HTML5LocalStorage.))

(defn- storage []
  (goog.storage.Storage. mech))

(defn load [key]
  (when-let [value (.get (storage) key)]
    (cljs.reader/read-string value)))

(defn save! [key value]
  (let [store (storage)]
    (.set store key (pr-str value))))
