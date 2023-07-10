(ns day8.re-frame-10x.panels.event.events
  (:require
   [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]))

(rf/reg-event-db
 ::set-code-visibility
 [(rf/path [:code :code-open?]) rf/trim-v]
 (fn [code-open? [open?-path open?]]
   (assoc-in code-open? open?-path open?)))

(rf/reg-event-db
 ::set-execution-order
 [(rf/path [:code :execution-order?]) rf/trim-v]
 (fn [_ [execution-order?]]
   execution-order?))

(rf/reg-event-db
 ::hover-form
 [(rf/path [:code :highlighted-form]) rf/trim-v]
 (fn [_ [new-form]]
   new-form))

(rf/reg-event-db
 ::exit-hover-form
 [(rf/path [:code :highlighted-form]) rf/trim-v]
 (fn [form [new-form]]
   (if (= form new-form)
     nil
     new-form)))

(rf/reg-event-db
 ::set-show-all-code?
 [(rf/path [:code :show-all-code?]) rf/trim-v]
 (fn [_ [show-all-code?]]
   show-all-code?))

(rf/reg-event-db
 ::repl-msg-state
 [(rf/path [:code :repl-msg-state]) rf/trim-v]
 (fn [current-state [new-state]]
   (if (and (= current-state :running) (= new-state :start)) ;; Toggles between :running and :re-running to guarantee rerenderig when you continuously call this event
     :re-running
     (if (= new-state :start) :running :end))))