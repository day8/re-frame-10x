(ns day8.re-frame-10x.view.container
  (:require-macros [day8.re-frame-10x.utils.macros :as macros])
  (:require [day8.re-frame-10x.inlined-deps.re-frame.v0v10v9.re-frame.core :as rf]
            [re-frame.db :as db]
            [day8.re-frame-10x.view.event :as event]
            [day8.re-frame-10x.view.app-db :as app-db]
            [day8.re-frame-10x.view.subs :as subs]
            [day8.re-frame-10x.view.views :as views]
            [day8.re-frame-10x.view.traces :as traces]
            [day8.re-frame-10x.view.fx :as fx]
            [day8.re-frame-10x.view.parts :as parts]
            [day8.re-frame-10x.view.timing :as timing]
            [day8.re-frame-10x.view.debug :as debug]
            [day8.re-frame-10x.view.settings :as settings]
            [day8.re-frame-10x.inlined-deps.garden.v1v3v9.garden.core :refer [css style]]
            [day8.re-frame-10x.inlined-deps.garden.v1v3v9.garden.units :refer [px]]
            [day8.re-frame-10x.view.history :as history]
            [day8.re-frame-10x.svgs :as svgs]
            [re-frame.trace]
            [day8.re-frame-10x.utils.re-com :as rc]
            [day8.re-frame-10x.common-styles :as common]
            [day8.re-frame-10x.utils.pretty-print-condensed :as pp]))

(defn tab-button
  [panel-id title]
  (let [selected-tab @(rf/subscribe [:settings/selected-tab])]
    [rc/v-box
     :style {:margin-bottom "-8px"
             :z-index       1}
     :children [[rc/button
                 :class (str "tab " (when (= selected-tab panel-id) "active"))
                 :label title
                 :on-click #(rf/dispatch [:settings/selected-tab panel-id])]
                [svgs/triangle-down
                 :style {:opacity (if (= selected-tab panel-id) "1" "0")}]]]))

(def outer-margins {:margin (str "0px " common/gs-19s)})


(def container-styles
  [:#--re-frame-10x--
   [:.container--replay-button
    {:width  "65px"   ;; common/gs-81s - 2 * (7px padding + 1px border)
     :height "29px"}] ;; common/gs-31s - 2 * 1px border
   [:.container--info-button
    {:border-radius    "50%"
     :color            "white"
     :background-color common/blue-modern-color
     :width            common/gs-12s
     :height           common/gs-12s}]
   [:.pulse-previous
    {:animation-duration "1000ms"
     :animation-name     "pulse-previous-re-frame-10x"}]    ;; Defined in day8.re-frame-10x.styles/at-keyframes
   [:.pulse-next
    {:animation-duration "1000ms"
     :animation-name     "pulse-next-re-frame-10x"}]])



(defn right-hand-buttons [external-window?]
  (let [selected-tab      (rf/subscribe [:settings/selected-tab])
        showing-settings? (= @selected-tab :settings)]
    [rc/h-box
     :align :center
     :children [(when showing-settings?
                  [:button {:class    "bm-active-button"
                            :on-click #(rf/dispatch [:settings/toggle-settings])} "Done"])
                [rc/button
                 :attr     {:title "Settings"}
                 :style    {:width "40px"}
                 :label    [svgs/settings
                            :fill (if showing-settings? "#F2994A" "white")]
                 :on-click #(rf/dispatch [:settings/toggle-settings])]
                (when-not external-window?
                  [rc/button
                   :attr     {:title "Pop out"}
                   :style    {:width "40px"}
                   :on-click #(rf/dispatch-sync [:global/launch-external])
                   :label    [svgs/open-external]])]]))

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


(defn event-name
  []
  (let [direction          @(rf/subscribe [:component/direction])
        current-event      @(rf/subscribe [:epochs/current-event])
        beginning-trace-id @(rf/subscribe [:epochs/beginning-trace-id])
        event-str          (if (some? current-event)
                             (pp/truncate 400 :end current-event)
                             "No event")]
    ^{:key beginning-trace-id}
    [rc/v-box
     :size "auto"
     :style {:max-height       "42px"                       ;42 is exactly 2 lines which is perhaps neater than common/gs-50s (which would allow 3 lines to be seen)
             :overflow-x       "hidden"
             :overflow-y       "auto"
             :background-color common/standard-background-color
             :font-style       (if (some? current-event) "normal" "italic")}
     :children [[:span
                 {:class (str "event-header dont-break-out " (if (= :previous direction) "pulse-previous" "pulse-next"))
                  :style {:position "relative"}}
                 event-str]]]))


(defn standard-header [external-window?]
  (let [older-epochs-available? @(rf/subscribe [:epochs/older-epochs-available?])
        newer-epochs-available? @(rf/subscribe [:epochs/newer-epochs-available?])
        showing-history?        @(rf/subscribe [:history/showing-history?])]
    [[rc/h-box
      :align :center
      :size "auto"
      :gap common/gs-12s
      :children [[:span.arrow.epoch-nav
                  (if older-epochs-available?
                    {:on-click #(do (rf/dispatch [:component/set-direction :previous])
                                    (rf/dispatch [:epochs/previous-epoch]))
                     :title    "Previous epoch"}
                    {:class "arrow__disabled"
                     :style {:cursor "not-allowed"}
                     :title "There are no previous epochs"})
                  [svgs/left
                   :fill (if older-epochs-available? "#6EC0E6" "#cfd8de")]]
                 [event-name]
                 [:span.arrow.epoch-nav
                  (if newer-epochs-available?
                    {:on-click #(do (rf/dispatch [:component/set-direction :next])
                                    (rf/dispatch [:epochs/next-epoch]))
                     :title    "Next epoch"}
                    {:class "arrow__disabled"
                     :style {:cursor "not-allowed"}
                     :title "There are no later epochs"})
                  [svgs/right
                   :fill (if newer-epochs-available? "#6EC0E6" "#cfd8de")]]
                 [rc/v-box
                  :gap common/gs-5s
                  :children
                  [[:span.arrow.epoch-aux-nav
                    (if newer-epochs-available?
                      {:on-click #(do (rf/dispatch [:component/set-direction :next])
                                      (rf/dispatch [:epochs/most-recent-epoch]))
                       :style {:cursor "pointer"}
                       :title    "Skip to latest epoch"}
                      {:class "arrow__disabled"
                       :style {:cursor "not-allowed"}
                       :title "Already showing latest epoch"})
                    [svgs/skip-to-end
                     :fill (if newer-epochs-available? "#6EC0E6" "#cfd8de")]]
                   [:span.arrow.epoch-aux-nav
                    {:on-click #(rf/dispatch [:history/toggle-history])
                     :title    "Show event history"}
                    (if showing-history?
                      [svgs/up-arrow]
                      [svgs/down-arrow])]]]]]
     [rc/gap-f :size common/gs-12s]
     [rc/line :size "2px" :color common/sidebar-heading-divider-color]
     [right-hand-buttons external-window?]]))

(defn devtools-inner [opts]
  (let [selected-tab      (rf/subscribe [:settings/selected-tab])
        panel-type        (:panel-type opts)
        external-window?  (= panel-type :popup)
        unloading?        (rf/subscribe [:global/unloading?])
        popup-failed?     @(rf/subscribe [:errors/popup-failed?])
        showing-settings? (= @selected-tab :settings)
        current-event     @(rf/subscribe [:epochs/current-event])
        showing-history?  @(rf/subscribe [:history/showing-history?])]
    [rc/v-box
     :class "panel-content"
     :width "100%"
     :style {:background-color common/standard-background-color}
     :children [(if showing-settings?
                  [rc/h-box
                   :class "panel-content-top nav"
                   :style {:padding "0px 19px"}
                   :children (settings-header external-window?)]
                  [rc/h-box
                   :class "panel-content-top nav"
                   :style {:padding "0px 19px"}
                   :children (standard-header external-window?)])
                (when showing-history?
                  [history/render])
                (when-not showing-settings?
                  [rc/h-box
                   :class "panel-content-tabs"
                   :justify :between
                   :children [[rc/h-box
                               ;:gap "7px"
                               :align :end
                               :height "50px"
                               :children [[tab-button :event "Event"]
                                          [tab-button :fx "fx"]
                                          [tab-button :app-db "app-db"]
                                          [tab-button :subs "Subs"]
                                          (when (:debug? opts)
                                            [tab-button :parts "Parts"])
                                          ;[tab-button :views "Views"]
                                          [tab-button :traces "Trace"]
                                          [tab-button :timing "Timing"]
                                          (when (:debug? opts)
                                            [tab-button :debug "Debug"])]]
                              (when (some? current-event)
                                [rc/h-box
                                 :align :center
                                 :padding "0px 19px 0px 7px"
                                 :gap "4px"
                                 :children [[rc/button
                                             :class "bm-muted-button container--replay-button"
                                             :label [rc/h-box
                                                     :align :center
                                                     :gap "3px"
                                                     :children [[svgs/reload]
                                                                "replay"]]
                                             :on-click #(do (rf/dispatch [:component/set-direction :next])
                                                            (rf/dispatch [:epochs/replay]))]
                                            [rc/hyperlink-info "https://github.com/day8/re-frame-10x/blob/master/docs/HyperlinkedInformation/ReplayButton.md"]]])]])
                [rc/line :color "#EEEEEE"]
                (when (and external-window? @unloading?)
                  [:h1.host-closed "Host window has closed. Reopen external window to continue tracing."])
                (when-not (re-frame.trace/is-trace-enabled?)
                  [:h1.host-closed {:style {:word-wrap "break-word"}} "Tracing is not enabled. Please set "
                   ;; Note this Closure define is in re-frame, not re-frame-10x
                   [:pre "{\"re_frame.trace.trace_enabled_QMARK_\" true}"] " in " [:pre ":closure-defines"]])
                (when (and (not external-window?) popup-failed?)
                  [:h1.errors "Couldn't open external window. Check if popups are allowed?"
                   [rc/hyperlink
                    :label "Dismiss"
                    :on-click #(rf/dispatch [:errors/dismiss-popup-failed])]])
                [rc/box
                 :v-scroll :on
                 :height "100%"
                 :style {:margin-left common/gs-19s}
                 :child (case @selected-tab
                          :event [event/render]
                          :fx [fx/render]
                          :app-db [app-db/render db/app-db]
                          :subs [subs/render]
                          :views [views/render]
                          :parts [parts/render]
                          :timing [timing/render]
                          :traces [traces/render]
                          :debug [debug/render]
                          :settings [settings/render]
                          [app-db/render db/app-db])]]]))