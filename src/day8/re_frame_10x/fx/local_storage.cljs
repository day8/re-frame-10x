(ns day8.re-frame-10x.fx.local-storage
  (:require
    [goog.storage.Storage]
    [goog.storage.mechanism.HTML5LocalStorage]
    [cljs.reader                                                  :as reader]
    [clojure.string                                               :as string]
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]))

(def storage (goog.storage.Storage. (goog.storage.mechanism.HTML5LocalStorage.)))

(def safe-prefix "day8.re-frame-10x.")

(defn- safe-key [key]
  "Adds a unique prefix to local storage keys to ensure they don't collide with the host application"
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

(defn delete-all-keys!
  "Deletes all re-frame-10x config keys"
  []
  (doseq [k (js/Object.keys js/localStorage)]
    (when (string/starts-with? k safe-prefix)
      (.remove storage k))))

(defn save
  ([key]
   (rf/after
     (fn [db]
       (.set storage (safe-key key) (pr-str db)))))
  ([key & ks]
   (rf/after
     (fn [db]
       (run!
         (fn [k]
           (let [v (if (vector? k) (get-in db k) (get db k))]
             (.set storage (safe-key key) (pr-str v))))
         ks)))))

(rf/reg-cofx
  ::load
  (fn [coeffects {:keys [key or]}]
    (assoc coeffects
      (keyword key)
      (load key or))))