(ns day8.re-frame.trace.events
  (:require [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [day8.re-frame.trace.utils.localstorage :as localstorage]))

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

(rf/reg-event-db
  :traces/filter-items
  (fn [db [_ filter-items]]
    (assoc-in db [:traces :filter-items] filter-items)))

(rf/reg-event-db
  :app-db/paths
  (fn [db [_ paths]]
    (assoc-in db [:app-db :paths] paths)))
