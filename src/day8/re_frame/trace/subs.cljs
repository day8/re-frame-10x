(ns day8.re-frame.trace.subs
  (:require [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]))

(rf/reg-sub
  :settings/root
  (fn [db _]
    (get db :settings)))

(rf/reg-sub
  :settings/panel-width%
  :<- [:settings/root]
  (fn [settings _]
    (get settings :panel-width%)))

(rf/reg-sub
  :settings/show-panel?
  :<- [:settings/root]
  (fn [settings _]
    (get settings :show-panel?)))

(rf/reg-sub
  :settings/selected-tab
  :<- [:settings/root]
  (fn [settings _]
    (get settings :selected-tab)))

(rf/reg-sub
  :app-db/root
  (fn [db _]
    (get db :app-db)))

(rf/reg-sub
  :app-db/paths
  :<- [:app-db/root]
  (fn [app-db-settings _]
    (get app-db-settings :paths)))

(rf/reg-sub
  :app-db/search-string
  :<- [:app-db/root]
  (fn [app-db-settings _]
    (get app-db-settings :search-string)))

(rf/reg-sub
  :traces/filter-items
  (fn [db _]
    (get-in db [:traces :filter-items])))

(rf/reg-sub
  :global/unloading?
  (fn [db _]
    (get-in db [:global :unloading?])))
