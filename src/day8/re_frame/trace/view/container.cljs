(ns day8.re-frame.trace.view.container
  (:require-macros [day8.re-frame.trace.utils.macros :as macros])
  (:require [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [re-frame.db :as db]
            [day8.re-frame.trace.view.app-db :as app-db]
            [day8.re-frame.trace.view.traces :as traces]
            [day8.re-frame.trace.view.subs :as subs]
            [day8.re-frame.trace.view.settings :as settings]
            [re-frame.trace]
            [reagent.core :as r]
            [day8.re-frame.trace.utils.re-com :as rc]
            [day8.re-frame.trace.common-styles :as common]))

(defn tab-button [panel-id title]
  (let [selected-tab @(rf/subscribe [:settings/selected-tab])]
    [:button {:class    (str "tab button bm-heading-text " (when (= selected-tab panel-id) "active"))
              :on-click #(rf/dispatch [:settings/selected-tab panel-id])} title]))

(def open-external (macros/slurp-macro "day8/re_frame/trace/images/open-external.svg"))

(def settings-svg (macros/slurp-macro "day8/re_frame/trace/images/wrench.svg"))
(def pause-svg (macros/slurp-macro "day8/re_frame/trace/images/pause.svg"))

(defn devtools-inner [traces opts]
  (let [selected-tab     (rf/subscribe [:settings/selected-tab])
        panel-type       (:panel-type opts)
        external-window? (= panel-type :popup)
        unloading?       (rf/subscribe [:global/unloading?])
        show-tabs?       (not= @selected-tab :settings)]
    [:div.panel-content
     {:style {:width "100%" :display "flex" :flex-direction "column" :background-color common/standard-background-color}}
     [rc/h-box
      :class "panel-content-top nav"
      :justify :between
      :children
      [[rc/h-box
        :align :center
        :children
        [[:span.arrow "◀"]
         [:span.event-header "[:some-namespace/blah 34 \"Hello\""]
         [:span.arrow "▶"]]]
       [rc/h-box
        :align :center
        :children
        [[:img.nav-icon
          {:title    "Pause"
           :src      (str "data:image/svg+xml;utf8,"
                          pause-svg)
           :on-click #(rf/dispatch [:settings/selected-tab :settings])}]
         [:img.nav-icon
          {:title    "Settings"
           :src      (str "data:image/svg+xml;utf8,"
                          settings-svg)
           :on-click #(rf/dispatch [:settings/toggle-settings])}]
         (when-not external-window?
           [:img.nav-icon.active
            {:src      (str "data:image/svg+xml;utf8,"
                            open-external)
             :on-click #(rf/dispatch-sync [:global/launch-external])}])]]]]
     (when show-tabs?
       [rc/h-box
        :class "panel-content-tabs"
        :justify :between
        :children
        [[rc/h-box
          :align :center
          :children
          [(tab-button :overview "Overview")
           (tab-button :app-db "app-db")
           (tab-button :subs "Subs")
           (tab-button :views "Views")
           (tab-button :traces "Trace")]]
         ]])
     [rc/line :style {:margin "0px 10px"}]
     (when (and external-window? @unloading?)
       [:h1.host-closed "Host window has closed. Reopen external window to continue tracing."])
     (when-not (re-frame.trace/is-trace-enabled?)
       [:h1.host-closed {:style {:word-wrap "break-word"}} "Tracing is not enabled. Please set " [:pre "{\"re_frame.trace.trace_enabled_QMARK_\" true}"] " in " [:pre ":closure-defines"]])
     (case @selected-tab
       :traces [traces/render-trace-panel traces]
       :app-db [app-db/render-state db/app-db]
       :subs [subs/subs-panel]
       :settings [settings/render]
       [app-db/render-state db/app-db])]))
