(ns day8.re-frame-10x.public.events
  "Internal: re-frame handler registrations for the public mutation API.

   Required from `day8.re-frame-10x.public` so registrations fire at
   namespace-load time. Consumers MUST NOT reach into this namespace —
   the public contract is the keyword event identifiers exposed as
   strings from `day8.re-frame-10x.public`, dispatched via
   `public/dispatch!`. Handler-key keywords here are the keyword form
   of those public string identifiers; drift between the two is gated
   end-to-end by tests in public_test.cljs and public_navigation_test.cljs."
  (:require
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core :as rf]
   [day8.re-frame-10x.navigation.epochs.events                   :as nav.events]))

(defn- no-op-fx
  "Return a re-frame effect map that intentionally dispatches no effects."
  []
  {})

(defn- second-newest-match-id
  "Return the match id immediately before the live tail, or nil when absent."
  [match-ids]
  (when (> (count match-ids) 1)
    (nth match-ids (- (count match-ids) 2))))

(defn previous-epoch-fx
  "Decide the fx for the `:day8.re-frame-10x.public/previous-epoch`
   forwarder, gating no-op cases the internal handler does not.

   Public to this namespace so unit tests can call it directly without
   `#'` reach-around: the inner `::nav.events/previous` clobbers
   `:selected-epoch-id` to nil at the oldest match and from the live
   tail, so a behavioural test through the public surface can't
   observe the no-op decisions this helper gates on."
  [{:keys [match-ids selected-epoch-id]}]
  (let [oldest-match-id  (first match-ids)
        at-oldest?       (= selected-epoch-id oldest-match-id)
        at-live-tail?    (nil? selected-epoch-id)
        previous-tail-id (second-newest-match-id match-ids)]
    (cond
      (or (empty? match-ids) at-oldest?) (no-op-fx)
      (and at-live-tail? previous-tail-id) {:dispatch [::nav.events/load previous-tail-id]}
      at-live-tail? (no-op-fx)
      :else {:dispatch [::nav.events/previous]})))

(rf/reg-event-fx
 :day8.re-frame-10x.public/load-epoch
 [rf/trim-v]
 (fn [_ [id]]
   {:dispatch [::nav.events/load id]}))

(rf/reg-event-fx
 :day8.re-frame-10x.public/most-recent-epoch
 (fn [_ _]
   {:dispatch [::nav.events/most-recent]}))

(rf/reg-event-fx
 :day8.re-frame-10x.public/previous-epoch
 (fn [{:keys [db]} _]
   (previous-epoch-fx (:epochs db))))

(rf/reg-event-fx
 :day8.re-frame-10x.public/next-epoch
 (fn [_ _]
   {:dispatch [::nav.events/next]}))

(rf/reg-event-fx
 :day8.re-frame-10x.public/reset-epochs
 (fn [_ _]
   {:dispatch [::nav.events/reset]}))

(rf/reg-event-db
 :day8.re-frame-10x.public/replay-epoch
 [(rf/path [:epochs])]
 (fn [epochs _]
   (nav.events/replay-epochs epochs)))

(rf/reg-event-fx
 :day8.re-frame-10x.public/reset-app-db
 [rf/trim-v]
 (fn [_ [id]]
   {:dispatch [::nav.events/reset-current-epoch-app-db id]}))
