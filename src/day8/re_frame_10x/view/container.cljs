(ns day8.re-frame-10x.view.container
  (:require-macros
    [day8.re-frame-10x.utils.macros :as macros])
  (:require
    [re-frame.db :as db]
    [re-frame.trace]
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.core :refer [css style]]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units :refer [px]]
    [day8.re-frame-10x.inlined-deps.spade.v1v1v0.spade.core :refer [defclass defglobal]]
    [day8.re-frame-10x.epochs.views :as epochs]
    [day8.re-frame-10x.components :as components]
    [day8.re-frame-10x.app-db.views :as app-db]
    [day8.re-frame-10x.subs.views :as subs]
    [day8.re-frame-10x.view.fx :as fx]
    [day8.re-frame-10x.debug.views :as debug]
    [day8.re-frame-10x.settings.views :as settings]
    [day8.re-frame-10x.material :as material]
    [day8.re-frame-10x.svgs :as svgs]
    [day8.re-frame-10x.utils.re-com :as rc]
    [day8.re-frame-10x.styles :as styles]
    [day8.re-frame-10x.utils.pretty-print-condensed :as pp]
    [day8.re-frame-10x.epochs.subs :as epochs.subs]
    [day8.re-frame-10x.epochs.events :as epochs.events]
    [day8.re-frame-10x.settings.subs :as settings.subs]
    [day8.re-frame-10x.settings.events :as settings.events]

    [day8.re-frame-10x.event.views :as event.views]
    [day8.re-frame-10x.timing.views :as timing.views]
    [day8.re-frame-10x.traces.views :as traces.views]))

(def outer-margins {:margin (str "0px " styles/gs-19s)})

#_(defglobal container-styles
    [:#--re-frame-10x--
     [:.container--replay-button
      {:width  styles/gs-81
       :height "29px"}] ;; styles/gs-31s - 2 * 1px border
     [:.container--info-button
      {:border-radius    "50%"
       :color            "white"
       :background-color styles/blue-modern-color
       :width            styles/gs-12s
       :height           styles/gs-12s}]
     [:.pulse-previous
      {:animation-duration "1000ms"
       :animation-name     "pulse-previous-re-frame-10x"}]    ;; Defined in day8.re-frame-10x.styles/at-keyframes
     [:.pulse-next
      {:animation-duration "1000ms"
       :animation-name     "pulse-next-re-frame-10x"}]])







(defn replay-button
  []
  (let [current-event @(rf/subscribe [::epochs.subs/selected-event])]
    (when (some? current-event)
      [components/icon-button
       {:icon     [material/refresh]
        :label    "replay"
        :title    "replay"
        :on-click #(rf/dispatch [::epochs.events/replay])}])))

(defn replay-help-button
  []
  (let [current-event @(rf/subscribe [::epochs.subs/selected-event])]
    (when (some? current-event)
      [components/hyperlink-info "https://github.com/day8/re-frame-10x/blob/master/docs/HyperlinkedInformation/ReplayButton.md"])))

(defclass tab-button-style
  [ambiance active?]
  {:z-index 1}
  [:.rc-button ;; .tab .tab.active
   {:background     (if (= :bright ambiance)
                      (if active? :#fff styles/nord1)
                      (if active? styles/nord1 styles/nord4))
    :color          (if (= :bright ambiance)
                      (if active? styles/nord0 styles/nord4)
                      (if active? styles/nord5 styles/nord0))
    :padding        [[0 styles/gs-12 (px 3) styles/gs-12]]
    :height         styles/gs-19
    :margin-right   styles/gs-5
    :border-top     [[(px 1) :solid styles/nord3]]
    :border-left    [[(px 1) :solid styles/nord3]]
    :border-right   [[(px 1) :solid styles/nord3]]
    :border-radius  [[(px 3) (px 3) 0 0]]
    :font-size      (px 14)
    :font-family    styles/font-stack
    :font-weight    400}
   (when-not active?
     [:&:hover
      {:cursor           :pointer
       :background-color :#fff
       :color            styles/nord1}])])

(defn tab-button
  [panel-id title]
  (let [ambiance     @(rf/subscribe [::settings.subs/ambiance])
        selected-tab @(rf/subscribe [::settings.subs/selected-tab])
        active?      (= panel-id selected-tab)]
    [rc/v-box
     :height   styles/gs-19s
     :class    (tab-button-style ambiance active?)
     :children [[rc/button
                 :label    title
                 :on-click #(rf/dispatch [::settings.events/selected-tab panel-id])]]]))

(defclass tab-buttons-style
  [ambiance]
  {:composes     (styles/navigation-border-top ambiance)
   :padding-left styles/gs-19})

(defn tab-buttons-left
  [debug?]
  [rc/h-box
   :align    :end
   :height   styles/gs-31s
   :children [[tab-button :event "event"]
              [tab-button :fx "fx"]
              [tab-button :app-db "app-db"]
              [tab-button :subs "subs"]
              [tab-button :traces "traces"]
              [tab-button :timing "timing"]
              (when debug?
                [tab-button :debug "debug"])]])

(defclass tab-buttons-right-style
  [ambiance]
  {:padding [[0 styles/gs-5 0 0]]})

(defn tab-buttons-right
  []
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [rc/h-box
     :class    (tab-buttons-right-style ambiance)
     :align    :center
     :gap      styles/gs-12s
     :children [;; TODO: add 'Replay' text, and smaller icon.
                [replay-button]
                ;; TODO: help smaller than what is currently to indicate Reply button is more important/relationship. e.g. just question mark, no button.
                [replay-help-button]]]))

(defn tab-buttons
  [{:keys [debug?]}]
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [rc/h-box
     :class    (tab-buttons-style ambiance)
     :justify  :between
     :children [[tab-buttons-left debug?]
                [tab-buttons-right]]]))

(defclass warning-style
  [ambiance]
  {:background-color styles/nord13
   :word-wrap        :break-word
   :margin-left      styles/gs-19
   :margin-right     styles/gs-19})

(defn warnings
  [external-window?]
  (let [ambiance   @(rf/subscribe [::settings.subs/ambiance])
        unloading? @(rf/subscribe [:global/unloading?])]
    [:<>
     (when (and external-window? unloading?)
       [:h1
        {:class (warning-style ambiance)}
        "Host window has closed. Reopen external window to continue tracing."])
     (when-not (re-frame.trace/is-trace-enabled?)
       [:h1
        {:class (warning-style ambiance)}
        "Tracing is not enabled. Please set "
        ;; Note this Closure define is in re-frame, not re-frame-10x
        [:pre "{re-frame.trace.trace-enabled? true}"] " in " [:pre ":closure-defines"]])]))

(defclass error-style
  [ambiance]
  {:background-color styles/nord11
   :margin-left      styles/gs-19
   :margin-right     styles/gs-19})

(defn errors
  [external-window?]
  (let [ambiance      @(rf/subscribe [::settings.subs/ambiance])
        popup-failed? @(rf/subscribe [:errors/popup-failed?])]
    (when (and (not external-window?) popup-failed?)
      [:h1
       {:class (error-style ambiance)}
       "Couldn't open external window. Check if popups are allowed?"
       [rc/hyperlink
        :label    "Dismiss"
        :on-click #(rf/dispatch [:errors/dismiss-popup-failed])]])))

(defclass tab-content-style
  [ambiance selected-tab]
  {:color       (if (= :bright ambiance) styles/nord0 styles/nord5)
   :margin-top  styles/gs-19s
   :margin-left styles/gs-19s
   :overflow-y  (if (contains? #{:event :fx :parts :timing :debug :settings} selected-tab)
                  "auto"
                  "initial")})

(defn tab-content
  []
  (let [ambiance     @(rf/subscribe [::settings.subs/ambiance])
        selected-tab @(rf/subscribe [::settings.subs/selected-tab])]
    [rc/v-box
     :class    (tab-content-style ambiance selected-tab)   ;;"tab-wrapper"
     :size     "1"
     :children [(case selected-tab
                  :event    [event.views/panel]
                  :fx       [fx/panel]
                  :app-db   [app-db/panel db/app-db]
                  :subs     [subs/panel]
                  :timing   [timing.views/panel]
                  :traces   [traces.views/panel]
                  :debug    [debug/render]
                  :settings [settings/render]

                  [app-db/panel db/app-db])]]))

(defclass devtools-inner-style
  [ambiance]
  {:composes (styles/colors-0 ambiance)})

(defn devtools-inner [{:keys [panel-type debug?]}]
  (let [ambiance          @(rf/subscribe [::settings.subs/ambiance])
        selected-tab      @(rf/subscribe [::settings.subs/selected-tab])
        external-window?  (= panel-type :popup)
        showing-settings? (= selected-tab :settings)]
    [rc/v-box
     :class    (devtools-inner-style ambiance)
     :width    "100%"
     :children [(if-not showing-settings?
                  [:<>
                   [epochs/navigation external-window?]
                   [tab-buttons {:debug? debug?}]]
                  [settings/navigation external-window?])
                [warnings external-window?]
                [errors external-window?]
                [tab-content]]]))

