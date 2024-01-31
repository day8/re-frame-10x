(ns day8.re-frame-10x.navigation.views
  (:require
   [goog.object :as gobj]
   [re-frame.db :as db]
   [re-frame.trace]
   [reagent.impl.batching :as batching]
   [day8.re-frame-10x.inlined-deps.reagent.v1v2v0.reagent.core :as r]
   [day8.re-frame-10x.inlined-deps.reagent.v1v2v0.reagent.dom :as rdom]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core :as rf]
   [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.core]
   [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units   :refer [px]]
   [day8.re-frame-10x.inlined-deps.spade.git-sha-5197e54.core            :refer [defclass]]
   [day8.re-frame-10x.inlined-deps.spade.git-sha-5197e54.container.dom   :as spade.dom]
   [day8.re-frame-10x.inlined-deps.spade.git-sha-5197e54.react           :as spade.react]
   [day8.re-frame-10x.components.buttons                         :as buttons]
   [day8.re-frame-10x.components.hyperlinks                      :as hyperlinks]
   [day8.re-frame-10x.components.inputs                          :as inputs]
   [day8.re-frame-10x.components.re-com                          :as rc]
   [day8.re-frame-10x.navigation.events                          :as navigation.events]
   [day8.re-frame-10x.navigation.subs                            :as navigation.subs]
   [day8.re-frame-10x.navigation.epochs.events                   :as epochs.events]
   [day8.re-frame-10x.navigation.epochs.subs                     :as epochs.subs]
   [day8.re-frame-10x.navigation.epochs.views                    :as epochs.views]
   [day8.re-frame-10x.panels.app-db.views                        :as app-db.views]
   [day8.re-frame-10x.panels.debug.views                         :as debug.views]
   [day8.re-frame-10x.panels.event.views                         :as event.views]
   [day8.re-frame-10x.panels.fx.views                            :as fx.views]
   [day8.re-frame-10x.panels.settings.events                     :as settings.events]
   [day8.re-frame-10x.panels.settings.subs                       :as settings.subs]
   [day8.re-frame-10x.panels.settings.views                      :as settings.views]
   [day8.re-frame-10x.panels.subs.views                          :as subs.views]
   [day8.re-frame-10x.panels.timing.views                        :as timing.views]
   [day8.re-frame-10x.panels.traces.views                        :as traces.views]
   [day8.re-frame-10x.material                                   :as material]
   [day8.re-frame-10x.styles                                     :as styles]
   [day8.re-frame-10x.tools.shadow-dom                           :as tools.shadow-dom]
   [day8.re-frame-10x.popup :as popup])
  (:require-macros [day8.re-frame-10x.components.re-com :refer [inline-resource]]))

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
      [buttons/icon
       {:icon     [material/refresh]
        :label    "replay"
        :title    "replay"
        :on-click #(rf/dispatch [::epochs.events/replay])}])))

(defn replay-help-button
  []
  (let [current-event @(rf/subscribe [::epochs.subs/selected-event])]
    (when (some? current-event)
      [hyperlinks/info "https://github.com/day8/re-frame-10x/blob/master/docs/HyperlinkedInformation/ReplayButton.md"])))

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
    :border-bottom  :none
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
   :children [[tab-button :event  "event"]
              [tab-button :fx     "fx"]
              [tab-button :app-db "app-db"]
              [tab-button :subs   "subs"]
              [tab-button :traces "traces"]
              [tab-button :timing "timing"]
              (when debug?
                [tab-button :debug "debug"])]])

(defclass tab-buttons-right-style
  [_]
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
  [_]
  {:background-color styles/nord13
   :word-wrap        :break-word
   :margin-left      styles/gs-19
   :margin-right     styles/gs-19})

(defn warnings
  [external-window?]
  (let [ambiance   @(rf/subscribe [::settings.subs/ambiance])
        unloading? @(rf/subscribe [::navigation.subs/unloading?])]
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
  [_]
  {:background-color styles/nord11
   :margin-left      styles/gs-19
   :margin-right     styles/gs-19})

(defn errors
  [external-window?]
  (let [ambiance      @(rf/subscribe [::settings.subs/ambiance])
        popup-failed? @(rf/subscribe [::navigation.subs/popup-failed?])]
    (when (and (not external-window?) popup-failed?)
      [:h1
       {:class (error-style ambiance)}
       "Couldn't open external window. Check if popups are allowed?"
       [rc/hyperlink
        :label    "Dismiss"
        :on-click #(rf/dispatch [::navigation.events/dismiss-popup-failed])]])))

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
                  :fx       [fx.views/panel]
                  :app-db   [app-db.views/panel db/app-db]
                  :subs     [subs.views/panel]
                  :timing   [timing.views/panel]
                  :traces   [traces.views/panel]
                  :debug    [debug.views/render]
                  :settings [settings.views/render]
                  [app-db.views/panel db/app-db])]]))

(defn show-history-button
  []
  (let [show-event-history? (rf/subscribe [::settings.subs/show-event-history?])
        standard-history    (if (nil? @show-event-history?) true @show-event-history?)]
    [buttons/icon
     {:icon     [rc/checkbox
                 :model standard-history
                 :on-change #(rf/dispatch [::settings.events/show-event-history? %])]
      :title    (if standard-history "Hide event history" "Show event history")
      :label    "history"
      :on-click #(rf/dispatch [::settings.events/show-event-history? (not standard-history)])}]))

(defn epoch-filtering
  []
  (let [ambiance            (rf/subscribe [::settings.subs/ambiance])
        show-event-history? (rf/subscribe [::settings.subs/show-event-history?])
        filter-str          (rf/subscribe [::epochs.subs/filter-str])]
    (when-not (= @show-event-history? false)
      [rc/h-box
       :class    (inputs/search-style @ambiance)
       :align    :center
       :children [[material/search]
                  [:input {:type        "text"
                           :default-value       @filter-str
                           :auto-focus  true
                           :placeholder "filter event history"
                           :size        (if (> 20 (count (str @filter-str)))
                                          25
                                          (count (str @filter-str)))
                           :on-change   #(rf/dispatch [::epochs.events/set-filter
                                                       (-> % .-target .-value)])}]
                  (when (seq @filter-str)
                    [buttons/icon
                     {:icon     [material/clear]
                      :title    "Clear epoch filter"
                      :on-click #(rf/dispatch [::epochs.events/set-filter ""])}])]])))

(defn settings-button
  []
  [buttons/icon
   {:icon     [material/settings]
    :title    "Settings"
    :on-click #(rf/dispatch [::settings.events/toggle])}])

(declare mount)

(defn popout-button
  [external-window?]
  (when-not external-window?
    [buttons/icon
     {:icon     [material/open-in-new]
      :title    "Pop out"
      :on-click #(rf/dispatch-sync [::navigation.events/launch-external mount])}]))

(defn hide-panel-button
  [external-window?]
  (when-not external-window?
    [buttons/icon
     {:icon     [material/collapse-right]
      :title    "Hide panel"
      :on-click #(rf/dispatch [::settings.events/user-toggle-panel])}]))

(defclass navigation-style
  [ambiance]
  {:composes (styles/navigation-border-bottom ambiance)}
  [:.rc-label
   {:padding-left styles/gs-19s}])

(defclass devtools-inner-style
  [ambiance]
  {:composes (styles/colors-0 ambiance)})

;; this is the styling to be applied to popup menus
(defclass path-annotations-menu-style
  []
  {}
  [:.goog-menu
   {:position         :absolute
    :background-color :#E5E9F0}])

(defn devtools-inner [{:keys [panel-type debug?]}]
  (let [ambiance            @(rf/subscribe [::settings.subs/ambiance])
        selected-tab        @(rf/subscribe [::settings.subs/selected-tab])
        show-event-history? (rf/subscribe [::settings.subs/show-event-history?])
        external-window?    (= panel-type :popup)
        showing-settings?   (= selected-tab :settings)
        panel-2             [rc/v-box
                             :height "100%"
                             :width "100%"
                             :children [(if showing-settings?
                                          [rc/h-box
                                           :height   "100%"
                                           :width    "100%"
                                           :class    (navigation-style ambiance)
                                           :align    :center
                                           :justify  :between
                                           :height   styles/gs-31s
                                           :gap      styles/gs-19s
                                           :children
                                           [[rc/label :label "Settings"]
                                            [rc/h-box
                                             :gap   styles/gs-12s
                                             :style {:margin-right styles/gs-19s}
                                             :children
                                             [[settings.views/done-button]
                                              [popout-button external-window?]]]]]
                                          [:div])
                                        (if-not showing-settings?
                                          [tab-buttons {:debug? debug?}]
                                          [:div])
                                        [warnings external-window?]
                                        [errors external-window?]
                                        [tab-content]]]]
    [rc/box
     :style {:position "relative"
             :width "100%"
             :height "100%"}
     :attr {:id "re-frame-10x__ui-container"}
     :child
     [:<>
      (if-not showing-settings?
        [rc/v-split
         :class         (str (styles/normalize) " " (devtools-inner-style ambiance) " " (path-annotations-menu-style))
         :height        "100%"
         :width         "100%"
         :debug?        debug?
         :initial-split "10%"
         :margin        "0px"
         :panel-1       [rc/v-box
                         :height "100%"
                         :width "100%"
                         :style {:overflow :auto}
                         :children
                         [[rc/h-box
                           :class  (navigation-style ambiance)
                           :align  :center
                           :height styles/gs-31s
                           :width  "100%"
                           :gap    styles/gs-19s
                           :children
                           [(when-not (= @show-event-history? false)
                              [rc/label :label "Event History"])
                            (if-not (= @show-event-history? false)
                              [epochs.views/left-buttons]
                              [rc/box
                               :size  "1"
                               :child [:div]])
                            [epoch-filtering]
                            [show-history-button]
                            [rc/h-box
                             :gap      styles/gs-12s
                             :style    {:margin-right styles/gs-5s}
                             :children [[settings-button]
                                        [popout-button external-window?]
                                        [hide-panel-button external-window?]]]]]
                          (if-not (= @show-event-history? false)
                            [epochs.views/epochs]
                            [rc/line])]]
         :panel-2 panel-2]
        [rc/box
         :class  (str (styles/normalize) " " (devtools-inner-style ambiance) " " (path-annotations-menu-style))
         :height "100%"
         :width  "100%"
         :child  panel-2])
      [popup/menu]]]))

(defn mount [popup-window popup-document]
  ;; When programming here, we need to be careful about which document and window
  ;; we are operating on, and keep in mind that the window can close without going
  ;; through standard react lifecycle, so we hook the beforeunload event.
  (let [shadow-root              (tools.shadow-dom/shadow-root popup-document "--re-frame-10x--" (inline-resource "day8/re_frame_10x.css"))
        spade-container          (spade.dom/create-container shadow-root)
        resize-update-scheduled? (atom false)
        handle-window-resize     (fn [_]
                                   (when-not @resize-update-scheduled?
                                     (batching/next-tick
                                      (fn []
                                        (let [width  (.-innerWidth popup-window)
                                              height (.-innerHeight popup-window)]
                                          (rf/dispatch [::settings.events/external-window-resize {:width width :height height}]))
                                        (reset! resize-update-scheduled? false)))
                                     (reset! resize-update-scheduled? true)))
        handle-window-position   (let [pos (atom {})]
                                   (fn []
                                     ;; Only update re-frame if the windows position has changed.
                                     (let [{:keys [left top]} @pos
                                           screen-left (.-screenX popup-window)
                                           screen-top  (.-screenY popup-window)]
                                       (when (or (not= left screen-left)
                                                 (not= top screen-top))
                                         (rf/dispatch [::settings.events/external-window-position {:left screen-left :top screen-top}])
                                         (reset! pos {:left screen-left :top screen-top})))))
        window-position-interval (atom nil)
        unmount                  (fn [_]
                                   (.removeEventListener popup-window "resize" handle-window-resize)
                                   (some-> @window-position-interval js/clearInterval)
                                   nil)]
    (gobj/set popup-window "onunload" #(rf/dispatch [::navigation.events/external-closed]))
    (rdom/render
     [(r/create-class
       {:display-name           "devtools outer external"
        :component-did-mount    (fn []
                                  (.addEventListener popup-window "resize" handle-window-resize)
                                  (.addEventListener popup-window "beforeunload" unmount)
                                    ;; Check the window position every 10 seconds
                                  (reset! window-position-interval
                                          (js/setInterval
                                           handle-window-position
                                           2000)))
        :component-will-unmount unmount
        :reagent-render         (fn []
                                  [spade.react/with-style-container spade-container
                                   [devtools-inner {:panel-type :popup}]])})]
     shadow-root)))
