(ns day8.re-frame-10x
  (:require
    [day8.re-frame-10x.utils.re-com :as rc]
    [day8.re-frame-10x.styles :as styles]
    [day8.re-frame-10x.view.container :as container]
    [day8.re-frame-10x.subs]
    [day8.re-frame-10x.events]
    [day8.re-frame-10x.db :as trace.db]
    [day8.reagent.impl.component :refer [patch-wrap-funs patch-custom-wrapper]]
    [day8.reagent.impl.batching :refer [patch-next-tick]]
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.inlined-deps.reagent.v1v0v0.reagent.core :as r]
    [day8.re-frame-10x.inlined-deps.reagent.v1v0v0.reagent.dom :as rdom]))

(goog-define debug? false)

#_(defonce real-schedule reagent.impl.batching/schedule)
#_(defonce do-after-render-trace-scheduled? (atom false))

(defn init-tracing!
  "Sets up any initial state that needs to be there for tracing. Does not enable tracing."
  []
  (patch-custom-wrapper)
  (patch-wrap-funs)
  (patch-next-tick))


(defn resizer-style [draggable-area]
  {:position "absolute" :z-index 2 :opacity 0
   :left     (str (- (/ draggable-area 2)) "px") :width "10px" :height "100%" :top "0px" :cursor "col-resize"})

(def ease-transition "left 0.2s ease-out, top 0.2s ease-out, width 0.2s ease-out, height 0.2s ease-out")

(defn devtools-outer [opts]
  ;; Add clear button
  ;; Filter out different trace types
  (let [position             (r/atom :right)
        panel-width%         (rf/subscribe [:settings/panel-width%])
        showing?             (rf/subscribe [:settings/show-panel?])
        dragging?            (r/atom false)
        pin-to-bottom?       (r/atom true)
        selected-tab         (rf/subscribe [:settings/selected-tab])
        window-width         (r/atom js/window.innerWidth)
        handle-window-resize (do (rf/dispatch [:settings/window-width js/window.innerWidth]) ;; Set initial
                                 (fn [e]
                                   ;; N.B. I don't think this should be a perf bottleneck.
                                   (let [window-width-val js/window.innerWidth]
                                     (rf/dispatch [:settings/window-width window-width-val])
                                     (reset! window-width window-width-val))))
        handle-keys          (fn [e]
                               (let [tag-name        (.-tagName (.-target e))
                                     entering-input? (contains? #{"INPUT" "SELECT" "TEXTAREA"} tag-name)]
                                 (when (and (not entering-input?)
                                            (= (.-key e) "h")
                                            (.-ctrlKey e))
                                   (rf/dispatch [:settings/user-toggle-panel])
                                   (.preventDefault e))))
        handle-mousemove     (fn [e]
                               (when @dragging?
                                 (let [x                (.-clientX e)
                                       y                (.-clientY e)
                                       new-window-width js/window.innerWidth]
                                   (.preventDefault e)
                                   (let [width% (/ (- new-window-width x) new-window-width)]
                                     (when (<= width% 0.9)
                                       (rf/dispatch [:settings/panel-width% width%])))
                                   (reset! window-width new-window-width))))
        handle-mouse-up      (fn [e] (reset! dragging? false))]
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
                                    :class "panel-wrapper"
                                    :width "0px"
                                    :height "0px"
                                    :style {:position "fixed"
                                            :top      "0px"
                                            :left     "0px"
                                            :z-index  99999999}
                                    :child [rc/h-box
                                            :class "panel"
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


(defn panel-div []
  (let [id    "--re-frame-10x--"
        panel (.getElementById js/document id)]
    (if panel
      panel
      (let [new-panel (.createElement js/document "div")]
        (.setAttribute new-panel "id" id)
        (.setAttribute new-panel "class" (str
                                           #_(styles/unset) " "
                                           (styles/normalize)))
        (.appendChild (.-body js/document) new-panel)
        (js/window.focus new-panel)
        new-panel))))

(defn inject-devtools! []
  (rdom/render [devtools-outer {:panel-type :inline
                                :debug?     debug?}] (panel-div)))

(defn traced-result [trace-id fragment-id]
  ;; TODO: this is not terribly efficient, figure out how to get the index of the trace directly.
  (let [trace (first (filter #(= trace-id (:id %)) (get-in @day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.db/app-db [:traces :all-traces])))]
    (get-in trace [:tags :code fragment-id :result])))

(defn init-db! []
  (trace.db/init-db debug?))

(defn ^:export factory-reset! []
  (rf/dispatch [:settings/factory-reset]))

(defn ^:export show-panel! [show-panel?]
  (rf/dispatch [:settings/show-panel? show-panel?]))
