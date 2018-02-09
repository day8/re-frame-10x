(ns day8.re-frame.trace.view.container
  (:require-macros [day8.re-frame.trace.utils.macros :as macros])
  (:require [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [re-frame.db :as db]
            [day8.re-frame.trace.view.event :as event]
            [day8.re-frame.trace.view.app-db :as app-db]
            [day8.re-frame.trace.view.subs :as subs]
            [day8.re-frame.trace.view.views :as views]
            [day8.re-frame.trace.view.traces :as traces]
            [day8.re-frame.trace.view.timing :as timing]
            [day8.re-frame.trace.view.debug :as debug]
            [day8.re-frame.trace.view.settings :as settings]
            [mranderson047.garden.v1v3v3.garden.core :refer [css style]]
            [mranderson047.garden.v1v3v3.garden.units :refer [px]]
            [re-frame.trace]
            [day8.re-frame.trace.utils.re-com :as rc]
            [day8.re-frame.trace.common-styles :as common]))

(def triangle-down (macros/slurp-macro "day8/re_frame/trace/images/triangle-down.svg"))
(defn tab-button [panel-id title]
  (let [selected-tab @(rf/subscribe [:settings/selected-tab])]
    [rc/v-box
     :style    {:margin-bottom "-8px"
                :z-index       1}
     :children [[:button {:class    (str "tab button bm-heading-text " (when (= selected-tab panel-id) "active"))
                          :on-click #(rf/dispatch [:settings/selected-tab panel-id])} title]
                [:img {:src   (str "data:image/svg+xml;utf8," triangle-down)
                       :style {:opacity (if (= selected-tab panel-id) "1" "0")}}]]]))

(def open-external (macros/slurp-macro "day8/re_frame/trace/images/logout.svg"))
(def settings-svg (macros/slurp-macro "day8/re_frame/trace/images/wrench.svg"))
(def orange-settings-svg (macros/slurp-macro "day8/re_frame/trace/images/orange-wrench.svg"))
(def pause-svg (macros/slurp-macro "day8/re_frame/trace/images/pause.svg"))
(def play-svg (macros/slurp-macro "day8/re_frame/trace/images/play.svg"))

(def outer-margins {:margin (str "0px " common/gs-19s)})



(defn right-hand-buttons [external-window?]
  (let [selected-tab      (rf/subscribe [:settings/selected-tab])
        paused?           (rf/subscribe [:settings/paused?])
        showing-settings? (= @selected-tab :settings)]
    [rc/h-box
     :align    :center
     :children [(when showing-settings?
                  [:button {:class    "bm-active-button"
                            :on-click #(rf/dispatch [:settings/toggle-settings])} "Done"])
                (if @paused?
                  [:img.nav-icon.noselect
                   {:title    "Play"
                    :src      (str "data:image/svg+xml;utf8,"
                                   play-svg)
                    :on-click #(rf/dispatch [:settings/play])}]
                  [:img.nav-icon.noselect
                   {:title    "Pause"
                    :src      (str "data:image/svg+xml;utf8,"
                                   pause-svg)
                    :on-click #(rf/dispatch [:settings/pause])}])
                [:img.nav-icon.noselect
                 {:title    "Settings"
                  :src      (str "data:image/svg+xml;utf8,"
                                 (if showing-settings? orange-settings-svg settings-svg))
                  :on-click #(rf/dispatch [:settings/toggle-settings])}]
                (when-not external-window?
                  [:img.nav-icon.active.noselect
                   {:title    "Pop out"
                    :src      (str "data:image/svg+xml;utf8,"
                                   open-external)
                    :on-click #(rf/dispatch-sync [:global/launch-external])}])]])
  )

(defn settings-header [external-window?]
  [[rc/h-box
    :align :center
    :size "auto"
    :gap common/gs-12s
    :children [[rc/label :class "bm-title-text" :label "Settings"]]]
   ;; TODO: this line needs to be between Done and other buttons
   [rc/gap-f :size common/gs-12s]
   [rc/line :size "2px" :color common/sidebar-heading-divider-color]
   [rc/gap-f :size common/gs-12s]
   [right-hand-buttons external-window?]])

(defn standard-header [external-window?]
  (let [current-event @(rf/subscribe [:epochs/current-event])
        older-epochs-available? @(rf/subscribe [:epochs/older-epochs-available?])
        newer-epochs-available? @(rf/subscribe [:epochs/newer-epochs-available?])]
    [[rc/h-box
      :align    :center
      :size     "auto"
      :gap      common/gs-12s
      :children [[:span.arrow (if older-epochs-available?
                                {:on-click #(rf/dispatch [:epochs/previous-epoch])}
                                {:class "arrow__disabled"}) "◀"]
                 [rc/v-box
                  :size     "auto"
                  :style    {:max-height       "42px" ;42 is exactly 2 lines which is perhaps neater than common/gs-50s (which would allow 3 lines to be seen)
                             :overflow-x       "hidden"
                             :overflow-y       "auto"
                             :background-color "white"}
                  :children [[:span.event-header (subs (prn-str current-event) 0 400)]]]
                 [:span.arrow (if newer-epochs-available?
                                {:on-click #(rf/dispatch [:epochs/next-epoch])}
                                {:class "arrow__disabled"})
                  "▶"]]]
     [rc/gap-f :size common/gs-12s]
     [rc/line :size "2px" :color common/sidebar-heading-divider-color]
     [right-hand-buttons external-window?]]))

(defn devtools-inner [opts]
  (let [selected-tab      (rf/subscribe [:settings/selected-tab])
        panel-type        (:panel-type opts)
        external-window?  (= panel-type :popup)
        unloading?        (rf/subscribe [:global/unloading?])
        showing-settings? (= @selected-tab :settings)]
    [:div.panel-content
     {:style {:width "100%" :display "flex" :flex-direction "column" :background-color common/standard-background-color}}
     (if showing-settings?
       [rc/h-box
        :class    "panel-content-top nav"
        :style    {:padding "0px 19px"}
        :children (settings-header external-window?)]
       [rc/h-box
        :class    "panel-content-top nav"
        :style    {:padding "0px 19px"}
        :children (standard-header external-window?)])
     (when-not showing-settings?
       [rc/h-box
        :class    "panel-content-tabs"
        :justify  :between
        :children [[rc/h-box
                    :gap      "7px"
                    :align    :end
                    :height   "50px"
                    :children [(tab-button :event "Event")
                               (tab-button :app-db "app-db")
                               (tab-button :subs "Subs")
                               ;(tab-button :views "Views")
                               (tab-button :traces "Trace")
                               (tab-button :timing "Timing")
                               (when (:debug? opts)
                                 (tab-button :debug "Debug"))]]]])
     [rc/line :color "#EEEEEE"]
     (when (and external-window? @unloading?)
       [:h1.host-closed "Host window has closed. Reopen external window to continue tracing."])
     (when-not (re-frame.trace/is-trace-enabled?)
       [:h1.host-closed {:style {:word-wrap "break-word"}} "Tracing is not enabled. Please set " [:pre "{\"re_frame.trace.trace_enabled_QMARK_\" true}"] " in " [:pre ":closure-defines"]])
     [rc/v-box
      :size "auto"
      :style {:margin-left common/gs-19s
              :overflow-y  (if (contains? #{:timing :debug :event :subs} @selected-tab)
                             "auto" "initial")
              ;:overflow    "auto" ;; TODO: Might have to put this back or add scrolling within the panels
              }
      :children [(case @selected-tab
                   :event    [event/render]
                   :app-db   [app-db/render db/app-db]
                   :subs     [subs/render]
                   :views    [views/render]
                   :traces   [traces/render]
                   :timing   [timing/render]
                   :debug    [debug/render]
                   :settings [settings/render]
                   [app-db/render db/app-db])]]]))

