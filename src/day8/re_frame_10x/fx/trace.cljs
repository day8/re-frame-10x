(ns day8.re-frame-10x.fx.trace
  (:require
   [re-frame.trace]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
   [day8.re-frame-10x.navigation.epochs.events                   :as epochs.events]))

(defn enable!
  [{:keys [key]}]
  (re-frame.trace/register-trace-cb key
                                    #(rf/dispatch [::epochs.events/receive-new-traces %])))

(rf/reg-fx
 ::enable
 enable!)

(defn disable!
  [{:keys [key]}]
  (re-frame.trace/remove-trace-cb key))

(rf/reg-fx
 ::disable
 disable!)
