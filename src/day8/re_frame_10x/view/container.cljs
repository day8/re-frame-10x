(ns day8.re-frame-10x.view.container
  (:require-macros [day8.re-frame-10x.utils.macros :as macros])
  (:require [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
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
            [mranderson047.garden.v1v3v3.garden.core :refer [css style]]
            [mranderson047.garden.v1v3v3.garden.units :refer [px]]
            [re-frame.trace]
            [day8.re-frame-10x.utils.re-com :as rc]
            [day8.re-frame-10x.common-styles :as common]
            [day8.re-frame-10x.utils.pretty-print-condensed :as pp]))

(def triangle-down (macros/slurp-macro "day8/re_frame_10x/images/triangle-down.svg"))

(defn tab-button
  [panel-id title]
  (let [selected-tab @(rf/subscribe [:settings/selected-tab])]
    [rc/v-box
     :style    {:margin-bottom "-8px"
                :z-index       1}
     :children [[:button {:class    (str "tab button bm-heading-text " (when (= selected-tab panel-id) "active"))
                          :on-click #(rf/dispatch [:settings/selected-tab panel-id])} title]
                [:img {:src   (str "data:image/svg+xml;utf8," triangle-down)
                       :style {:opacity (if (= selected-tab panel-id) "1" "0")}}]]]))

(def open-external (macros/slurp-macro "day8/re_frame_10x/images/logout.svg"))
(def settings-svg (macros/slurp-macro "day8/re_frame_10x/images/wrench.svg"))
(def orange-settings-svg (macros/slurp-macro "day8/re_frame_10x/images/orange-wrench.svg"))
(def reload (macros/slurp-macro "day8/re_frame_10x/images/reload.svg"))
(def reload-disabled (macros/slurp-macro "day8/re_frame_10x/images/reload-disabled.svg"))
(def skip-to-end (macros/slurp-macro "day8/re_frame_10x/images/skip-to-end.svg"))
(def skip-to-end-disabled (macros/slurp-macro "day8/re_frame_10x/images/skip-to-end-disabled.svg"))

(def outer-margins {:margin (str "0px " common/gs-19s)})


(def container-styles
  [:#--re-frame-10x--
   [:.container--replay-button
    {:width  "65px"   ;; common/gs-81s - 2 * (7px padding + 1px border)
     :height "29px"}] ;; common/gs-31s - 2 * 1px border
   [:.container--info-button
    {:border-radius    "50%"
     :color            "white"
     :background-color common/blue-modern-color}]
   [:.pulse-previous
    {:animation-duration "1000ms"
     :animation-name     "pulse-previous-re-frame-10x"}] ;; Defined in day8.re-frame-10x.styles/at-keyframes
   [:.pulse-next
    {:animation-duration "1000ms"
     :animation-name     "pulse-next-re-frame-10x"}]
   ])


(defn right-hand-buttons [external-window?]
  (let [selected-tab      (rf/subscribe [:settings/selected-tab])
        showing-settings? (= @selected-tab :settings)]
    [rc/h-box
     :align    :center
     :children [(when showing-settings?
                  [:button {:class    "bm-active-button"
                            :on-click #(rf/dispatch [:settings/toggle-settings])} "Done"])
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
                    :on-click #(rf/dispatch-sync [:global/launch-external])}])]]))


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
        newer-epochs-available? @(rf/subscribe [:epochs/newer-epochs-available?])]
    [[rc/h-box
      :align    :center
      :size     "auto"
      :gap      common/gs-12s
      :children [[:span.arrow.epoch-nav
                  (if older-epochs-available?
                    {:on-click #(do (rf/dispatch [:component/set-direction :previous])
                                    (rf/dispatch [:epochs/previous-epoch]))
                     :title    "Previous epoch"}
                    {:class "arrow__disabled"
                     :title "There are no previous epochs"})
                  "◀"]
                 [event-name]
                 [:span.arrow.epoch-nav
                  (if newer-epochs-available?
                    {:on-click #(do (rf/dispatch [:component/set-direction :next])
                                    (rf/dispatch [:epochs/next-epoch]))
                     :title    "Next epoch"}
                    {:class "arrow__disabled"
                     :title "There are no later epochs"})
                  "▶"]
                 [:span.arrow.epoch-nav
                  (if newer-epochs-available?
                    {:on-click #(do (rf/dispatch [:component/set-direction :next])
                                    (rf/dispatch [:epochs/most-recent-epoch]))
                     :title    "Skip to latest epoch"}
                    {:class "arrow__disabled"
                     :title "Already showing latest epoch"})
                  [:img
                   {:src      (str "data:image/svg+xml;utf8," (if newer-epochs-available? skip-to-end skip-to-end-disabled))
                    :style    {:cursor        (if newer-epochs-available? "pointer" "default")
                               :height        "12px"
                               :margin-bottom "-1px"}}]]]]
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
     {:style {:width            "100%"
              :display          "flex"
              :flex-direction   "column"
              :background-color common/standard-background-color}}
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
                   [rc/h-box
                    :align    :center
                    :padding  "0px 19px 0px 7px"
                    :gap      "4px"
                    :children [[rc/button
                                :class "bm-muted-button container--replay-button"
                                :label [rc/h-box
                                        :align    :center
                                        :gap      "3px"
                                        :children [[:img
                                                    {:src      (str "data:image/svg+xml;utf8," reload)
                                                     :style    {:cursor "pointer"
                                                                :height "23px"}}]
                                                   "replay"]]
                                :on-click #(do (rf/dispatch [:component/set-direction :next])
                                               (rf/dispatch [:epochs/replay]))]
                               [rc/hyperlink-info "https://github.com/Day8/re-frame-10x/blob/master/docs/HyperlinkedInformation/ReplayButton.md"]]]]])
     [rc/line :color "#EEEEEE"]
     (when (and external-window? @unloading?)
       [:h1.host-closed "Host window has closed. Reopen external window to continue tracing."])
     (when-not (re-frame.trace/is-trace-enabled?)
       [:h1.host-closed {:style {:word-wrap "break-word"}} "Tracing is not enabled. Please set "
        ;; Note this Closure define is in re-frame, not re-frame-10x
        [:pre "{\"re_frame.trace.trace_enabled_QMARK_\" true}"] " in " [:pre ":closure-defines"]])
     [rc/v-box
      :size "auto"
      :style {:margin-left common/gs-19s
              :overflow-y  (if (contains? #{:event :fx :parts :timing :debug :settings} @selected-tab)
                             "auto" "initial")
              ;:overflow    "auto" ;; TODO: Might have to put this back or add scrolling within the panels
              }
      :children [(case @selected-tab
                   :event    [event/render]
                   :fx       [fx/render]
                   :app-db   [app-db/render db/app-db]
                   :subs     [subs/render]
                   :views    [views/render]
                   :parts    [parts/render]
                   :timing   [timing/render]
                   :traces   [traces/render]
                   :debug    [debug/render]
                   :settings [settings/render]
                   [app-db/render db/app-db])]]]))
