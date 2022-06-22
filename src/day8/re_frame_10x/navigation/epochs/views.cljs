(ns day8.re-frame-10x.navigation.epochs.views
  (:require
    [day8.re-frame-10x.inlined-deps.reagent.v1v0v0.reagent.core   :as r]
    [day8.re-frame-10x.inlined-deps.reagent.v1v0v0.reagent.dom    :as rdom]
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.inlined-deps.spade.git-sha-93ef290.core    :refer [defclass]]
    [day8.re-frame-10x.components.buttons                         :as buttons]
    [day8.re-frame-10x.components.re-com                          :as rc]
    [day8.re-frame-10x.material                                   :as material]
    [day8.re-frame-10x.components.cljs-devtools                   :as cljs-devtools]
    [day8.re-frame-10x.navigation.epochs.events                   :as epochs.events]
    [day8.re-frame-10x.navigation.epochs.subs                     :as epochs.subs]
    [day8.re-frame-10x.panels.settings.events                     :as settings.events]
    [day8.re-frame-10x.panels.settings.subs                       :as settings.subs]
    [day8.re-frame-10x.styles                                     :as styles]))

(defclass epoch-style
  [_ active?]
  {:cursor (if (not active?) :pointer :default)})


(defclass epoch-chevron-style
  [_ active? hover?]
  {:background-color (when (or active? hover?) styles/nord13)}
  [:svg :path
   {:fill (if (or active? hover?) :#fff styles/nord4)}]
  [:&:hover
   [:svg :path
    {:fill (if (or active? hover?) :#fff :#fff)}]])

(defclass epoch-data-style
  [ambiance]
  {:background-color (if (= ambiance :bright) :#fff styles/nord0)})

(defn epoch
  []
  (let [hover?     (r/atom false)
        active?    (r/atom false)]
    (r/create-class
      {:component-did-mount
       (fn [this]
         (when @active?
           (rf/dispatch [::epochs.events/scroll-into-view-debounced (rdom/dom-node this)])))

       :component-did-update
       (fn [this]
         (when @active?
           (rf/dispatch [::epochs.events/scroll-into-view-debounced (rdom/dom-node this)])))

       :reagent-render
       (fn [event id]
         (let [ambiance   @(rf/subscribe [::settings.subs/ambiance])
               current-id @(rf/subscribe [::epochs.subs/selected-epoch-id])]
           (reset! active? (= id current-id))
           [rc/h-box
            :class      (epoch-style ambiance @active?)
            :align      :start
            :min-height styles/gs-19s
            :attr       {:on-click       #(when-not @active? (rf/dispatch [::epochs.events/load id]))
                         :on-mouse-enter #(reset! hover? true)
                         :on-mouse-leave #(reset! hover? false)}
            :children
            [[rc/box
              :class  (epoch-chevron-style ambiance @active? @hover?)
              :height styles/gs-19s
              :align  :center
              :child
              [material/chevron-right
               {:size "17px"}]]
             [rc/gap-f :size styles/gs-2s]
             [rc/box
              :class      (epoch-data-style ambiance)
              :min-height styles/gs-19s
              :size       "1"
              :child      [cljs-devtools/simple-render event [id]]]]]))})))

(defclass epochs-style
  [ambiance]
  {:composes     (styles/background ambiance)
   #_#_:padding-left styles/gs-2s})

(defn epochs
  []
  (let [ambiance   @(rf/subscribe [::settings.subs/ambiance])
        all-events @(rf/subscribe [::epochs.subs/events-by-id])]
    [rc/v-box
     :class    (epochs-style ambiance)
     :width    "100%"
     :children (into [[rc/gap-f :size styles/gs-2s]]
                     (for [[id event] (reverse all-events)
                           :when (not-empty event)]
                       [:<>
                        [epoch event id]
                        [rc/gap-f :size styles/gs-2s]]))]))



(defn prev-button
  []
  (let [older-epochs-available? @(rf/subscribe [::epochs.subs/older-epochs-available?])]
    [buttons/icon
     {:icon      [material/arrow-left]
      :title     (if older-epochs-available? "Previous epoch" "There are no previous epochs")
      :disabled? (not older-epochs-available?)
      :on-click  #(rf/dispatch [::epochs.events/previous])}]))

(defn next-button
  []
  (let [newer-epochs-available? @(rf/subscribe [::epochs.subs/newer-epochs-available?])]
    [buttons/icon
     {:icon      [material/arrow-right]
      :title     (if newer-epochs-available? "Next epoch" "There are no later epochs")
      :disabled? (not newer-epochs-available?)
      :on-click  #(rf/dispatch [::epochs.events/next])}]))

(defn latest-button
  []
  (let [newer-epochs-available? @(rf/subscribe [::epochs.subs/newer-epochs-available?])]
    [buttons/icon
     {:icon      [material/skip-next]
      :title     (if newer-epochs-available? "Skip to latest epoch" "Already showig latest epoch")
      :disabled? (not newer-epochs-available?)
      :on-click  #(rf/dispatch [::epochs.events/most-recent])}]))

(defn left-buttons
  []
  [rc/h-box
   :size     "1"
   :gap      styles/gs-12s
   :align    :center
   :children [[prev-button]
              [next-button]
              [latest-button]]])

(defn ambiance-button
  []
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [buttons/icon
     {:icon (if (= ambiance :bright)
              [material/light-mode]
              [material/dark-mode])
      :title (if (= ambiance :bright)
               "Dark ambiance"
               "Bright ambiance")
      :on-click #(rf/dispatch [::settings.events/set-ambiance (if (= ambiance :bright) :dark :bright)])}]))
