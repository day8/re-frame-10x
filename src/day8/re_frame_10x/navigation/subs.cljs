(ns day8.re-frame-10x.navigation.subs
  (:require
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]))

(rf/reg-sub
  ::global
  (fn [{:keys [global]} _]
    global))

(rf/reg-sub
  ::unloading?
  (fn [{:keys [unloading?]} _]
    unloading?))

(rf/reg-sub
  ::errors
  (fn [{:keys [errors]} _]
    errors))

(rf/reg-sub
  ::popup-failed?
  :<- [::errors]
  (fn [{:keys [popup-failed?]} _]
    popup-failed?))