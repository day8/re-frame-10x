(ns day8.re-frame.trace.components.container
  (:require-macros [day8.re-frame.trace.utils.macros :as macros])
  (:require [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [re-frame.db :as db]
            [day8.re-frame.trace.panels.app-db :as app-db]
            [day8.re-frame.trace.panels.subvis :as subvis]
            [day8.re-frame.trace.panels.traces :as traces]
            [day8.re-frame.trace.panels.subs :as subs]
            [re-frame.trace]
            [reagent.core :as r]
            [day8.re-frame.trace.utils.re-com :as rc]))

(defn tab-button [panel-id title]
  (let [selected-tab @(rf/subscribe [:settings/selected-tab])]
    [:button {:class    (str "tab button " (when (= selected-tab panel-id) "active"))
              :on-click #(rf/dispatch [:settings/selected-tab panel-id])} title]))

(def reload (macros/slurp-macro "day8/re_frame/trace/images/reload.svg"))
(def reload-disabled (macros/slurp-macro "day8/re_frame/trace/images/reload-disabled.svg"))
(def open-external (macros/slurp-macro "day8/re_frame/trace/images/open-external.svg"))
(def snapshot (macros/slurp-macro "day8/re_frame/trace/images/snapshot.svg"))
(def snapshot-ready (macros/slurp-macro "day8/re_frame/trace/images/snapshot-ready.svg"))

(defn devtools-inner [traces opts]
  (let [selected-tab     (rf/subscribe [:settings/selected-tab])
        panel-type       (:panel-type opts)
        external-window? (= panel-type :popup)
        unloading?       (rf/subscribe [:global/unloading?])
        snapshot-ready?  (rf/subscribe [:snapshot/snapshot-ready?])]
    [:div.panel-content
     {:style {:width "100%" :display "flex" :flex-direction "column"}}
     [rc/h-box
      :class "panel-content-top nav"
      :justify :between
      :children
      [[rc/h-box
        :children
        [(tab-button :traces "Traces")
         (tab-button :app-db "App DB")
         (tab-button :subs "Subs")
         #_(tab-button :subvis "SubVis")
         ]]
       [rc/h-box
        :align :center
        :children
        [[:img.nav-icon
          {:title    "Load app-db snapshot"
           :class    (when-not @snapshot-ready? "inactive")
           :src      (str "data:image/svg+xml;utf8,"
                          (if @snapshot-ready?
                            reload
                            reload-disabled))
           :on-click #(rf/dispatch-sync [:snapshot/load-snapshot])}]
         [:img.nav-icon
          {:title    "Snapshot app-db"
           :class    (when @snapshot-ready? "active")
           :src      (str "data:image/svg+xml;utf8,"
                          (if @snapshot-ready?
                            snapshot-ready
                            snapshot))
           :on-click #(rf/dispatch-sync [:snapshot/save-snapshot])}]
         (when-not external-window?
           [:img.nav-icon.active
            {:src      (str "data:image/svg+xml;utf8,"
                            open-external)
             :on-click #(rf/dispatch-sync [:global/launch-external])}])]]]]
     (when (and external-window? @unloading?)
       [:h1.host-closed "Host window has closed. Reopen external window to continue tracing."])
     (when-not (re-frame.trace/is-trace-enabled?)
       [:h1.host-closed {:style {:word-wrap "break-word"}} "Tracing is not enabled. Please set " [:pre "{\"re_frame.trace.trace_enabled_QMARK_\" true}"] " in " [:pre ":closure-defines"]])
     (case @selected-tab
       :traces [traces/render-trace-panel traces]
       :app-db [app-db/render-state db/app-db]
       :subvis [subvis/render-subvis traces]
       :subs [subs/subs-panel]
       [app-db/render-state db/app-db])]))
