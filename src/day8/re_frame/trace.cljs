(ns day8.re-frame.trace
  (:require [day8.re-frame.trace.subvis :as subvis]
            [day8.re-frame.trace.styles :as styles]
            [day8.re-frame.trace.components :as components]
            [re-frame.trace :as trace :include-macros true]
            [cljs.pprint :as pprint]
            [clojure.string :as str]
            [reagent.core :as r]
            [reagent.interop :refer-macros [$ $!]]
            [reagent.impl.util :as util]
            [reagent.impl.component :as component]
            [reagent.impl.batching :as batch]
            [reagent.ratom :as ratom]
            [goog.object :as gob]
            [re-frame.interop :as interop]

            [devtools.formatters.core :as devtools]))


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
                                   :tags      {:component-path (reagent.impl.component/component-path c)}
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
                                          :tags      {:component-path (reagent.impl.component/component-path c)
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


(def traces (interop/ratom []))
(defn log-trace? [trace]
  (let [rendering? (= (:op-type trace) :render)]
    (if-not rendering?
      true
      (not (str/includes? (or (get-in trace [:tags :component-path]) "") "day8.re_frame.trace")))


    #_(if-let [comp-p (get-in trace [:tags :component-path])]
        (println comp-p))))

(defn disable-tracing! []
  (re-frame.trace/remove-trace-cb ::cb))

(defn enable-tracing! []
  (re-frame.trace/register-trace-cb ::cb (fn [new-traces]
                                           (let [new-traces (filter log-trace? new-traces)]
                                             (swap! traces #(reduce conj % new-traces))))))

(defn init-tracing!
  "Sets up any intial state that needs to be there for tracing. Does not enable tracing."
  []
  (monkey-patch-reagent))


(defn search-input [{:keys [title on-save on-change on-stop]}]
  (let [val  (r/atom title)
        save #(let [v (-> @val str str/trim)]
                (when (pos? (count v))
                  (on-save v)))]
    (fn []
      [:input {:style       {:margin-left 7}
               :type        "text"
               :value       @val
               :auto-focus  true
               :on-change   #(do (reset! val (-> % .-target .-value))
                                 (on-change %))
               :on-key-down #(case (.-which %)
                               13 (do
                                    (save)
                                    (reset! val ""))
                               nil)}])))

(defn query->fn [query]
  (if (= :contains (:filter-type query))
    (fn [trace]
      (str/includes? (str/lower-case (str (:operation trace) " " (:op-type trace)))
                    (:query query)))
    (fn [trace]
      (< (js/parseInt (:query query)) (:duration trace)))))

(defn render-traces [showing-traces]
  (doall
    (for [{:keys [op-type id operation tags duration] :as trace} showing-traces]
      (let [padding   {:padding "0px 5px 0px 5px"}
            row-style (merge padding {:border-top (case op-type :event "1px solid lightgrey" nil)})
            #_#__ (js/console.log (devtools/header-api-call tags))]
        (list [:tr {:key   id
                    :style {:color (case op-type
                                     :sub/create "green"
                                     :sub/run "#fd701e"
                                     :event "blue"
                                     :render "purple"
                                     :re-frame.router/fsm-trigger "#fd701e"
                                     nil)}}
               [:td {:style row-style} (str op-type)]
               [:td {:style row-style} operation]
               [:td
                {:style (merge row-style {:font-weight (if (< slower-than-bold-int duration)
                                                         "bold"
                                                         "")
                                          :white-space "nowrap"})}

                (.toFixed duration 1) " ms"]]
              (when true
                [:tr {:key (str id "-details")}
                 [:td {:col-span 3} (with-out-str (pprint/pprint (dissoc tags :query-v :event :duration)))]]))))))

(defn render-trace-panel []
  (let [filter-input     (r/atom "")
        filter-items     (r/atom [])
        filter-type      (r/atom :contains)]
    (fn []
      (let [showing-traces       (if (= @filter-items [])
                                   @traces
                                   (filter (apply every-pred (map query->fn @filter-items)) @traces))
            save-query           (fn [_]
                                   (swap! filter-items conj {:id (random-uuid)
                                                             :query (if (= @filter-type :contains)
                                                                      (str/lower-case @filter-input)
                                                                      (js/parseInt @filter-input))
                                                             :filter-type @filter-type}))]
        [:div
         [:div.filter-control {:style {:margin-bottom 20}}
           [:div.filter-control-input
            {:style {:margin-bottom 10}}
            [:select {:value @filter-type :on-change (fn [e]
                                                         (reset! filter-type
                                                                 (keyword (str/replace (.. e -target -value) " " "-"))))}
             [:option :contains]
             [:option :slower-than]]
            [search-input {:on-save save-query
                           :on-change #(reset! filter-input (.. % -target -value))}]
            [:button.button.icon-button {:on-click save-query
                                         :style {:margin 0}}
             [components/icon-add]]
            [:br]]
           [:ul.filter-items
             (map (fn [item]
                      ^{:key (:id item)}
                      [:li.filter-item.button
                        {:on-click (fn [event] (swap! filter-items #(remove (comp (partial = (:query item)) :query) %)))}
                        (:filter-type item) ": " [:span.filter-item-string (:query item)]
                        [:button.icon-button [components/icon-remove]]])
                  @filter-items)]]
         [:table
          {:cell-spacing "0" :width "100%"}
          [:thead>tr
           [:th "operations"]
           [:th
             (when (pos? (count @filter-items))
               (str (count showing-traces) " of "))
             (when (pos? (count @traces))
               (str (count @traces)))
             " events "
             (when (pos? (count @traces))
               [:span "(" [:button.text-button {:on-click #(do (trace/reset-tracing!) (reset! traces []))} "clear"] ")"])]
           [:th "meta"]]
          [:tbody (render-traces showing-traces)]]]))))

(defn resizer-style [draggable-area]
  {:position "absolute" :z-index 2 :opacity 0
   :left     (str (- (/ draggable-area 2)) "px") :width "10px" :top "0px" :height "100%" :cursor "col-resize"})

(def ease-transition "left 0.2s ease-out, top 0.2s ease-out, width 0.2s ease-out, height 0.2s ease-out")

(defn devtools []
  ;; Add clear button
  ;; Filter out different trace types
  (let [position       (r/atom :right)
        size           (r/atom 0.35)
        showing?       (r/atom false)
        dragging?      (r/atom false)
        pin-to-bottom? (r/atom true)
        selected-tab   (r/atom :traces)
        handle-keys    (fn [e]
                         (let [combo-key?      (or (.-ctrlKey e) (.-metaKey e) (.-altKey e))
                               tag-name        (.-tagName (.-target e))
                               key             (.-key e)
                               entering-input? (contains? #{"INPUT" "SELECT" "TEXTAREA"} tag-name)]
                           (when (and (not entering-input?) combo-key?)
                             (cond
                               (and (= key "h") (.-ctrlKey e))
                               (do (swap! showing? not)
                                   (if @showing?
                                     (enable-tracing!)
                                     (disable-tracing!))
                                   (.preventDefault e))))))]
    (r/create-class
      {:component-will-mount   #(js/window.addEventListener "keydown" handle-keys)
       :component-will-unmount #(js/window.removeEventListener "keydown" handle-keys)
       :display-name           "devtools outer"
       :reagent-render         (fn []
                                 (let [draggable-area 10
                                       full-width     js/window.innerWidth
                                       full-height    js/window.innerHeight
                                       left           (if @showing? (str (* 100 (- 1 @size)) "%")
                                                                    (str full-width "px"))
                                       transition     (if @showing?
                                                        ease-transition
                                                        (str ease-transition ", opacity 0.01s linear 0.2s"))]
                                   [:div.panel-wrapper
                                    {:style {:position "fixed" :width "0px" :height "0px" :top "0px" :left "0px" :z-index 99999999}}
                                    [:div.panel
                                      {:style {:position   "fixed" :z-index 1 :box-shadow "rgba(0, 0, 0, 0.298039) 0px 0px 4px" :background "white"
                                               :left       left :top "0px" :width (str (* 100 @size) "%") :height "100%"
                                               :transition transition}}
                                      [:div.panel-resizer {:style         (resizer-style draggable-area)
                                                           :on-mouse-down #(reset! dragging? true)
                                                           :on-mouse-up   #(reset! dragging? false)
                                                           :on-mouse-move (fn [e]
                                                                           (when @dragging?
                                                                             (let [x (.-clientX e)
                                                                                   y (.-clientY e)]
                                                                               (.preventDefault e)
                                                                               (reset! size (/ (- full-width x)
                                                                                               full-width)))))}]
                                      [:div.panel-content
                                        {:style {:width "100%" :height "100%" :display "flex" :flex-direction "column"}}
                                        [:div.panel-content-top
                                          [:div.nav
                                            [:button {:class (str "tab button " (when (= @selected-tab :traces) "active"))
                                                      :on-click #(reset! selected-tab :traces)} "Traces"]
                                            [:button {:class (str "tab button " (when (= @selected-tab :subvis) "active"))
                                                      :on-click #(reset! selected-tab :subvis)} "SubVis"]]]
                                        [:div.panel-content-scrollable
                                          (case @selected-tab
                                            :traces [render-trace-panel]
                                            :subvis [subvis/render-subvis traces]
                                            [render-trace-panel])]]]]))})))

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

(defn inject-styles []
  (let [id    "--re-frame-trace-styles--"
        styles-el (.getElementById js/document id)
        new-styles-el (.createElement js/document "style")
        new-styles styles/panel-styles]
    (.setAttribute new-styles-el "id" id)
    (-> new-styles-el
        (.-innerHTML)
        (set! new-styles))
    (if styles-el
      (-> styles-el
          (.-parentNode)
          (.replaceChild new-styles-el styles-el))
      (let []
        (.appendChild (.-head js/document) new-styles-el)
        new-styles-el))))

(defn inject-devtools! []
  (inject-styles)
  (r/render [devtools] (panel-div)))
