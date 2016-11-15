(ns day8.re-frame.trace
  (:require [day8.re-frame.trace.subvis :as subvis]
            [re-frame.tracing :as trace :include-macros true]
            [cljs.pprint :as pprint]
            [clojure.string :as str]
            [reagent.core :as r]
            [reagent.interop :refer-macros [$ $!]]
            [reagent.impl.util :as util]
            [reagent.impl.component :as component]
            [reagent.impl.batching :as batch]
            [reagent.ratom :as ratom]
            [re-frame.utils :as rutils]
            [goog.object :as gob]
            [re-frame.interop :as interop]

            [devtools.formatters.core :as devtools]
            ))

(defn comp-name [c]
  (let [n (or (component/component-path c)
              (some-> c .-constructor util/fun-name))]
    (if-not (empty? n)
      n
      "")))



(def static-fns
  {:render
   (fn render []
     (this-as c
       (trace/with-trace {:op-type   :render
                          :tags      {:component-path (reagent.impl.component/component-path c)}
                          :operation (last (str/split (reagent.impl.component/component-path c) #" > "))}
                         (if util/*non-reactive*
                           (reagent.impl.component/do-render c)
                           (let [rat        ($ c :cljsRatom)
                                 _          (batch/mark-rendered c)
                                 res        (if (nil? rat)
                                              (ratom/run-in-reaction #(reagent.impl.component/do-render c) c "cljsRatom"
                                                                     batch/queue-render reagent.impl.component/rat-opts)
                                              (._run rat false))
                                 cljs-ratom ($ c :cljsRatom)]               ;; actually a reaction
                             (trace/merge-trace!
                               {:tags {:reaction      (rutils/reagent-id cljs-ratom)
                                       :input-signals (when cljs-ratom
                                                        (map rutils/reagent-id (gob/get cljs-ratom "watching" :none)))}})
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
                                   :tags      {:component-path (reagent.impl.component/component-path c)}
                                   :operation (last (str/split name #" > "))}
                                  (real-renderer c)

                                  ))))

    (set! reagent.impl.component/static-fns static-fns)

    (set! reagent.impl.component/custom-wrapper
          (fn [key f]
            (case key
              :componentWillUnmount
              (fn [] (this-as c
                              (trace/with-trace {:op-type   key
                                                 :operation (last (str/split (comp-name c) #" > "))
                                                 :tags      {:component-path (reagent.impl.component/component-path c)
                                                             :reaction       (rutils/reagent-id ($ c :cljsRatom))}})
                              (.call (real-custom-wrapper key f) c c)))

              (real-custom-wrapper key f))))

    #_(set! reagent.impl.batching/next-tick (fn [f]
                                              (real-next-tick (fn []
                                                                (trace/with-trace {:op-type :raf}
                                                                                  (f))))))

    #_(set! reagent.impl.batching/schedule schedule
            #_(fn []
                (reagent.impl.batching/do-after-render (fn [] (trace/with-trace {:op-type :raf-end})))
                (real-schedule)))
    ))

(def traces (interop/ratom []))
(defn log-trace? [trace]
  (let [render-type? (= (:type trace) :render)]
    (not (and render-type?
              (str/includes? (or (get-in trace [:tags :component-path]) "") "day8.re_frame.trace.devtools")))))

(defn init-tracing! []
  (re-frame.tracing/register-trace-cb ::cb (fn [new-traces]
                                             (let [new-traces (filter log-trace? new-traces)]
                                               (swap! traces #(reduce conj % new-traces)))))
  (monkey-patch-reagent)
  )

(defn search-input [{:keys [title on-save on-stop]}]
  (let [val  (r/atom title)
        save #(let [v (-> @val str clojure.string/trim)]
               (on-save v))]
    (fn [props]
      [:input (merge props
                     {:type        "text"
                      :value       @val
                      :auto-focus  true
                      :on-blur     save
                      :on-change   #(reset! val (-> % .-target .-value))
                      :on-key-down #(case (.-which %)
                                     13 (save)
                                     nil)})])))


(defn render-traces []
  (let [search           (r/atom "")
        slower-than-ms   (r/atom "")
        slower-than-bold (r/atom "")]
    (fn []
      (let [slower-than-ms-int   (js/parseInt @slower-than-ms)
            slower-than-bold-int (js/parseInt @slower-than-bold)
            op-filter            (when-not (str/blank? @search)
                                   (filter #(str/includes? (str (:operation %) " " (:type %)) @search)))
            ms-filter            (when-not (str/blank? @slower-than-ms)
                                   (filter #(< slower-than-ms-int (:duration %))))
            transducers          (apply comp (remove nil? [ms-filter op-filter]))
            showing-traces       (sequence transducers @traces)

            filter-msg           (if (and (str/blank? @search) (str/blank? @slower-than-ms))
                                   (str "Filter " (count @traces) " events: ")
                                   (str "Filtering " (count showing-traces) " of " (count @traces) " events:"))
            padding              {:padding "0px 5px 0px 5px"}]
        [:div
         {:style {:padding "10px"}}
         [:h1 "TRACES"]
         [:span filter-msg [:button {:on-click #(do (trace/reset-tracing!) (reset! traces []))} " Clear traces"]] [:br]
         [:span "Filter events " [search-input {:on-save #(reset! search %)}]] [:br]
         [:span "Filter slower than " [search-input {:on-save #(reset! slower-than-ms %)}] "ms "] [:br]
         [:span "Bold slower than " [search-input {:on-save #(reset! slower-than-bold %)}] "ms "]
         [:table
          {:cell-spacing "0" :width "100%"}
          [:thead>tr
           [:th "op"]
           [:th "event"]
           [:th "meta"]]
          [:tbody
           (doall
             (for [{:keys [type id operation tags duration] :as trace} showing-traces]
               (let [row-style (merge padding {:border-top (case type :event "1px solid lightgrey" nil)})
                     _         (js/console.log (devtools/header-api-call tags))
                     ]
                 (list [:tr {:key   id
                             :style {:color (case type
                                              :sub/create "green"
                                              :sub/run "red"
                                              :event "blue"
                                              :render "purple"
                                              :re-frame.router/fsm-trigger "red"
                                              nil)}}
                        [:td {:style row-style} (str type)]
                        [:td {:style row-style} operation]
                        [:td
                         {:style (merge row-style {:font-weight (if (< slower-than-bold-int duration)
                                                                  "bold"
                                                                  "")})}
                         (.toFixed duration 1) " ms"]]
                       (when true
                         [:tr {:key (str id "-details")}
                          [:td {:col-span 3} (with-out-str (pprint/pprint (dissoc tags :query-v :event :duration)))]])
                       ))))]]]))))

(defn resizer-style [draggable-area]
  {:position "absolute" :z-index 2 :opacity 0
   :left     (str (- (/ draggable-area 2)) "px") :width "10px" :top "0px" :height "100%" :cursor "col-resize"})

(def ease-transition "left 0.2s ease-out, top 0.2s ease-out, width 0.2s ease-out, height 0.2s ease-out")

(defn devtools []
  ;; Add clear button
  ;; Filter out different trace types
  (let [position       (r/atom :right)
        size           (r/atom 0.5)
        showing?       (r/atom true)
        dragging?      (r/atom false)
        pin-to-bottom? (r/atom true)
        selected-tab   (r/atom :subvis)
        _              (js/console.log "Rendering devtools")
        handle-keys    (fn [e]
                         (let [combo-key?      (or (.-ctrlKey e) (.-metaKey e) (.-altKey e))
                               tag-name        (.-tagName (.-target e))
                               key             (.-key e)
                               entering-input? (contains? #{"INPUT" "SELECT" "TEXTAREA"} tag-name)]
                           (when (and (not entering-input?) combo-key?)
                             (cond
                               (and (= key "h") (.-ctrlKey e))
                               (do (swap! showing? not)
                                   (.preventDefault e))))))]
    (r/create-class
      {:component-will-mount   #(js/window.addEventListener "keydown" handle-keys)
       :component-will-unmount #(js/window.removeEventListener "keydown" handle-keys)
       :display-name           "devtools outer"
       :reagent-render       (fn []
                                 (let [draggable-area 10
                                       full-width     js/window.innerWidth
                                       full-height    js/window.innerHeight
                                       left           (if @showing? (str (* 100 (- 1 @size)) "%")
                                                                    (str full-width "px"))
                                       transition     (if @showing?
                                                        ease-transition
                                                        (str ease-transition ", opacity 0.01s linear 0.2s"))]
                                   [:div {:style {:position "fixed" :width "0px" :height "0px" :top "0px" :left "0px" :z-index 99999999}}
                                    [:div {:style {:position   "fixed" :z-index 1 :box-shadow "rgba(0, 0, 0, 0.298039) 0px 0px 4px" :background "white"
                                                   :left       left :top "0px" :width (str (* 100 @size) "%") :height "100%"
                                                   :transition transition}}
                                     [:div.resizer {:style         (resizer-style draggable-area)
                                                    :on-mouse-down #(reset! dragging? true)
                                                    :on-mouse-up   #(reset! dragging? false)
                                                    :on-mouse-move (fn [e]
                                                                     (when @dragging?
                                                                       (let [x (.-clientX e)
                                                                             y (.-clientY e)]
                                                                         (.preventDefault e)
                                                                         (reset! size (/ (- full-width x)
                                                                                         full-width)))))}]
                                     [:div {:style {:width "100%" :height "100%" :overflow "auto"}}
                                      [:button {:on-click #(reset! selected-tab :traces)} "Traces"]
                                      [:button {:on-click #(reset! selected-tab :subvis)} "SubVis"]
                                      (case @selected-tab
                                        :traces [render-traces]
                                        :subvis [subvis/render-subvis traces]
                                        [render-traces])]]]))})))

