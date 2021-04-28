(ns day8.re-frame-10x.traces.events
  (:require
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.utils.localstorage :as localstorage]
    [clojure.string :as string]
    [clojure.set :as set]))

(rf/reg-event-db
  ::set-draft-query-type
  [(rf/path [:traces :draft-query-type]) rf/trim-v]
  (fn [_ [draft-query-type]]
    draft-query-type))

(rf/reg-event-db
  ::set-draft-query
  [(rf/path [:traces :draft-query]) rf/trim-v]
  (fn [_ [draft-query] _]
    draft-query))

(rf/reg-event-fx
  ::save-draft-query
  [(rf/path [:traces])]
  (fn [{:keys [db]} _]
    (let [{:keys [draft-query-type draft-query]
           :or   {draft-query-type :contains}} db]
      (if (and (= draft-query-type :slower-than)
               (js/isNaN (js/parseFloat draft-query)))
        {:db (assoc db :draft-query-error true)}
        {:db (-> db
                 (assoc :draft-query-error false)
                 (assoc :draft-query ""))
         :dispatch [::add-query
                    {:type  draft-query-type
                     :query draft-query}]}))))

(rf/reg-event-db
  ::set-queries
  [(rf/path [:traces :queries]) rf/trim-v (localstorage/after "filter-items")]
  (fn [_ [filters]]
    filters))

(rf/reg-event-db
  ::add-query
  [(rf/path [:traces :queries]) rf/unwrap (localstorage/after "filter-items")]
  (fn [filters {:keys [query type]}]
    (if (some #(= query (:query %)) filters)
      filters
      (let [filters (if (= :slower-than type)
                      (remove #(= :slower-than (:type %)) filters)
                      filters)]
        (conj filters
              {:id    (random-uuid)
               :query (if (= type :contains)
                        (string/lower-case query)
                        (js/parseFloat query))
               :type  type})))))

(rf/reg-event-db
  ::remove-query
  [(rf/path [:traces :queries]) rf/unwrap (localstorage/after "filter-items")]
  (fn [filters {:keys [id]}]
    (remove #(= (:id %) id) filters)))

(rf/reg-event-db
  ::reset-queries
  [(rf/path [:traces :queries]) (localstorage/after "filter-items")]
  (fn [_ _]
    []))

(rf/reg-event-db
  ::toggle-categories
  [(rf/path [:traces :categories]) rf/trim-v (localstorage/after "categories")]
  (fn [old [new]]
    (if (set/superset? old new)
      (set/difference old new)
      (set/union old new))))

(rf/reg-event-db
  ::set-categories
  [(rf/path [:traces :categories]) rf/trim-v (localstorage/after "categories")]
  (fn [_ [categories]]
    categories))

(rf/reg-event-db
  ::set-filter-by-selected-epoch?
  [(rf/path [:traces :filter-by-selected-epoch?]) rf/trim-v (localstorage/after "show-epoch-traces?")]
  (fn [_ [filter-by-selected-epoch?]]
    filter-by-selected-epoch?))

(rf/reg-event-db
  ::toggle-expansions
  [(rf/path [:traces :expansions])]
  (fn [expansions _]
    (-> expansions
        (assoc :overrides {})
        (update :show-all? not))))

(rf/reg-event-db
  ::toggle-expansion
  [(rf/path [:traces :expansions]) rf/trim-v]
  (fn [expansions [id]]
    (let [showing? (get-in expansions [:overrides id] (:show-all? expansions))]
      (update-in expansions [:overrides id] #(if showing? false (not %))))))