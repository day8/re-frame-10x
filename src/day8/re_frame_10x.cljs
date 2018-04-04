(ns day8.re-frame-10x
  (:require [day8.re-frame-10x.styles :as styles]
            [day8.re-frame-10x.view.container :as container]
            [day8.re-frame-10x.subs]
            [day8.re-frame-10x.events]
            [day8.re-frame-10x.db :as trace.db]
            [re-frame.trace :as trace :include-macros true]
            [clojure.string :as str]
            [reagent.interop :refer-macros [$ $!]]
            [reagent.impl.util :as util]
            [reagent.impl.component :as component]
            [reagent.impl.batching :as batch]
            [reagent.ratom :as ratom]
            [goog.object :as gob]
            [re-frame.interop :as interop]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [mranderson047.reagent.v0v7v0.reagent.core :as r]))

(goog-define debug? false)

;; from https://github.com/reagent-project/reagent/blob/3fd0f1b1d8f43dbf169d136f0f905030d7e093bd/src/reagent/impl/component.cljs#L274
(defn fiber-component-path [fiber]
  (let [name   (some-> fiber
                       ($ :type)
                       ($ :displayName))
        parent (some-> fiber
                       ($ :return))
        path   (some-> parent
                       fiber-component-path
                       (str " > "))
        res    (str path name)]
    (when-not (empty? res) res)))

(defn component-path [c]
  ;; Alternative branch for React 16
  (if-let [fiber (some-> c ($ :_reactInternalFiber))]
    (fiber-component-path fiber)
    (component/component-path c)))

(defn comp-name [c]
  (let [n (or (component-path c)
              (some-> c .-constructor util/fun-name))]
    (if-not (empty? n)
      n
      "")))

(def operation-name (memoize (fn [component] (last (str/split (component-path component) #" > ")))))

(def static-fns
  {:render
   (fn mp-render []                                         ;; Monkeypatched render
     (this-as c
       (trace/with-trace {:op-type   :render
                          :tags      {:component-path (component-path c)}
                          :operation (operation-name c)}
                         (if util/*non-reactive*
                           (reagent.impl.component/do-render c)
                           (let [rat        ($ c :cljsRatom)
                                 _          (batch/mark-rendered c)
                                 res        (if (nil? rat)
                                              (ratom/run-in-reaction #(reagent.impl.component/do-render c) c "cljsRatom"
                                                                     batch/queue-render reagent.impl.component/rat-opts)
                                              (._run rat false))
                                 cljs-ratom ($ c :cljsRatom)] ;; actually a reaction
                             (trace/merge-trace!
                               {:tags {:reaction      (interop/reagent-id cljs-ratom)
                                       :input-signals (when cljs-ratom
                                                        (map interop/reagent-id (gob/get cljs-ratom "watching" :none)))}})
                             res)))))})


(defonce real-custom-wrapper reagent.impl.component/custom-wrapper)
(defonce real-next-tick reagent.impl.batching/next-tick)
(defonce real-schedule reagent.impl.batching/schedule)
(defonce do-after-render-trace-scheduled? (atom false))

(defn monkey-patch-reagent []
  (let [#_#_real-renderer reagent.impl.component/do-render
        ]


    #_(set! reagent.impl.component/do-render
            (fn [c]
              (let [name (comp-name c)]
                (js/console.log c)
                (trace/with-trace {:op-type   :render
                                   :tags      {:component-path (component-path c)}
                                   :operation (last (str/split name #" > "))}
                                  (real-renderer c)))))

    (set! reagent.impl.component/static-fns static-fns)

    (set! reagent.impl.component/custom-wrapper
          (fn [key f]
            (case key
              :componentWillUnmount
              (fn [] (this-as c
                       (trace/with-trace {:op-type   key
                                          :operation (last (str/split (comp-name c) #" > "))
                                          :tags      {:component-path (component-path c)
                                                      :reaction       (interop/reagent-id ($ c :cljsRatom))}})
                       (.call (real-custom-wrapper key f) c c)))

              (real-custom-wrapper key f))))

    (set! reagent.impl.batching/next-tick
          (fn [f]
            ;; Schedule a trace to be emitted after a render if there is nothing else scheduled after that render.
            ;; This signals the end of the epoch.

            #_(swap! do-after-render-trace-scheduled?
                     (fn [scheduled?]
                       (js/console.log "Setting up scheduled after" scheduled?)
                       (if scheduled?
                         scheduled?
                         (do (reagent.impl.batching/do-after-render ;; a do-after-flush would probably be a better spot to put this if it existed.
                               (fn []
                                 (js/console.log "Do after render" reagent.impl.batching/render-queue)
                                 (reset! do-after-render-trace-scheduled? false)
                                 (when (false? (.-scheduled? reagent.impl.batching/render-queue))
                                   (trace/with-trace {:op-type :reagent/quiescent}))))
                             true))))
            (real-next-tick (fn []
                              (trace/with-trace {:op-type :raf}
                                                (f)
                                                (trace/with-trace {:op-type :raf-end})
                                                (when (false? (.-scheduled? reagent.impl.batching/render-queue))
                                                  (trace/with-trace {:op-type :reagent/quiescent}))

                                                )))))

    #_(set! reagent.impl.batching/schedule
            (fn []
              (reagent.impl.batching/do-after-render
                (fn []
                  (when @do-after-render-trace-scheduled?
                    (trace/with-trace {:op-type :do-after-render})
                    (reset! do-after-render-trace-scheduled? false))))
              (real-schedule)))))


(defn init-tracing!
  "Sets up any initial state that needs to be there for tracing. Does not enable tracing."
  []
  (monkey-patch-reagent))


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
        handle-window-resize (fn [e]
                               ;; N.B. I don't think this should be a perf bottleneck.
                               (reset! window-width js/window.innerWidth))
        handle-keys          (fn [e]
                               (let [combo-key?      (or (.-ctrlKey e) (.-metaKey e) (.-altKey e))
                                     tag-name        (.-tagName (.-target e))
                                     key             (.-key e)
                                     entering-input? (contains? #{"INPUT" "SELECT" "TEXTAREA"} tag-name)]
                                 (when (and (not entering-input?) combo-key?)
                                   (cond
                                     (and (= key "h") (.-ctrlKey e))
                                     (do (rf/dispatch [:settings/user-toggle-panel])
                                         (.preventDefault e))))))
        handle-mousemove     (fn [e]
                               (when @dragging?
                                 (let [x                (.-clientX e)
                                       y                (.-clientY e)
                                       new-window-width js/window.innerWidth]
                                   (.preventDefault e)
                                   (rf/dispatch [:settings/panel-width% (/ (- new-window-width x) new-window-width)])
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
                                   [:div.panel-wrapper
                                    {:style {:position "fixed" :width "0px" :height "0px" :top "0px" :left "0px" :z-index 99999999}}
                                    [:div.panel
                                     {:style {:position   "fixed" :z-index 1 :box-shadow "rgba(0, 0, 0, 0.3) 0px 0px 4px" :background "white"
                                              :display    "flex"
                                              :left       left :top "0px" :width (str (* 100 @panel-width%) "%") :height "100%"
                                              :transition transition}}
                                     [:div.panel-resizer (when @showing? {:style         (resizer-style draggable-area)
                                                                          :on-mouse-down #(reset! dragging? true)})]
                                     [container/devtools-inner opts]]]))})))


(defn panel-div []
  (let [id    "--re-frame-10x--"
        panel (.getElementById js/document id)]
    (if panel
      panel
      (let [new-panel (.createElement js/document "div")]
        (.setAttribute new-panel "id" id)
        (.appendChild (.-body js/document) new-panel)
        (js/window.focus new-panel)
        new-panel))))

(defn inject-devtools! []
  (styles/inject-trace-styles js/document)
  (r/render [devtools-outer {:panel-type :inline
                             :debug?     debug?}] (panel-div)))

(defn traced-result [trace-id fragment-id]
  ;; TODO: this is not terribly efficient, figure out how to get the index of the trace directly.
  (let [trace (first (filter #(= trace-id (:id %)) (get-in @mranderson047.re-frame.v0v10v2.re-frame.db/app-db [:traces :all-traces])))]
    (get-in trace [:tags :code fragment-id :result])))

(defn init-db! []
  (trace.db/init-db debug?))

(defn ^:export factory-reset! []
  (rf/dispatch [:settings/factory-reset]))
