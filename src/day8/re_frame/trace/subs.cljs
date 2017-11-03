(ns day8.re-frame.trace.subs
  (:require [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]))

(rf/reg-sub
  :settings/panel-width%
  (fn [db _]
    (get-in db [:settings :panel-width%])))

(rf/reg-sub
  :settings/show-panel?
  (fn [db _]
    (get-in db [:settings :show-panel?])))

(rf/reg-sub
  :settings/selected-tab
  (fn [db _]
    (get-in db [:settings :selected-tab])))
