(ns day8.re-frame-10x.panels.subs.subs
  (:require
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.navigation.epochs.subs :as epochs.subs]
    [day8.re-frame-10x.panels.traces.subs :as traces.subs]
    [day8.re-frame-10x.metamorphic :as metam]
    [clojure.string :as string]))

(rf/reg-sub
  ::root
  (fn [{:keys [subs]} _]
    subs))

(rf/reg-sub
  ::all-sub-traces
  :<- [::traces.subs/filtered-by-epoch-always]
  (fn [traces]
    (filter metam/subscription? traces)))

(rf/reg-sub
  ::subscription-info
  :<- [::epochs.subs/root]
  (fn [{:keys [subscription-info]} _]
    subscription-info))

(rf/reg-sub
  ::sub-state
  :<- [::epochs.subs/root]
  (fn [{:keys [sub-state]} _]
    sub-state))

(rf/reg-sub
  ::current-epoch-sub-state
  :<- [::epochs.subs/selected-match-state]
  (fn [{:keys [sub-state]} _]
    sub-state))

(defn sub-type->value
  [sub-type]
  (case sub-type
    :sub/create  5
    :sub/run     4
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
      (recur (dec exp)
             (+ total (* (sub-type->value sub-type) (js/Math.pow 10 exp)))
             (rest order))
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
  (let [remove-fn (if (= subscription ::intra-epoch-subs)
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
  ::pre-epoch-state
  :<- [::current-epoch-sub-state]
  (fn [{:keys [pre-epoch-state]} _]
    pre-epoch-state))

(rf/reg-sub
  ::reaction-state
  :<- [::current-epoch-sub-state]
  (fn [{:keys [reaction-state]} _]
    reaction-state))

(rf/reg-sub
  ::intra-epoch-subs
  :<- [::subscription-info]
  :<- [::pre-epoch-state]
  prepare-pod-info)

(rf/reg-sub
  ::all-subs
  :<- [::subscription-info]
  :<- [::reaction-state]
  prepare-pod-info)

(rf/reg-sub
  ::filter-str
  :<- [::root]
  (fn [{:keys [filter-str]} _]
    filter-str))

(rf/reg-sub
  ::visible-subs
  :<- [::all-subs]
  :<- [::ignore-unchanged-l2-subs?]
  :<- [::filter-str]
  :<- [::sub-pins]
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
                                                (or (string/includes? path filter-str)
                                                    (get-in pins [id :pin?]))))))))

(rf/reg-sub
  ::sub-counts
  :<- [::visible-subs]
  (fn [subs _]
    (->> subs
         (mapcat :order)
         (frequencies))))

(rf/reg-sub
  ::created-count
  :<- [::sub-counts]
  (fn [counts]
    (get counts :sub/create 0)))

(rf/reg-sub
  ::re-run-count
  :<- [::sub-counts]
  (fn [counts]
    (get counts :sub/run 0)))

(rf/reg-sub
  ::destroyed-count
  :<- [::sub-counts]
  (fn [counts]
    (get counts :sub/dispose 0)))

(rf/reg-sub
  ::not-run-count
  :<- [::sub-counts]
  (fn [counts]
    (get counts :sub/not-run 0)))

(rf/reg-sub
  ::unchanged-l2-subs-count
  :<- [::all-subs]
  (fn [subs]
    (count (filter metam/unchanged-l2-subscription? subs))))

(rf/reg-sub
  ::ignore-unchanged-l2-subs?
  :<- [::root]
  (fn [subs _]
    (:ignore-unchanged-subs? subs true)))

(rf/reg-sub
  ::sub-expansions
  :<- [::root]
  (fn [{:keys [expansions]} _]
    expansions))

(rf/reg-sub
  ::sub-pins
  :<- [::root]
  (fn [{:keys [pinned]} _]
    pinned))
