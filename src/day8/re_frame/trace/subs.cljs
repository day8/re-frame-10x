(ns day8.re-frame.trace.subs
  (:require [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [day8.re-frame.trace.metamorphic :as metam]
            [day8.re-frame.trace.utils.utils :as utils]))

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
  :app-db/current-epoch-app-db-after
  :<- [:epochs/current-event-trace]
  (fn [trace _]
    (get-in trace [:tags :app-db-after])))

(rf/reg-sub
  :app-db/current-epoch-app-db-before
  :<- [:epochs/current-event-trace]
  (fn [trace _]
    (get-in trace [:tags :app-db-before])))

(rf/reg-sub
  :app-db/paths
  :<- [:app-db/root]
  (fn [app-db-settings _]
    (map #(assoc (val %) :id (key %))
         (get app-db-settings :paths))))

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
    (into [] (filter #(<= beginning (:id %) ending)) traces)))

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
  :epochs/current-match
  :<- [:epochs/epoch-root]
  (fn [epochs _]
    (let [matches       (:matches epochs)
          current-index (:current-epoch-index epochs)
          match         (nth matches (+ (count matches) (or current-index 0)) (last matches))]
      match)))

(rf/reg-sub
  :epochs/current-event-trace
  :<- [:epochs/current-match]
  (fn [match _]
    (metam/matched-event match)))

(rf/reg-sub
  :epochs/current-event
  :<- [:epochs/current-event-trace]
  (fn [trace _]
    (get-in trace [:tags :event])))

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
  :<- [:epochs/current-match]
  (fn [match]
    (:id (first match))))

(rf/reg-sub
  :epochs/ending-trace-id
  :<- [:epochs/current-match]
  (fn [match]
    (:id (last match))))

(rf/reg-sub
  :epochs/older-epochs-available?
  :<- [:epochs/current-event-index]
  :<- [:epochs/number-of-matches]
  (fn [[current total]]
    (pos? (+ current total -1))))

(rf/reg-sub
  :epochs/newer-epochs-available?
  :<- [:epochs/current-event-index]
  :<- [:epochs/number-of-matches]
  (fn [[current total]]
    (and (not (zero? current))
         (some? current))))

;;

(rf/reg-sub
  :timing/total-epoch-time
  :<- [:traces/current-event-traces]
  (fn [traces]
    (let [start-of-epoch (nth traces 0)
          end-of-epoch   (utils/last-in-vec traces)]
      (metam/elapsed-time start-of-epoch end-of-epoch))))

(rf/reg-sub
  :timing/animation-frame-count
  :<- [:traces/current-event-traces]
  (fn [traces]
    (count (filter metam/request-animation-frame? traces))))

(rf/reg-sub
  :timing/event-processing-time
  :<- [:traces/current-event-traces]
  (fn [traces]
    (let [start-of-epoch (nth traces 0)
          finish-run     (first (filter metam/finish-run? traces))]
      (metam/elapsed-time start-of-epoch finish-run))))

(rf/reg-sub
  :timing/render-time
  :<- [:traces/current-event-traces]
  (fn [traces]
    (let [start-of-render (first (filter metam/request-animation-frame? traces))
          end-of-epoch (utils/last-in-vec traces)]
      (metam/elapsed-time start-of-render end-of-epoch))))

(rf/reg-sub
  :timing/data-available?
  :<- [:traces/current-event-traces]
  (fn [traces]
    (not (empty? traces))))

;;

(rf/reg-sub
  :subs/all-sub-traces
  :<- [:traces/current-event-traces]
  (fn [traces]
    (filter metam/subscription? traces)))

(defn sub-sort-val
  [sub]
  (case (:type sub)
    :created 1
    :re-run 2
    :destroyed 3
    :not-run 4))

(def subscription-comparator
  (fn [x y]
    (compare (sub-sort-val x) (sub-sort-val y))))

(defn sub-op-type->type [t]
  (case (:op-type t)
    :sub/create :created
    :sub/run :re-run
    :sub/dispose :destroyed

    :not-run))

(rf/reg-sub
  :subs/all-subs
  :<- [:subs/all-sub-traces]
  (fn [traces]
    (let [raw [{:id (gensym) :type :destroyed :layer "3" :path "[:todo/blah]" :open? false :diff? false}
               {:id (gensym) :type :re-run :layer "2" :path "[:todo/blah]" :open? false :diff? false}
               {:id (gensym) :type :created :layer "3" :path "[:todo/completed]" :open? false :diff? true}
               {:id (gensym) :type :re-run :layer "3" :path "[:todo/completed]" :open? false :diff? false}
               {:id (gensym) :type :not-run :layer "3" :path "[:todo/blah]" :open? false :diff? false}]

          raw (map (fn [trace] (let [pod-type (sub-op-type->type trace)
                                     path-str    (pr-str (get-in trace [:tags :query-v]))]
                                 {:id (str pod-type path-str)
                                  :type  pod-type
                                  :layer "2"
                                  :path  path-str

                                  ;; TODO: data for sub
                                  ;; TODO: get layer level
                                  ;; TODO: Get not run subscriptions

                                  :open? false
                                  :diff  false}))
                   traces)

          run-multiple? (frequencies (map :path raw))]
      (js/console.log "Run Multiple" run-multiple?)
      (js/console.log "Traces" traces)
      (js/console.log "Raw" raw)
      (sort-by identity subscription-comparator raw))))

(rf/reg-sub
  :subs/created-count
  :<- [:subs/all-sub-traces]
  (fn [traces]
    (count (filter metam/subscription-created? traces))))

(rf/reg-sub
  :subs/re-run-count
  :<- [:subs/all-sub-traces]
  (fn [traces]
    (count (filter metam/subscription-re-run? traces))))

(rf/reg-sub
  :subs/destroyed-count
  :<- [:subs/all-sub-traces]
  (fn [traces]
    (count (filter metam/subscription-destroyed? traces))))

(rf/reg-sub
  :subs/not-run-count
  :<- [:subs/all-sub-traces]
  (fn [traces]
    (count (filter metam/subscription-not-run? traces))))
