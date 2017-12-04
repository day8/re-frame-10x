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

;; App DB

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
  :app-db/expansions
  :<- [:app-db/root]
  (fn [app-db-settings _]
    (get app-db-settings :json-ml-expansions)))

(rf/reg-sub
  :app-db/node-expanded?
  :<- [:app-db/expansions]
  (fn [expansions [_ path]]
    (contains? expansions path)))

;;

(rf/reg-sub
  :traces/filter-items
  (fn [db _]
    (get-in db [:traces :filter-items])))

(rf/reg-sub
  :global/unloading?
  (fn [db _]
    (get-in db [:global :unloading?])))

;;

(rf/reg-sub
  :snapshot/snapshot-root
  (fn [db _]
    (:snapshot db)))

(rf/reg-sub
  :snapshot/snapshot-ready?
  :<- [:snapshot/snapshot-root]
  (fn [snapshot _]
    (contains? snapshot :current-snapshot)))
