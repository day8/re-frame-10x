(ns day8.re-frame-10x.subs
  (:require
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.metamorphic :as metam]
    [day8.re-frame-10x.epochs.subs :as epochs.subs]
    [day8.re-frame-10x.traces.subs :as traces.subs]
    [clojure.string :as str]
    [cljs.spec.alpha :as s]))

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
  :subs/root
  (fn [db _]
    (:subs db)))

(rf/reg-sub
  :subs/all-sub-traces
  :<- [::traces.subs/filtered-by-epoch]
  (fn [traces]
    (filter metam/subscription? traces)))

(rf/reg-sub
  :subs/subscription-info
  :<- [::epochs.subs/root]
  (fn [epoch]
    (:subscription-info epoch)))

(rf/reg-sub
  :subs/sub-state
  :<- [::epochs.subs/root]
  (fn [epochs]
    (:sub-state epochs)))

(rf/reg-sub
  :subs/current-epoch-sub-state
  :<- [::epochs.subs/selected-match-state]
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
  :subs/filter-str
  :<- [:subs/root]
  (fn [root _]
    (:filter-str root)))


(rf/reg-sub
  :subs/visible-subs
  :<- [:subs/all-subs]
  :<- [:subs/ignore-unchanged-l2-subs?]
  :<- [:subs/filter-str]
  :<- [:subs/sub-pins]
  (fn [[all-subs ignore-unchanged-l2? filter-str pins]]
    (let [compare-fn (fn [s1 s2]
                       (let [p1 (boolean (get-in pins [(:id s1) :pin?]))
                             p2 (boolean (get-in pins [(:id s2) :pin?]))]
                         (if (= p1 p2)
                           (compare (:path s1) (:path s2))
                           p1)))]
      (cond->> (sort compare-fn all-subs)
               ignore-unchanged-l2? (remove metam/unchanged-l2-subscription?)
               (not-empty filter-str) (filter (fn [{:keys [path id]}]
                                                (or (str/includes? path filter-str)
                                                    (get-in pins [id :pin?]))))))))

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

(rf/reg-sub
  :subs/sub-pins
  :<- [:subs/root]
  (fn [subs _]
    (:pinned subs)))

;;

(rf/reg-sub
  :errors/root
  (fn [db _]
    (:errors db)))

(rf/reg-sub
  :errors/popup-failed?
  :<- [:errors/root]
  (fn [errors _]
    (:popup-failed? errors)))

