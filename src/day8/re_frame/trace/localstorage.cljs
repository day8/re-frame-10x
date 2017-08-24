(ns day8.re-frame.trace.localstorage
  (:require [goog.storage.Storage :as Storage]
            [goog.storage.mechanism.HTML5LocalStorage :as html5localstore]
            [cljs.reader :as reader])
  (:refer-clojure :exclude [get]))

(def storage (goog.storage.Storage. (goog.storage.mechanism.HTML5LocalStorage.)))

(defn- safe-key [key]
  "Adds a unique prefix to keys to ensure they don't collide with the host application"
  (str "day8.re-frame.trace." key))

(defn get
  "Gets a re-frame-trace value from local storage."
  ([key]
   (get key nil))
  ([key not-found]
   (let [value (.get storage (safe-key key))]
     (if (undefined? value)
       not-found
       (reader/read-string value)))))

(defn save!
  "Saves a re-frame-trace value to local storage."
  [key value]
  (.set storage (safe-key key) (pr-str value)))
