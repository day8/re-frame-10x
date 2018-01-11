(ns day8.re-frame.trace.subs
  (:require [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [day8.re-frame.trace.metamorphic :as metam]))

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
    (if (:showing-settings? settings)
      :settings
      (get settings :selected-tab))))

(rf/reg-sub
  :settings/paused?
  :<- [:settings/root]
  (fn [settings _]
    (:paused? settings)))

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
  :traces/trace-root
  (fn [db _]
    (:traces db)))

(rf/reg-sub
  :traces/filter-items
  (fn [db _]
    (get-in db [:traces :filter-items])))

(rf/reg-sub
  :traces/expansions
  (fn [db _]
    (get-in db [:traces :expansions])))

(rf/reg-sub
  :traces/categories
  (fn [db _]
    (get-in db [:traces :categories])))

(rf/reg-sub
  :traces/all-traces
  :<- [:traces/trace-root]
  (fn [traces _]
    (:all-traces traces)))

(rf/reg-sub
  :traces/number-of-traces
  :<- [:traces/trace-root]
  (fn [traces _]
    (count traces)))

(rf/reg-sub
  :traces/current-event-traces
  :<- [:traces/all-traces]
  :<- [:epochs/beginning-trace-id]
  :<- [:epochs/ending-trace-id]
  (fn [[traces beginning ending] _]
    (filter #(<= beginning (:id %) ending) traces)
    #_traces))

(rf/reg-sub
  :traces/show-epoch-traces?
  :<- [:traces/trace-root]
  (fn [trace-root]
    (:show-epoch-traces? trace-root)))

;;

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

;;

(rf/reg-sub
  :epochs/epoch-root
  (fn [db _]
    (:epochs db)))

(rf/reg-sub
  :epochs/current-event
  :<- [:epochs/epoch-root]
  (fn [epochs _]
    (let [matches (:matches epochs)
          current-index (:current-epoch-index epochs)
          match (nth matches (+ (count matches) (or current-index 0)) (last matches))
          event (get-in (metam/matched-event match) [:tags :event])]
      event)))

(rf/reg-sub
  :epochs/number-of-matches
  :<- [:epochs/epoch-root]
  (fn [epochs _]
    (count (get epochs :matches))))

(rf/reg-sub
  :epochs/current-event-index
  :<- [:epochs/epoch-root]
  (fn [epochs _]
    (:current-epoch-index epochs)))

(rf/reg-sub
  :epochs/event-position
  :<- [:epochs/current-event-index]
  :<- [:epochs/number-of-matches]
  (fn [[current total]]
    (str current " of " total)))

(rf/reg-sub
  :epochs/beginning-trace-id
  :<- [:epochs/epoch-root]
  (fn [epochs]
    ;; TODO: make it use the real match
    (let [match (last (:matches epochs))]
      (:id (first match)))))

(rf/reg-sub
  :epochs/ending-trace-id
  :<- [:epochs/epoch-root]
  (fn [epochs]
    ;; TODO: make it use the real match
    (let [match (last (:matches epochs))]
      (:id (last match)))))

