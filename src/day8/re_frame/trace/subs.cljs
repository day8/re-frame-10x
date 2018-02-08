(ns day8.re-frame.trace.subs
  (:require [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [day8.re-frame.trace.metamorphic :as metam]
            [day8.re-frame.trace.utils.utils :as utils]
            [clojure.string :as str]
            [cljs.spec.alpha :as s]
            [expound.alpha :as expound]))

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

(rf/reg-sub
  :settings/number-of-retained-epochs
  :<- [:settings/root]
  (fn [settings]
    (:number-of-epochs settings)))

(rf/reg-sub
  :settings/ignored-events
  :<- [:settings/root]
  (fn [settings]
    (sort-by :sort (vals (:ignored-events settings)))))

(rf/reg-sub
  :settings/filtered-view-trace
  :<- [:settings/root]
  (fn [settings]
    (sort-by :sort (vals (:filtered-view-trace settings)))))

(rf/reg-sub
  :settings/low-level-trace
  ;; TODO: filter from traces panel
  ;; TODO: eventually drop these low level traces after computing the state we need from them.
  :<- [:settings/root]
  (fn [settings]
    (:low-level-trace settings)))

(rf/reg-sub
  :settings/debug?
  :<- [:settings/root]
  (fn [settings]
    (:debug? settings)))

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

(rf/reg-sub
  :app-db/reagent-id
  :<- [:app-db/root]
  (fn [root _]
    (:reagent-id root)))

;;

(rf/reg-sub
  :traces/trace-root
  (fn [db _]
    (:traces db)))

(rf/reg-sub
  :trace-panel/root
  (fn [db _]
    (:trace-panel db)))

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
  :<- [:traces/all-traces]
  (fn [traces _]
    (count traces)))

(rf/reg-sub
  :traces/current-event-traces
  :<- [:traces/all-traces]
  :<- [:epochs/beginning-trace-id]
  :<- [:epochs/ending-trace-id]
  (fn [[traces beginning ending] _]
    (into [] (utils/id-between-xf beginning ending) traces)))

(defn filter-ignored-views [[traces filtered-views] _]
  (let [munged-ns (->> filtered-views
                       (map (comp munge :ns-str))
                       (set))]
    (into []
          ;; Filter out view namespaces we don't care about.
          (remove
            (fn [trace] (and (metam/render? trace)
                             (contains? munged-ns (subs (:operation trace) 0 (str/last-index-of (:operation trace) "."))))))
          traces)))

(rf/reg-sub
  :traces/current-event-visible-traces
  :<- [:traces/current-event-traces]
  :<- [:settings/filtered-view-trace]
  filter-ignored-views)

(rf/reg-sub
  :traces/all-visible-traces
  :<- [:traces/all-traces]
  :<- [:settings/filtered-view-trace]
  filter-ignored-views)

(rf/reg-sub
  :trace-panel/show-epoch-traces?
  :<- [:trace-panel/root]
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
  :epochs/current-match-state
  :<- [:epochs/epoch-root]
  :<- [:epochs/match-ids]
  (fn [[epochs match-ids] _]
    (let [current-id (:current-epoch-id epochs)
          match      (cond
                       (nil? current-id) (last (:matches epochs))
                       (< current-id (first match-ids)) (first (:matches epochs))
                       ;; This case seems impossible, but can happen if the user filters out
                       ;; an event that they are 'on'.
                       (> current-id (last match-ids)) (last (:matches epochs))
                       :else (get (:matches-by-id epochs) current-id))]
      match)))

(rf/reg-sub
  :epochs/current-match
  :<- [:epochs/current-match-state]
  (fn [match-state _]
    (:match-info match-state)))

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
  :epochs/current-epoch-id
  :<- [:epochs/epoch-root]
  (fn [epochs _]
    (:current-epoch-id epochs)))

(rf/reg-sub
  :epochs/match-ids
  :<- [:epochs/epoch-root]
  (fn [epochs]
    (:match-ids epochs)))

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
  :<- [:epochs/current-epoch-id]
  :<- [:epochs/match-ids]
  (fn [[current ids]]
    (and (< 1 (count ids))
         (or (nil? current)
             (> current (nth ids 0))))))

(rf/reg-sub
  :epochs/newer-epochs-available?
  :<- [:epochs/current-epoch-id]
  :<- [:epochs/match-ids]
  (fn [[current ids]]
    (and (< 1 (count ids))
         (some? current)
         (< current (utils/last-in-vec ids)))))

;;

(rf/reg-sub
  :timing/total-epoch-time
  :<- [:traces/current-event-traces]
  (fn [traces]
    (let [start-of-epoch (nth traces 0)
          end-of-epoch   (utils/last-in-vec traces)]
      (metam/elapsed-time start-of-epoch end-of-epoch))))

(rf/reg-sub
  :timing/animation-frame-traces
  :<- [:traces/current-event-traces]
  (fn [traces]
    (filter #(or (metam/request-animation-frame? %)
                 (metam/request-animation-frame-end? %))
            traces)))

(rf/reg-sub
  :timing/animation-frame-count
  :<- [:timing/animation-frame-traces]
  (fn [frame-traces]
    (count (filter metam/request-animation-frame? frame-traces))))

(rf/reg-sub
  :timing/animation-frame-time
  :<- [:timing/animation-frame-traces]
  (fn [frame-traces [_ frame-number]]
    (let [frames (partition 2 frame-traces)
          [start end] (first (drop (dec frame-number) frames))]
      (metam/elapsed-time start end))))

(rf/reg-sub
  :timing/event-processing-time
  :<- [:epochs/current-match-state]
  (fn [match]
    (get-in match [:timing :re-frame/event-time])))

(rf/reg-sub
  :timing/render-time
  :<- [:traces/current-event-traces]
  (fn [traces]
    (let [start-of-render (first (filter metam/request-animation-frame? traces))
          end-of-epoch    (utils/last-in-vec traces)]
      (metam/elapsed-time start-of-render end-of-epoch))))

(rf/reg-sub
  :timing/data-available?
  :<- [:traces/current-event-traces]
  (fn [traces]
    (not (empty? traces))))

;;

(rf/reg-sub
  :subs/root
  (fn [db _]
    (:subs db)))

(rf/reg-sub
  :subs/all-sub-traces
  :<- [:traces/current-event-traces]
  (fn [traces]
    (filter metam/subscription? traces)))

(rf/reg-sub
  :subs/subscription-info
  :<- [:epochs/epoch-root]
  (fn [epoch]
    (:subscription-info epoch)))

(rf/reg-sub
  :subs/sub-state
  :<- [:epochs/epoch-root]
  (fn [epochs]
    (:sub-state epochs)))

(rf/reg-sub
  :subs/current-epoch-sub-state
  :<- [:epochs/current-match-state]
  (fn [match-state]
    (:sub-state match-state)))

(def string! (s/and string? #(not (empty? %))))

(s/def :sub/id string!)
(s/def :sub/reagent-id string!)
(s/def :sub/type #{:created :re-run :destroyed :not-run})
(s/def :sub/layer (s/nilable pos-int?))
(s/def :sub/path-data any?)
(s/def :sub/path string!)
(s/def :sub/value any?)
(s/def :sub/previous-value any?)
(s/def :subs/view-panel-sub
  (s/keys :req-un [:sub/id :sub/reagent-id :sub/type :sub/layer :sub/path-data :sub/path]
          :opt-un [:sub/value :sub/previous-value]))
(s/def :subs/view-subs (s/coll-of :subs/view-panel-sub))




(defn sub-sort-val
  [sub-type]
  (case sub-type
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

(defn prepare-pod-info
  "Returns sub info prepared for rendering in pods"
  [[sub-info sub-state]]
  (let [rx-state (:reaction-state sub-state)
        subx     (->>
                   rx-state
                   (remove (fn [me] (nil? (:order (val me)))))
                   (map (fn [me] (let [state        (val me)
                                       subscription (:subscription state)
                                       sub          {:id         (key me)
                                                     :reagent-id (key me)
                                                     :type       :created
                                                     :layer      (get-in sub-info [(first subscription) :layer])
                                                     :path-data  subscription
                                                     :path       (pr-str subscription)
                                                     :order      (:order state)}
                                       sub          (if (contains? state :value)
                                                      (assoc sub :value (:value state))
                                                      sub)
                                       sub          (if (contains? state :previous-value)
                                                      (assoc sub :previous-value (:previous-value state))
                                                      sub)]
                                   sub))))
        ]
    (utils/spy "subx" subx)


    (when-not (s/valid? :subs/view-subs subx)
      (js/console.error (expound/expound-str :subs/view-subs subx)))

    subx))

(rf/reg-sub
  :subs/inter-epoch-subs
  :<- [:subs/subscription-info]
  :<- [:subs/current-epoch-sub-state]
  prepare-pod-info)

(rf/reg-sub
  :subs/all-subs
  :<- [:subs/subscription-info]
  :<- [:subs/current-epoch-sub-state]
  prepare-pod-info)

(rf/reg-sub
  :subs/visible-subs
  :<- [:subs/all-subs]
  :<- [:subs/ignore-unchanged-l2-subs?]
  (fn [[all-subs ignore-unchanged-l2?]]
    (if ignore-unchanged-l2?
      (remove metam/unchanged-l2-subscription? all-subs)
      all-subs)))

(rf/reg-sub
  :subs/sub-counts
  :<- [:subs/visible-subs]
  (fn [subs _]
    (->> subs
         (map :type)
         (frequencies))))

(rf/reg-sub
  :subs/created-count
  :<- [:subs/sub-counts]
  (fn [counts]
    (get counts :created 0)))

(rf/reg-sub
  :subs/re-run-count
  :<- [:subs/sub-counts]
  (fn [counts]
    (get counts :re-run 0)))

(rf/reg-sub
  :subs/destroyed-count
  :<- [:subs/sub-counts]
  (fn [counts]
    (get counts :destroyed 0)))

(rf/reg-sub
  :subs/not-run-count
  :<- [:subs/sub-counts]
  (fn [counts]
    (get counts :not-run 0)))

(rf/reg-sub
  :subs/unchanged-l2-subs-count
  :<- [:subs/all-subs]
  (fn [subs]
    (count (filter metam/unchanged-l2-subscription? subs))))

(rf/reg-sub
  :subs/ignore-unchanged-l2-subs?
  :<- [:subs/root]
  (fn [subs _]
    (:ignore-unchanged-subs? subs true)))

(rf/reg-sub
  :subs/sub-expansions
  :<- [:subs/root]
  (fn [subs _]
    (:expansions subs)))
