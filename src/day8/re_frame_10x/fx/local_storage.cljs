(ns day8.re-frame-10x.fx.local-storage
  (:require
   [goog.storage.Storage]
   [goog.storage.mechanism.HTML5LocalStorage]
   [goog.testing.storage.FakeMechanism]
   [cljs.reader                                                  :as reader]
   [clojure.string                                               :as string]
   [day8.re-frame-10x.tools.datafy                               :as tools.datafy]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core :as rf]))

(def storage-mechanism
  "LocalStorage is not available in sandboxed iframes, so check
  window.localStorage and use the fake storage mechanism if it's not available.
  re-frame-10x settings will not persist, but it will work."
  (try
    (when js/localStorage
      (goog.storage.mechanism.HTML5LocalStorage.))
    (catch js/Error _
      (goog.testing.storage.FakeMechanism.))))

(def storage (goog.storage.Storage. storage-mechanism))

(def safe-prefix "day8.re-frame-10x.")

(defn- safe-key
  "Adds a unique prefix to local storage keys to ensure they don't collide with the host application"
  [key]
  (str safe-prefix key))

(defn load
  "Loads a re-frame-10x value from local storage."
  ([key]
   (load key nil))
  ([key not-found]
   (let [value (.get storage (safe-key key))]
     (if (undefined? value)
       not-found
       (reader/read-string value)))))

(defn- all-keys []
  (try
    (js/Object.keys js/localStorage)
    (catch js/Error _
      [])))

(defn delete-all-keys!
  "Deletes all re-frame-10x config keys"
  []
  (doseq [k (all-keys)]
    (when (string/starts-with? k safe-prefix)
      (.remove storage k))))

(defn save
  ([key]
   (rf/after
    (fn [db]
      (.set storage (safe-key key) (tools.datafy/pr-str-safe db)))))
  ([key & ks]
   (rf/after
    (fn [db]
      (run!
       (fn [k]
         (let [v (if (vector? k) (get-in db k) (get db k))]
           (.set storage (safe-key key) (tools.datafy/pr-str-safe v))))
       ks)))))

(rf/reg-cofx
 ::load
 (fn [coeffects {:keys [key or]}]
   (assoc coeffects
          (keyword key)
          (load key or))))
