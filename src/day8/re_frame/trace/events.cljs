(ns day8.re-frame.trace.events
  (:require [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [day8.re-frame.trace.utils.utils :as utils]
            [day8.re-frame.trace.utils.localstorage :as localstorage]
            [clojure.string :as str]))

(rf/reg-event-db
  :settings/panel-width%
  (fn [db [_ width%]]
    (localstorage/save! "panel-width-ratio" (max width% 0.05))
    (assoc-in db [:settings :panel-width%] (max width% 0.05))))

(rf/reg-event-db
  :settings/selected-tab
  (fn [db [_ selected-tab]]
    (localstorage/save! "selected-tab" selected-tab)
    (assoc-in db [:settings :selected-tab] selected-tab)))

(rf/reg-event-db
  :settings/show-panel?
  (fn [db [_ show-panel?]]
    (localstorage/save! "show-panel" show-panel?)
    (assoc-in db [:settings :show-panel?] show-panel?)))

(rf/reg-event-db
  :settings/toggle-panel
  (fn [db _]
    (let [show-panel? (not (get-in db [:settings :show-panel?]))]
      (localstorage/save! "show-panel" show-panel?)
      (assoc-in db [:settings :show-panel?] show-panel?))))

;; Traces

(defn save-filter-items [filter-items]
  (localstorage/save! "filter-items" filter-items))

(rf/reg-event-db
  :traces/filter-items
  (fn [db [_ filter-items]]
    (save-filter-items filter-items)
    (assoc-in db [:traces :filter-items] filter-items)))

(rf/reg-event-db
  :traces/add-filter
  [(rf/path [:traces :filter-items])]
  (fn [filter-items [_ filter-input filter-type]]
    (let [new-db (when-not (some #(= filter-input (:query %)) filter-items) ;; prevent duplicate filter strings
                   ;; if existing, remove prior filter for :slower-than
                   ;; TODO: rework how time filters are used.
                   (when (and (= :slower-than filter-type)
                              (some #(= filter-type (:filter-type %)) filter-items))
                     (remove #(= :slower-than (:filter-type %)) filter-items))
                   ;; add new filter
                   (conj filter-items {:id          (random-uuid)
                                       :query       (if (= filter-type :contains)
                                                      (str/lower-case filter-input)
                                                      (js/parseFloat filter-input))
                                       :filter-type filter-type}))]
      (save-filter-items new-db)
      new-db)))

(rf/reg-event-db
  :traces/remove-filter
  [(rf/path [:traces :filter-items])]
  (fn [filter-items [_ filter-id]]
    (let [new-db (remove #(= (:id %) filter-id) filter-items)]
      (save-filter-items new-db)
      new-db)))

(rf/reg-event-db
  :traces/reset-filter-items
  (fn [db _]
    (let [new-db (utils/dissoc-in db [:traces :filter-items])]
      (save-filter-items (get-in db :traces :filter-items))
      new-db)))

;; App DB

(rf/reg-event-db
  :app-db/paths
  (fn [db [_ paths]]
    (localstorage/save! "app-db-paths" paths)
    (assoc-in db [:app-db :paths] paths)))
