(ns day8.re-frame.trace.localstorage
  (:require [goog.storage.Storage :as Storage]
            [goog.storage.mechanism.HTML5LocalStorage :as html5localstore]))

(defn- storage []
  (let [mech (goog.storage.mechanism.HTML5LocalStorage.)
        store (goog.storage.Storage. mech)]
    store))

(defn get! [key]
  (let [store (storage)]
    (.get store key)))

(defn set! [key val]
  (let [store (storage)]
    (.set store key val)))
