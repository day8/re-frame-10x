(ns day8.re-frame-10x.panels.timing.subs
  (:require
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.tools.metamorphic                          :as metam]
    [day8.re-frame-10x.navigation.epochs.subs                     :as epochs.subs]
    [day8.re-frame-10x.panels.traces.subs                         :as traces.subs]
    [day8.re-frame-10x.tools.coll                                 :as tools.coll]))

(rf/reg-sub
  ::total-epoch-time
  :<- [::traces.subs/filtered-by-epoch-always]
  (fn [traces]
    (let [start-of-epoch (nth traces 0)
          end-of-epoch   (tools.coll/last-in-vec traces)]
      (metam/elapsed-time start-of-epoch end-of-epoch))))

(rf/reg-sub
  ::animation-frame-traces
  :<- [::traces.subs/filtered-by-epoch-always]
  (fn [traces]
    (filter #(or (metam/request-animation-frame? %)
                 (metam/request-animation-frame-end? %))
            traces)))

(rf/reg-sub
  ::animation-frame-count
  :<- [::animation-frame-traces]
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
  ::animation-frame-time
  :<- [::animation-frame-traces]
  :<- [::traces.subs/filtered-by-epoch-always]
  (fn [[af-start-end epoch-traces] [_ frame-number]]
    (let [frame-pairs (partition 2 af-start-end)]
      (when (> (count frame-pairs) frame-number)
        (let [[start end] (nth frame-pairs (dec frame-number))
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
           :timing/animation-frame-misc   (- total-time subs-time render-time)})))))


(rf/reg-sub
  ::event-processing-time
  :<- [::epochs.subs/selected-match-state]
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
  ::render-time
  :<- [::traces.subs/filtered-by-epoch-always]
  (fn [traces]
    (let [start-of-render (first (filter metam/request-animation-frame? traces))
          end-of-epoch    (tools.coll/last-in-vec traces)]
      (metam/elapsed-time start-of-render end-of-epoch))))

(rf/reg-sub
  ::data-available?
  :<- [::traces.subs/filtered-by-epoch-always]
  (fn [traces]
    (not (empty? traces))))

