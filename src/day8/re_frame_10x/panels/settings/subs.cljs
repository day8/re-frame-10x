(ns day8.re-frame-10x.panels.settings.subs
  (:require
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]))

(rf/reg-sub
  ::root
  (fn [{:keys [settings]} _]
    settings))

(rf/reg-sub
  ::panel-width%
  :<- [::root]
  (fn [{:keys [panel-width%]} _]
    panel-width%))

(rf/reg-sub
  ::panel-width%-rounded
  :<- [::panel-width%]
  ;; Rounds panel width to nearest n%
  (fn [panel-width% [_ n]]
    ;; https://stackoverflow.com/a/19621472
    (/ (* (Math/ceil (/ (* panel-width% 100)
                        n))
          n)
       100.0)))

(rf/reg-sub
  ::window-width
  ;; Prefer window-width-rounded if you don't need the exact number of pixels.
  :<- [::root]
  (fn [{:keys [window-width]} _]
    window-width))

(rf/reg-sub
  ::window-width-rounded
  :<- [::window-width]
  ;; Window width, rounded up to the nearest n pixels.
  ;; Useful when you want to respond to window size changes
  ;; but not too many of them.
  (fn [width [_ n]]
    (* (Math/ceil (/ width n))
       n)))

(rf/reg-sub
  ::show-panel?
  :<- [::root]
  (fn [{:keys [show-panel?]} _]
    show-panel?))

(rf/reg-sub
  ::showing-settings?
  :<- [::root]
  (fn [{:keys [showing-settings?]} _]
    showing-settings?))

(rf/reg-sub
  ::selected-tab
  :<- [::root]
  :<- [::showing-settings?]
  (fn [[{:keys [selected-tab]} showing-settings?] _]
    (if showing-settings?
      :settings
      selected-tab)))

(rf/reg-sub
  ::number-of-retained-epochs
  :<- [::root]
  (fn [{:keys [number-of-epochs]} _]
    number-of-epochs))

(rf/reg-sub
  ::ignored-events
  :<- [::root]
  (fn [{:keys [ignored-events]} _]
    (sort-by :sort (vals ignored-events))))

(rf/reg-sub
  ::filtered-view-trace
  :<- [::root]
  (fn [{:keys [filtered-view-trace]} _]
    (sort-by :sort (vals filtered-view-trace))))

(rf/reg-sub
  ::low-level-trace
  ;; TODO: filter from traces panel
  ;; TODO: eventually drop these low level traces after computing the state we need from them.
  :<- [::root]
  (fn [{:keys [low-level-trace]} _]
    low-level-trace))

(rf/reg-sub
  ::debug?
  :<- [::root]
  (fn [{:keys [debug?]} _]
    debug?))

(rf/reg-sub
  ::app-db-follows-events?
  :<- [::root]
  (fn [{:keys [app-db-follows-events?]} _]
    app-db-follows-events?))

(rf/reg-sub
  ::ambiance
  :<- [::root]
  (fn [{:keys [ambiance]} _]
    ambiance))

(rf/reg-sub
  ::syntax-color-scheme
  :<- [::root]
  (fn [{:keys [syntax-color-scheme]} _]
    syntax-color-scheme))

(rf/reg-sub
  ::show-event-history?
  :<- [::root]
  (fn [{:keys [show-event-history?]} _]
    show-event-history?))

(rf/reg-sub
  ::open-new-inspectors?
  :<- [::root]
  (fn [{:keys [open-new-inspectors?]} _]
    open-new-inspectors?))

(rf/reg-sub
 ::handle-keys?
 :<- [::root]
 (fn [{:keys [handle-keys?]} _]
   handle-keys?))

(rf/reg-sub
 ::ready-to-bind-key
 :<- [::root]
 (fn [{:keys [ready-to-bind-key]} _]
   ready-to-bind-key))

(rf/reg-sub
 ::key-bindings
 :<- [::root]
 (fn [{:keys [key-bindings]} [_ k]]
   (if k
     (get key-bindings k)
     key-bindings)))

(rf/reg-sub
 ::log-outputs
 :<- [::root]
 (fn [{:keys [log-outputs]} [_ k]]
   (if k
     (get log-outputs k)
     log-outputs)))

(rf/reg-sub
 ::log-pretty?
 :<- [::root]
 (fn [{:keys [log-pretty?]} _]
   log-pretty?))

