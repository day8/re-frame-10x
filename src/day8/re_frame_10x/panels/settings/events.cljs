(ns day8.re-frame-10x.panels.settings.events
  (:require
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.utils.localstorage :as localstorage]))

(rf/reg-event-db
  ::panel-width%
  [(rf/path [:settings :panel-width%]) rf/trim-v (localstorage/after "panel-width-ratio")]
  (fn [_ [width%]]
    (max width% 0.05)))

(rf/reg-event-db
  ::window-width
  [(rf/path [:settings :window-width]) rf/trim-v]
  (fn [_ [width]]
    width))

(rf/reg-event-db
  ::selected-tab
  [(rf/path [:settings :selected-tab]) rf/trim-v (localstorage/after "selected-tab")]
  (fn [_ [selected-tab]]
    selected-tab))

(rf/reg-event-db
  ::toggle
  [(rf/path [:settings :showing-settings?])]
  (fn [showing? _]
    (not showing?)))

(rf/reg-event-db
  ::show-panel?
  [(rf/path [:settings :show-panel?]) rf/trim-v (localstorage/after "show-panel")]
  (fn [_ [show-panel?]]
    show-panel?))

(rf/reg-event-db
  ::factory-reset
  (fn [db _]
    ;; [IJ] TODO: these should be fx
    (localstorage/delete-all-keys!)
    (js/location.reload)
    db))

(rf/reg-event-db
  ::set-ambiance
  [(rf/path [:settings :ambiance]) rf/trim-v (localstorage/after "ambiance")]
  (fn [_ [ambiance]]
    ambiance))

(rf/reg-event-db
  ::set-syntax-color-scheme
  [(rf/path [:settings :syntax-color-scheme]) rf/trim-v (localstorage/after "syntax-color-scheme")]
  (fn [_ [syntax-color-scheme]]
    syntax-color-scheme))