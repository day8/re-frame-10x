(ns day8.re-frame-10x.subs
  (:require [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [day8.re-frame-10x.metamorphic :as metam]
            [day8.re-frame-10x.utils.utils :as utils]
            [clojure.string :as str]
            [cljs.spec.alpha :as s]
            [zprint.core :as zp]))

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

(rf/reg-sub
  :settings/app-db-follows-events?
  :<- [:settings/root]
  (fn [settings]
    (:app-db-follows-events? settings)))

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
  :<- [:epochs/current-match]
  (fn [epochs _]
    (:id (first epochs))))

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

(defn ^number +nil
  "Returns the sum of nums. (+) returns nil (not 0 like in cljs.core)."
  ([] nil)
  ([x] x)
  ([x y] (cljs.core/+ x y))
  ([x y & more]
   (reduce + (cljs.core/+ x y) more)))

(rf/reg-sub
  :timing/animation-frame-time
  :<- [:timing/animation-frame-traces]
  :<- [:traces/current-event-traces]
  (fn [[af-start-end epoch-traces] [_ frame-number]]
    (let [frame-pairs (partition 2 af-start-end)
          [start end] (nth frame-pairs (dec frame-number))
          af-traces   (into [] (metam/id-between-xf (:id start) (:id end)) epoch-traces)
          total-time  (metam/elapsed-time start end)
          ;; TODO: these times double count renders/subs that happened as a child of another
          ;; need to fix either here, at ingestion point, or most preferably in re-frame at tracing point.
          subs-time   (transduce (comp
                                   (filter metam/subscription?)
                                   (map :duration))
                                 +nil af-traces)
          render-time (transduce (comp
                                   (filter metam/render?)
                                   (map :duration))
                                 +nil af-traces)]
      {:timing/animation-frame-total  total-time
       :timing/animation-frame-subs   subs-time
       :timing/animation-frame-render render-time
       ;; TODO: handle rounding weirdness here, make sure it is never below 0.
       :timing/animation-frame-misc   (- total-time subs-time render-time)})))

(rf/reg-sub
  :timing/event-processing-time
  :<- [:epochs/current-match-state]
  (fn [match]
    (let [{:re-frame/keys [event-time event-handler-time event-dofx-time event-run-time]} (get match :timing)
          ;; The scope of tracing is:
          ;; event-run-time
          ;;   event-time
          ;;     event-handler-time
          ;;     event-dofx-time
          ;;     <other stuff>
          ;;   <other stuff>
          remaining-interceptors (- event-time event-handler-time event-dofx-time)]
      {:timing/event-total        event-run-time
       :timing/event-handler      event-handler-time
       :timing/event-effects      event-dofx-time
       :timing/event-interceptors remaining-interceptors
       ;; TODO: look at splitting out interceptors from misc, there was a suspiciously high amount of time
       ;; in misc on some events, so that needs to be investigated.
       ; :timing/event-misc (- event-run-time event-time)
       :timing/event-misc         (- event-run-time event-handler-time event-dofx-time)})))

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
(s/def :sub/run-types #{:sub/create :sub/dispose :sub/run :sub/not-run})
(s/def :sub/order (s/nilable (s/coll-of :sub/run-types)))
(s/def :sub/layer (s/nilable pos-int?))
(s/def :sub/path-data any?)
(s/def :sub/path string!)
(s/def :sub/value any?)
(s/def :sub/previous-value any?)
(s/def :subs/view-panel-sub
  (s/keys :req-un [:sub/id :sub/reagent-id :sub/order :sub/layer :sub/path-data :sub/path]
          :opt-un [:sub/value :sub/previous-value]))
(s/def :subs/view-subs (s/coll-of :subs/view-panel-sub))

(defn sub-type-value
  [sub-type]
  (case sub-type
    :sub/create 5
    :sub/run 4
    :sub/dispose 3
    :sub/not-run 2
    1))

(defn accumulate-sub-value
  "Calculate a sorting value for a series of subscription trace types."
  ;; A reader might reasonably ask, "Why are we going to all this work here?"
  ;; We calculate a custom value rather than just comparing two order vectors,
  ;; because the default compare logic for comparing vectors is to sort shorter
  ;; vectors above longer ones, whereas we want all CRR, CR, C orders to be
  ;; sorted adjacent to each other, in that order.
  ;;
  ;; The first sub type in the order is worth (n * 10^3),
  ;; then the next one (if it exists), is worth (n * 10^2), and so-on.
  [order]
  (loop [exp   3
         total 0
         order order]
    (if-let [sub-type (first order)]
      (recur (dec exp) (+ total (* (sub-type-value sub-type) (js/Math.pow 10 exp))) (rest order))
      total)))

(def accumulate-sub-value-memoized
  (memoize accumulate-sub-value))

(defn sub-sort-val [order-x order-y]
  ;; Note x and y are reversed here so that the "highest" sub orders get sorted first.
  (compare (accumulate-sub-value-memoized order-y)
           (accumulate-sub-value-memoized order-x)))

(defn sub-op-type->type [t]
  (case (:op-type t)
    :sub/create :created
    :sub/run :re-run
    :sub/dispose :destroyed

    :not-run))

(defn prepare-pod-info
  "Returns sub info prepared for rendering in pods"
  [[sub-info sub-state] [subscription]]
  (let [remove-fn (if (= subscription :subs/intra-epoch-subs)
                    (fn [me] (nil? (:order (val me))))
                    (constantly false))
        subx      (->> sub-state
                       (remove remove-fn)
                       (map (fn [me] (let [state        (val me)
                                           subscription (:subscription state)
                                           sub          {:id         (key me)
                                                         :reagent-id (key me)
                                                         :layer      (get-in sub-info [(first subscription) :layer])
                                                         :path-data  subscription
                                                         :path       (pr-str subscription)
                                                         :order      (or (:order state) [:sub/not-run])
                                                         :sub/traits (:sub/traits state)}
                                           sub          (if (contains? state :value)
                                                          (assoc sub :value (:value state))
                                                          sub)
                                           sub          (if (contains? state :previous-value)
                                                          (assoc sub :previous-value (:previous-value state))
                                                          sub)]
                                       sub)))
                       (sort-by :order sub-sort-val)        ;; Also sort by subscription-id
                       #_(sort-by :path))]
    subx))


(rf/reg-sub
  :subs/pre-epoch-state
  :<- [:subs/current-epoch-sub-state]
  (fn [sub-state]
    (:pre-epoch-state sub-state)))

(rf/reg-sub
  :subs/reaction-state
  :<- [:subs/current-epoch-sub-state]
  (fn [sub-state]
    (:reaction-state sub-state)))

(rf/reg-sub
  :subs/intra-epoch-subs
  :<- [:subs/subscription-info]
  :<- [:subs/pre-epoch-state]
  prepare-pod-info)

(rf/reg-sub
  :subs/all-subs
  :<- [:subs/subscription-info]
  :<- [:subs/reaction-state]
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
         (mapcat :order)
         (frequencies))))

(rf/reg-sub
  :subs/created-count
  :<- [:subs/sub-counts]
  (fn [counts]
    (get counts :sub/create 0)))

(rf/reg-sub
  :subs/re-run-count
  :<- [:subs/sub-counts]
  (fn [counts]
    (get counts :sub/run 0)))

(rf/reg-sub
  :subs/destroyed-count
  :<- [:subs/sub-counts]
  (fn [counts]
    (get counts :sub/dispose 0)))

(rf/reg-sub
  :subs/not-run-count
  :<- [:subs/sub-counts]
  (fn [counts]
    (get counts :sub/not-run 0)))

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


;;

(rf/reg-sub
  :code/root
  (fn [db _]
    (:code db)))

(rf/reg-sub
  :code/current-code
  :<- [:traces/current-event-traces]
  (fn [traces _]
    (keep-indexed (fn [i trace]
                    (when-some [code (get-in trace [:tags :code])]
                      {:id    i
                       :trace-id (:id trace)
                       :title (pr-str (:op-type trace))
                       :code  (->> code (map-indexed (fn [i code] (assoc code :id i))) vec) ;; Add index
                       :form  (get-in trace [:tags :form])}))
                  traces)))

(rf/reg-sub
  :code/current-form
  :<- [:code/current-code]
  (fn [code _]
    (:form (first code))))

(rf/reg-sub
  :code/current-zprint-form
  :<- [:code/current-form]
  (fn [form _]
    (zp/zprint-str form)))

(rf/reg-sub
  :code/code-open?
  :<- [:code/root]
  (fn [code _]
    (:code-open? code)))

(rf/reg-sub
  :code/highlighted-form
  :<- [:code/root]
  (fn [code _]
    (:highlighted-form code)))

(rf/reg-sub
  :code/show-all-code?
  :<- [:code/root]
  (fn [code _]
    (:show-all-code? code)))

(rf/reg-sub
  :code/repl-msg-state
  :<- [:code/root]
  (fn [code _]
    (:repl-msg-state code)))


;;

(rf/reg-sub
  :component/root
  (fn [db _]
    (:component db)))

(rf/reg-sub
  :component/direction
  :<- [:component/root]
  (fn [component _]
    (:direction component)))
