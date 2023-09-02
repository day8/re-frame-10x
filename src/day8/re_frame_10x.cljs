(ns day8.re-frame-10x
  (:require
   [day8.re-frame-10x.inlined-deps.reagent.v1v0v0.reagent.core           :as r]
   [day8.re-frame-10x.inlined-deps.reagent.v1v0v0.reagent.dom            :as rdom]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core         :as rf]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.db]
   [day8.re-frame-10x.inlined-deps.spade.git-sha-93ef290.container.dom   :as spade.dom]
   [day8.re-frame-10x.inlined-deps.spade.git-sha-93ef290.react           :as spade.react]
   [day8.reagent.impl.batching                                           :refer [patch-next-tick]]
   [day8.reagent.impl.component                                          :refer [patch-wrap-funs patch-custom-wrapper]]
   [day8.re-frame-10x.tools.datafy                                       :as tools.datafy]
   [day8.re-frame-10x.tools.shadow-dom                                   :as tools.shadow-dom]
   [day8.re-frame-10x.events                                             :as events]
   [day8.re-frame-10x.components.re-com                                  :as rc]
   [day8.re-frame-10x.navigation.views                                   :as container]
   [day8.re-frame-10x.panels.settings.subs                               :as settings.subs]
   [day8.re-frame-10x.panels.settings.events                             :as settings.events]))

(goog-define debug? false)

#_(defonce real-schedule reagent.impl.batching/schedule)
#_(defonce do-after-render-trace-scheduled? (atom false))

(defn resizer-style [draggable-area]
  {:position "absolute" :z-index 2 :opacity 0
   :left     (str (- (/ draggable-area 2)) "px") :width "10px" :height "100%" :top "0px" :cursor "col-resize"})

(def ease-transition "left 0.2s ease-out, top 0.2s ease-out, width 0.2s ease-out, height 0.2s ease-out")

(defn devtools-outer [opts]
  ;; Add clear button
  ;; Filter out different trace types
  (let [handle-keys?         (rf/subscribe [::settings.subs/handle-keys?])
        panel-width%         (rf/subscribe [::settings.subs/panel-width%])
        showing?             (rf/subscribe [::settings.subs/show-panel?])
        dragging?            (r/atom false)
        window-width         (r/atom js/window.innerWidth)
        panel-key            (rf/subscribe [::settings.subs/key-bindings :show-panel])
        ready-to-bind-key    (rf/subscribe [::settings.subs/ready-to-bind-key])
        handle-window-resize (do (rf/dispatch [::settings.events/window-width js/window.innerWidth]) ;; Set initial
                                 (fn [_]
                                   ;; N.B. I don't think this should be a perf bottleneck.
                                   (let [window-width-val js/window.innerWidth]
                                     (rf/dispatch [::settings.events/window-width window-width-val])
                                     (reset! window-width window-width-val))))
        handle-keys          (fn [e]
                               (let [tag-name        (.-tagName (.-target e))
                                     modifier?       (contains? #{"Shift" "Alt" "Control"} (.-key e))
                                     entering-input? (contains? #{"INPUT" "SELECT" "TEXTAREA"} tag-name)]
                                 (when-not entering-input?
                                   (cond
                                     (and @ready-to-bind-key (not modifier?))
                                     (do (rf/dispatch [::settings.events/bind-key
                                                       @ready-to-bind-key
                                                       (tools.datafy/keyboard-event e)])
                                         (rf/dispatch [::settings.events/ready-to-bind-key nil])
                                         (.preventDefault e))
                                     (and @handle-keys?
                                          (= @panel-key (tools.datafy/keyboard-event e)))
                                     (do (rf/dispatch [::settings.events/user-toggle-panel])
                                         (.preventDefault e))))))
        handle-mousemove     (fn [e]
                               (when @dragging?
                                 (let [x                (.-clientX e)
                                       #_#_y                (.-clientY e)
                                       new-window-width js/window.innerWidth]
                                   (.preventDefault e)
                                   (let [width% (/ (- new-window-width x) new-window-width)]
                                     (when (<= width% 0.9)
                                       (rf/dispatch [::settings.events/panel-width% width%])))
                                   (reset! window-width new-window-width))))
        handle-mouse-up      (fn [_] (reset! dragging? false))]
    (r/create-class
     {:component-did-mount    (fn []
                                (js/window.addEventListener "keydown" handle-keys)
                                (js/window.addEventListener "mousemove" handle-mousemove)
                                (js/window.addEventListener "mouseup" handle-mouse-up)
                                (js/window.addEventListener "resize" handle-window-resize))
      :component-will-unmount (fn []
                                (js/window.removeEventListener "keydown" handle-keys)
                                (js/window.removeEventListener "mousemove" handle-mousemove)
                                (js/window.removeEventListener "mouseup" handle-mouse-up)
                                (js/window.removeEventListener "resize" handle-window-resize))
      :display-name           "devtools outer"
      :reagent-render         (fn []
                                (let [draggable-area 10
                                      left           (if @showing? (str (* 100 (- 1 @panel-width%)) "%")
                                                         (str @window-width "px"))
                                      transition     (if @dragging?
                                                       ""
                                                       ease-transition)]
                                  [rc/box
                                   :width  "0px"
                                   :height "0px"
                                   :style  {:position "fixed"
                                            :top      "0px"
                                            :left     "0px"
                                            :z-index  99999999}
                                   :child [rc/h-box
                                           :width (str (* 100 @panel-width%) "%")
                                           :height "100%"
                                           :style {:position   "fixed"
                                                   :z-index    1
                                                   :box-shadow "rgba(0, 0, 0, 0.3) 0px 0px 4px"
                                                   :background "white"
                                                   :left       left
                                                   :top        "0px"
                                                   :transition transition}
                                           :children [[:div.panel-resizer (when @showing? {:style         (resizer-style draggable-area)
                                                                                           :on-mouse-down #(reset! dragging? true)})]
                                                      [container/devtools-inner opts]]]]))})))

(defn traced-result [trace-id fragment-id]
  ;; TODO: this is not terribly efficient, figure out how to get the index of the trace directly.
  (let [trace (first (filter #(= trace-id (:id %)) (get-in @day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.db/app-db [:traces :all])))]
    (get-in trace [:tags :code fragment-id :result])))

(defn ^:export factory-reset! []
  (rf/dispatch [::settings.events/factory-reset]))

(defn ^:export show-panel! [show-panel?]
  (rf/dispatch [::settings.events/show-panel? show-panel?]))

(defn ^:export handle-keys! [handle-keys?]
  (rf/dispatch [::settings.events/handle-keys? handle-keys?]))

;; --- NEW ---

(defn inject!
  []
  (rf/clear-subscription-cache!)
  (let [shadow-root (tools.shadow-dom/shadow-root js/document "--re-frame-10x--")
        container   (spade.dom/create-container shadow-root)]

    (rdom/render
     [spade.react/with-style-container container
      [devtools-outer
       {:panel-type :inline
        :debug?     debug?}]]
     shadow-root)))

(defn patch!
  "Sets up any initial state that needs to be there for tracing. Does not enable tracing."
  []
  (patch-custom-wrapper)
  (patch-wrap-funs)
  (patch-next-tick))

(defn fix-db-noop []
  (re-frame.core/reg-global-interceptor
   (re-frame.core/->interceptor
    :id :day8.re-frame-10x/internal
    :after #(assoc-in % [:effects :db :day8.re-frame-10x/internal] (gensym)))))

(fix-db-noop)

(defn init!
  []
  (patch!)
  (rf/dispatch-sync [::events/init {:debug? debug?}])
  (inject!))