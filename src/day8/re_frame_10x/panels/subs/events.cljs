(ns day8.re-frame-10x.panels.subs.events
  (:require
   [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]))

(rf/reg-event-db
 ::ignore-unchanged-l2-subs?
 [(rf/path [:subs :ignore-unchanged-subs?]) rf/trim-v]
 (fn [_ [ignore?]]
   ignore?))

(rf/reg-event-db
 ::open-pod?
 [(rf/path [:subs :expansions]) rf/trim-v]
 (fn [expansions [id open?]]
   (assoc-in expansions [id :open?] open?)))

(rf/reg-event-db
 ::set-diff-visibility
 [(rf/path [:subs :expansions]) rf/trim-v]
 (fn [expansions [id diff?]]
   (let [open? (if diff?
                 true
                 (get-in expansions [id :open?]))]
     (-> expansions
         (assoc-in [id :diff?] diff?)
          ;; If we turn on diffing then we want to also expand the path
         (assoc-in [id :open?] open?)))))

(rf/reg-event-db
 ::set-pinned
 [(rf/path [:subs :pinned]) rf/trim-v]
 (fn [pinned [id pinned?]]
   (assoc-in pinned [id :pin?] pinned?)))

(rf/reg-event-db
 ::set-filter
 [(rf/path [:subs :filter-str]) rf/trim-v]
 (fn [_ [filter-str]]
   filter-str))