(ns day8.re-frame-10x.panels.app-db.events
  (:require
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.fx.local-storage :as local-storage]
    [clojure.string :as string]
    [day8.re-frame-10x.tools.reader.edn :as reader.edn]))

(def paths-interceptors
  [(rf/path [:app-db :paths]) rf/trim-v (local-storage/after "app-db-paths")])

;; The core idea with :app-db/update-path and :app-db/update-path-blur
;; is that we need to separate the users text input (`path-str`) with the
;; parsing of that string (`path`). We let the user type any string that
;; they like, and check it for validity on each change. If it is valid
;; then we update `path` and mark the pod as valid. If it isn't valid then
;; we don't update `path` and mark the pod as invalid.
;;
;; On blur of the input, we reset path-str to the last valid path, if
;; the pod isn't currently valid.


(rf/reg-event-db
  ::create-path
  paths-interceptors
  (fn [paths _]
    (assoc paths
      (js/Date.now)
      {:diff?       false
       :open?       true
       :path        nil
       :path-str    ""
       :valid-path? true})))

(rf/reg-event-db
  :app-db/update-path
  paths-interceptors
  (fn [paths [path-id path-str]]
    (let [path  (reader.edn/read-string-maybe path-str)
          paths (assoc-in paths [path-id :path-str] path-str)]
      (if (or (and (some? path)
                   (sequential? path))
              (string/blank? path-str))
        (-> paths
            (assoc-in [path-id :path] path)
            (assoc-in [path-id :valid-path?] true))
        (assoc-in paths [path-id :valid-path?] false)))))

;; [IJ] TODO: This doesn't appear to be used anywhere:
(rf/reg-event-db
  ::set-search-string
  [(rf/path [:app-db :search-string]) rf/trim-v]
  (fn [_ [search-string]]
    search-string))

(rf/reg-event-db
  ::set-json-ml-paths
  [(rf/path [:app-db :json-ml-expansions]) rf/trim-v (local-storage/after "app-db-json-ml-expansions")]
  (fn [_ [paths]]
    paths))

(rf/reg-event-db
  ::toggle-expansion
  [(rf/path [:app-db :json-ml-expansions]) rf/trim-v (local-storage/after "app-db-json-ml-expansions")]
  (fn [paths [path]]
    (if (contains? paths path)
      (disj paths path)
      (conj paths path))))

(rf/reg-event-db
  ::reagent-id
  [(rf/path [:app-db :reagent-id])]
  (fn [_ _]
    (re-frame.interop/reagent-id re-frame.db/app-db)))