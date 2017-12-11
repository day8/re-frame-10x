(ns day8.re-frame.trace
  (:require [day8.re-frame.trace.view.app-db :as app-db]
            [day8.re-frame.trace.styles :as styles]
            [day8.re-frame.trace.view.components :as components]
            [day8.re-frame.trace.view.container :as container]
            [day8.re-frame.trace.utils.localstorage :as localstorage]
            [day8.re-frame.trace.events :as events]
            [day8.re-frame.trace.subs]
            [day8.re-frame.trace.db :as trace.db]
            [re-frame.trace :as trace :include-macros true]
            [re-frame.db :as db]
            [cljs.pprint :as pprint]
            [clojure.string :as str]
            [clojure.set :as set]
            [reagent.core :as r]
            [reagent.interop :refer-macros [$ $!]]
            [reagent.impl.util :as util]
            [reagent.impl.component :as component]
            [reagent.impl.batching :as batch]
            [reagent.ratom :as ratom]
            [goog.object :as gob]
            [re-frame.interop :as interop]
            [devtools.formatters.core :as devtools]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]))


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

(def static-fns
  {:render
   (fn render []
     (this-as c
       (trace/with-trace {:op-type   :render
                          :tags      {:component-path (component-path c)}
                          :operation (last (str/split (component-path c) #" > "))}
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


(defn monkey-patch-reagent []
  (let [#_#_real-renderer reagent.impl.component/do-render
        real-custom-wrapper reagent.impl.component/custom-wrapper
        real-next-tick      reagent.impl.batching/next-tick
        real-schedule       reagent.impl.batching/schedule]


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

    #_(set! reagent.impl.batching/next-tick (fn [f]
                                              (real-next-tick (fn []
                                                                (trace/with-trace {:op-type :raf}
                                                                                  (f))))))

    #_(set! reagent.impl.batching/schedule schedule
            #_(fn []
                (reagent.impl.batching/do-after-render (fn [] (trace/with-trace {:op-type :raf-end})))
                (real-schedule)))))


(defn init-tracing!
  "Sets up any initial state that needs to be there for tracing. Does not enable tracing."
  []
  (monkey-patch-reagent))


(defn resizer-style [draggable-area]
  {:position "absolute" :z-index 2 :opacity 0
   :left     (str (- (/ draggable-area 2)) "px") :width "10px" :height "100%" :top "0px" :cursor "col-resize"})

(def ease-transition "left 0.2s ease-out, top 0.2s ease-out, width 0.2s ease-out, height 0.2s ease-out")

(defn devtools-outer [traces opts]
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
      {:component-did-mount   (fn []
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
                                     [:div.panel-resizer {:style         (resizer-style draggable-area)
                                                          :on-mouse-down #(reset! dragging? true)}]
                                     [container/devtools-inner traces opts]]]))})))


(defn panel-div []
  (let [id    "--re-frame-trace--"
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
  (r/render [devtools-outer events/traces {:panel-type :inline}] (panel-div)))

(defn init-db! []
  (trace.db/init-db))
