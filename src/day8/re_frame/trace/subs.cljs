(ns day8.re-frame.trace.subs
  (:require [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [re-frame.db]
            [clojure.string :as str]))

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
  :app-db/re-frame-db
  (fn []
    [])
  (fn [_ _]
    @re-frame.db/app-db))

(rf/reg-sub
  :app-db/autocomplete-keys
  :<- [:app-db/root]
  :<- [:app-db/re-frame-db]
  (fn [[app-db-settings re-frame-db] _]
    (let [search-string (:search-string app-db-settings)]
      (take 20
            (try
              (if (str/blank? search-string)
                (keys re-frame-db)
                (let [path (try
                             (cljs.reader/read-string (str "[" search-string "]"))
                             (catch :default e
                               nil))]
                  (when (some? path)
                    (keys (get-in re-frame-db path)))))
              (catch :default e
                nil))))))

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
