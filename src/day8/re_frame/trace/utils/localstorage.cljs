(ns day8.re-frame.trace.utils.localstorage
  (:require [goog.storage.Storage]
            [goog.storage.mechanism.HTML5LocalStorage]
            [cljs.reader :as reader]
            [clojure.string :as str])
  (:refer-clojure :exclude [get]))

(def storage (goog.storage.Storage. (goog.storage.mechanism.HTML5LocalStorage.)))

(def safe-prefix "day8.re-frame.trace.")

(defn- safe-key [key]
  "Adds a unique prefix to local storage keys to ensure they don't collide with the host application"
  (str safe-prefix key))

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

(defn delete-all-keys!
  "Deletes all re-frame-trace config keys"
  []
  (doseq [k (js/Object.keys js/localStorage)]
    (when (str/starts-with? k safe-prefix)
      (.remove storage k))))
