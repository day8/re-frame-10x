(ns day8.re-frame-10x.navigation.epochs.subs
  (:require
   [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
   [day8.re-frame-10x.tools.metamorphic                          :as metam]
   [day8.re-frame-10x.tools.coll                                 :as tools.coll]))

(rf/reg-sub
 ::root
 (fn [{:keys [epochs]} _]
   epochs))

(rf/reg-sub
 ::filter-str
 :<- [::root]
 (fn [{:keys [filter-str]} _]
   filter-str))

(rf/reg-sub
 ::matches-by-id
 :<- [::root]
 (fn [{:keys [matches-by-id]} _]
   matches-by-id))

(rf/reg-sub
 ::events-by-id
 :<- [::matches-by-id]
 :<- [::filter-str]
 (fn [[matches-by-id filter-str] _]
   (let [matches-by-id (map (juxt key (comp :event :tags metam/matched-event :match-info val))
                            matches-by-id)]
     (sort-by first > (if (seq filter-str)
                        (filter (fn [match]
                                  (let [reg-ex       (try
                                                       (re-pattern filter-str)
                                                       (catch js/Error e
                                                         (js/console.error e)
                                                         #""))
                                        match-string (-> match second str)]
                                    (re-find reg-ex match-string)))
                                matches-by-id)
                        matches-by-id)))))

(rf/reg-sub
 ::selected-epoch-id
 :<- [::root]
 (fn [{:keys [selected-epoch-id]} _]
   selected-epoch-id))

(rf/reg-sub
 ::match-ids
 :<- [::root]
 (fn [{:keys [match-ids]} _]
   match-ids))

(rf/reg-sub
 ::matches
 :<- [::root]
 (fn [{:keys [matches]} _]
   matches))

(rf/reg-sub
 ::selected-match-state
 :<- [::match-ids]
 :<- [::matches]
 :<- [::matches-by-id]
 :<- [::selected-epoch-id]
 (fn [[match-ids matches matches-by-id selected-epoch-id] _]
   (cond
     (nil? selected-epoch-id) (last matches)
     (< selected-epoch-id (first match-ids)) (first matches)
      ;; This case seems impossible, but can happen if the user filters out
      ;; an event that they are 'on'.
     (> selected-epoch-id (last match-ids)) (last matches)
     :else (get matches-by-id selected-epoch-id))))

(rf/reg-sub
 ::selected-match
 :<- [::selected-match-state]
 (fn [{:keys [match-info]} _]
   match-info))

(rf/reg-sub
 ::selected-event-trace
 :<- [::selected-match]
 (fn [match _]
   (metam/matched-event match)))

(rf/reg-sub
 ::selected-event
 :<- [::selected-event-trace]
 (fn [trace _]
   (get-in trace [:tags :event])))

(rf/reg-sub
 ::number-of-matches
 :<- [::matches]
 (fn [matches _]
   (count matches)))

(rf/reg-sub
 ::beginning-trace-id
 :<- [::selected-match]
 (fn [match _]
   (:id (first match))))

(rf/reg-sub
 ::ending-trace-id
 :<- [::selected-match]
 (fn [match _]
   (:id (last match))))

(rf/reg-sub
 ::older-epochs-available?
 :<- [::selected-epoch-id]
 :<- [::match-ids]
 (fn [[selected-epoch-id match-ids] _]
   (and (< 1 (count match-ids))
        (or (nil? selected-epoch-id)
            (> selected-epoch-id (nth match-ids 0))))))

(rf/reg-sub
 ::newer-epochs-available?
 :<- [::selected-epoch-id]
 :<- [::match-ids]
 (fn [[selected-epoch-id match-ids] _]
   (and (< 1 (count match-ids))
        (some? selected-epoch-id)
        (< selected-epoch-id (tools.coll/last-in-vec match-ids)))))

