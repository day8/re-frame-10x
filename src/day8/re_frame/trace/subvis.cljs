(ns day8.re-frame.trace.subvis
  (:require cljsjs.d3
            [day8.re-frame.trace.d3 :as d3t]
            [reagent.core :as r]
            [re-frame.utils :as rutils]
            [re-frame.db :as db]
            [goog.object :as gob]
            [clojure.set :as set]))

(def width 400)
(def height 200)

(def mygraph (r/atom {:nodes [{:id 1 :group 1}
                              {:id 2 :group 1}
                              {:id 3 :group 2}]
                      :links [{:source 1 :target 2 :value 1}]}))

(defn trace->sub-graph [traces]
  (let [disposed             (->> traces
                                  (filter #(#{:sub/dispose} (:type %)))
                                  (map #(get-in % [:tags :reaction]))
                                  set)
        sub-nodes            (->> traces
                                  (filter #(#{:sub/create} (:type %)))
                                  (remove #(contains? disposed (get-in % [:tags :reaction])))
                                  (remove #(get-in % [:tags :cached?]))
                                  (map (fn [trace]
                                         {:id    (get-in trace [:tags :reaction])
                                          :title (str (:operation trace))
                                          :group (mod (:id trace) 20)
                                          :r     10
                                          :data  trace})))

        unmounted-components #{}
        view-nodes           (->> traces
                                  (filter #(#{:render} (:type %)))
                                  (remove #(contains? unmounted-components (:id %))) ;; todo
                                  (map (fn [trace]
                                         {:id    (get-in trace [:tags :reaction])
                                          :title (:operation trace)
                                          :group (mod (:id trace) 20)
                                          :r     5
                                          :data  trace}))

                                  )

        sub-links            (->> traces
                                  (filter #(#{:sub/run} (:type %)))
                                  (remove #(contains? disposed (get-in % [:tags :reaction])))
                                  (mapcat (fn [trace]
                                            (for [input-signal (get-in trace [:tags :input-signals])
                                                  :let [reaction (get-in trace [:tags :reaction])]
                                                  :when (every? some? [input-signal reaction])]
                                              {:source input-signal :target reaction :value 1}))))

        view-links           (->> traces
                                  (filter #(#{:render} (:type %)))
                                  (remove #(contains? unmounted-components (get-in % [:tags :reaction])))
                                  (mapcat (fn [trace]
                                            (for [input-signal (get-in trace [:tags :input-signals])
                                                  :let [reaction (get-in trace [:tags :reaction])]
                                                  :when (every? some? [input-signal reaction])]
                                              {:source input-signal :target reaction :value 0.5}))))

        app-db               {:id    (rutils/reagent-id db/app-db)
                              :title "app-db"
                              :group 1
                              :r     20
                              :fx    (/ width 2)
                              :fy    30}


        all-nodes            (concat sub-nodes [app-db] view-nodes)
        node-ids             (set (map :id all-nodes))
        nodes-links          (->> (mapcat (fn [{:keys [source target]}] [source target]) view-links)
                                  set)
        missing-nodes        (set/difference nodes-links node-ids) ;; These are local ratoms

        view-links           (->> view-links
                                  (remove #(get missing-nodes (:source %))))
        ]

    {:nodes all-nodes
     :links (concat sub-links view-links)}))

(defn min-max
  "Returns x if it is within min-val and max-val
  otherwise returns min-val if x is less than min-val
  and max-val if x is greater than max-val.

  Essentially this provides a bounding box/clamp around x."
  [min-val x max-val]
  (assert (<= min-val max-val))
  (cljs.core/max min-val (cljs.core/min x max-val)))

(defn force-inner [graph]
  (r/create-class
    {:reagent-render     (fn [] [:div [:svg {:width width :height height}]])

     :component-did-mount  (fn []
                             (let [nodes       (clj->js (:nodes graph))
                                   links       (clj->js (:links graph))
                                   color       (.scaleOrdinal js/d3 (.-schemeCategory20 js/d3))
                                   svg         (. js/d3 select "svg")
                                   simulation  (.. js/d3
                                                   (forceSimulation)
                                                   (force "link" (.. js/d3 (forceLink)
                                                                     (id #(.-id %))
                                                                     (distance (constantly 100))))
                                                   (force "charge" (.. js/d3 (forceManyBody)
                                                                       (strength (constantly -100))))
                                                   (force "center" (. js/d3 forceCenter (/ width 2) (/ height 2))))

                                   dragstarted (fn [d]
                                                 (when (zero? (.. js/d3 -event -active))
                                                   (.. simulation
                                                       (alphaTarget 0.3)
                                                       (restart)))

                                                 (set! (.-fx d) (.-x d))
                                                 (set! (.-fy d) (.-y d)))

                                   dragged     (fn [d]
                                                 (set! (.-fx d) (.. js/d3 -event -x))
                                                 (set! (.-fy d) (.. js/d3 -event -y)))

                                   dragended   (fn [d]
                                                 (when (zero? (.. js/d3 -event -active))
                                                   (.. simulation
                                                       (alphaTarget 0.0)))
                                                 (set! (.-fx d) nil)
                                                 (set! (.-fy d) nil))

                                   link        (.. (. svg append "g")
                                                   (attr "class" "links")
                                                   (selectAll "line")
                                                   (data links)
                                                   (enter)
                                                   (append "line")
                                                   (attr "stroke-width" (fn [d] (Math/sqrt (.-value d)))))

                                   node        (.. (. svg append "g")
                                                   (attr "class" "nodes")
                                                   (selectAll "circle")
                                                   (data nodes)
                                                   (enter)
                                                   (append "g")
                                                   (attr "class" "node"))

                                   circle      (.. node
                                                   (append "circle")
                                                   (attr "r" #(or (gob/get % "r" 10)))
                                                   (attr "fill" (fn [d] (color (.-group d))))
                                                   (call (.. (. js/d3 drag)
                                                             (on "start" dragstarted)
                                                             (on "drag" dragged)
                                                             (on "end" dragended))))

                                   label       (.. node
                                                   (append "text")
                                                   (attr "dy" ".35em")
                                                   (text #(gob/get % "title" "")))

                                   ticked      (fn []
                                                 (.. link
                                                     (attr "x1" (fn [d] (.. d -source -x)))
                                                     (attr "y1" (fn [d] (.. d -source -y)))
                                                     (attr "x2" (fn [d] (.. d -target -x)))
                                                     (attr "y2" (fn [d] (.. d -target -y))))
                                                 (.. circle
                                                     (attr "cx" (fn [d] (min-max (.. d -r) (.. d -x) (- width (.. d -r)))))
                                                     (attr "cy" (fn [d] (min-max (.. d -r) (.. d -y) (- height (.. d -r))))))
                                                 (.. label
                                                     (attr "x" #(+ (.-x %) 2 (.-r %)))
                                                     (attr "y" #(+ (.-y %))))
                                                 nil)
                                   ]

                               (.. simulation
                                   (nodes nodes)
                                   (on "tick" ticked))

                               (.. simulation
                                   (force "link")
                                   (links links))))

     :component-did-update (fn [this]
                             #_(let [[_ data] (r/argv this)
                                     d3data  (clj->js data)
                                     circles (.. js/d3
                                                 (select "svg")
                                                 (selectAll "circle")
                                                 (data d3data (fn [d i] (when d (.-name d)))))]
                                 (.. circles
                                     ;(attr "cx" (fn [d] (.-x d)))
                                     ;(attr "cy" (fn [d] (.-y d)))
                                     ;(attr "r" (fn [d] (.-r d)))
                                     enter
                                     (append "circle")
                                     (attr "cx" 200)
                                     (attr "cy" 200)
                                     (attr "r" 500)
                                     (transition)
                                     (attr "cx" (fn [d] (.-x d)))
                                     (attr "cy" (fn [d] (.-y d)))
                                     (attr "r" (fn [d] (.-r d)))
                                     (attr "fill" (fn [d] (.-color d))))
                                 (.. circles
                                     exit
                                     remove)))}))


(defn force-outer [traces-ratom]
  (fn []
    (let [trace-graph (trace->sub-graph @traces-ratom)]
      [force-inner trace-graph])))

(defonce desc (r/atom 1))

(defn render-subvis [traces-ratom]
  (let [color-a (atom nil)
        svg-a (atom nil)
        simulation-a (atom nil)]
    (fn []
      [:div
       {:style {:padding "10px"}}
       [:h1 "SUBVIS"]
       [force-outer traces-ratom]
       [:hr]
       [:h2 {:on-click #(swap! desc inc)} "Click"]
       [(d3t/create-d3 {:render-component (fn [ratom]
                                            [:div
                                             [:h1 (str "SVG")]
                                             [:svg#d3cmp {:width width :height height}]])
                        :d3-did-mount     (fn [ratom]
                                            (let [graph       (trace->sub-graph @ratom)
                                                  nodes       (clj->js (:nodes graph))
                                                  links       (clj->js (:links graph))
                                                  color       (reset! color-a (.scaleOrdinal js/d3 (.-schemeCategory20 js/d3)))
                                                  svg         (reset! svg-a (. js/d3 select "#d3cmp"))
                                                  simulation  (reset! simulation-a
                                                                      (.. js/d3
                                                                          (forceSimulation)
                                                                          (force "link" (.. js/d3 (forceLink)
                                                                                            (id #(.-id %))
                                                                                            (distance (constantly 100))))
                                                                          (force "charge" (.. js/d3 (forceManyBody)
                                                                                              (strength (constantly -100))))
                                                                          (force "center" (. js/d3 forceCenter (/ width 2) (/ height 2)))))

                                                  dragstarted (fn [d]
                                                                (when (zero? (.. js/d3 -event -active))
                                                                  (.. simulation
                                                                      (alphaTarget 0.3)
                                                                      (restart)))

                                                                (set! (.-fx d) (.-x d))
                                                                (set! (.-fy d) (.-y d)))

                                                  dragged     (fn [d]
                                                                (set! (.-fx d) (.. js/d3 -event -x))
                                                                (set! (.-fy d) (.. js/d3 -event -y)))

                                                  dragended   (fn [d]
                                                                (when (zero? (.. js/d3 -event -active))
                                                                  (.. simulation
                                                                      (alphaTarget 0.0)))
                                                                (set! (.-fx d) nil)
                                                                (set! (.-fy d) nil))

                                                  link        (.. (. svg append "g")
                                                                  (attr "class" "links")
                                                                  (selectAll "line")
                                                                  (data links)
                                                                  (enter)
                                                                  (append "line")
                                                                  (attr "stroke-width" (fn [d] (Math/sqrt (.-value d))))
                                                                  )

                                                  link-sel    (.. svg
                                                                  (selectAll ".links > line"))

                                                  node        (.. (. svg append "g")
                                                                  (attr "class" "nodes")
                                                                  (selectAll "circle")
                                                                  (data nodes)
                                                                  (enter)
                                                                  (append "g")
                                                                  (attr "class" "node"))

                                                 circle      (.. node
                                                                  (append "circle")
                                                                  (attr "r" #(or (gob/get % "r" 10)))
                                                                  (attr "fill" (fn [d] (color (.-group d))))
                                                                  (call (.. (. js/d3 drag)
                                                                            (on "start" dragstarted)
                                                                            (on "drag" dragged)
                                                                            (on "end" dragended))))

                                                  circle-sel  (.. svg
                                                                  (selectAll ".node > circle"))

                                                  label       (.. node
                                                                  (append "text")
                                                                  (attr "dy" ".35em")
                                                                  (text #(gob/get % "title" "")))

                                                  label-sel   (.. svg
                                                                  (selectAll ".node > text"))

                                                  ticked      (fn []
                                                                (.. link-sel
                                                                    (attr "x1" (fn [d] (.. d -source -x)))
                                                                    (attr "y1" (fn [d] (.. d -source -y)))
                                                                    (attr "x2" (fn [d] (.. d -target -x)))
                                                                    (attr "y2" (fn [d] (.. d -target -y))))
                                                                (.. circle-sel
                                                                    (attr "cx" (fn [d] (min-max (.. d -r) (.. d -x) (- width (.. d -r)))))
                                                                    (attr "cy" (fn [d] (min-max (.. d -r) (.. d -y) (- height (.. d -r))))))
                                                                (.. label-sel
                                                                    (attr "x" #(+ (.-x %) 2 (.-r %)))
                                                                    (attr "y" #(+ (.-y %))))
                                                                nil)
                                                  ]

                                              (.. simulation
                                                  (nodes nodes)
                                                  (on "tick" ticked))

                                              (.. simulation
                                                  (force "link")
                                                  (links links))))
                       :d3-enter         (fn [ratom]
                                            (let [graph       (trace->sub-graph @ratom)
                                                  nodes       (clj->js (:nodes graph))
                                                  links       (clj->js (:links graph))

                                                  svg @svg-a
                                                  color @color-a
                                                  simulation @simulation-a

                                                  dragstarted (fn [d]
                                                                (when (zero? (.. js/d3 -event -active))
                                                                  (.. simulation
                                                                      (alphaTarget 0.3)
                                                                      (restart)))

                                                                (set! (.-fx d) (.-x d))
                                                                (set! (.-fy d) (.-y d)))

                                                  dragged     (fn [d]
                                                                (set! (.-fx d) (.. js/d3 -event -x))
                                                                (set! (.-fy d) (.. js/d3 -event -y)))

                                                  dragended   (fn [d]
                                                                (when (zero? (.. js/d3 -event -active))
                                                                  (.. simulation
                                                                      (alphaTarget 0.0)))
                                                                (set! (.-fx d) nil)
                                                                (set! (.-fy d) nil))

                                                  link        (.. (. svg append "g")
                                                                  (attr "class" "links")
                                                                  (selectAll "line")
                                                                  (data links)
                                                                  (enter)
                                                                  (append "line")
                                                                  (attr "stroke-width" (fn [d] (Math/sqrt (.-value d)))))

                                                  node        (.. (. svg append "g")
                                                                  (attr "class" "nodes")
                                                                  (selectAll "circle")
                                                                  (data nodes)
                                                                  (enter)
                                                                  (append "g")
                                                                  (attr "class" "node"))

                                                  circle      (.. node
                                                                  (append "circle")
                                                                  (attr "r" #(or (gob/get % "r" 10)))
                                                                  (attr "fill" (fn [d] (color (.-group d))))
                                                                  (call (.. (. js/d3 drag)
                                                                            (on "start" dragstarted)
                                                                            (on "drag" dragged)
                                                                            (on "end" dragended))))


                                                  label       (.. node
                                                                  (append "text")
                                                                  (attr "dy" ".35em")
                                                                  (text #(gob/get % "title" "")))

                                                  ]

                                              (.. simulation
                                                  (force "link")
                                                  (links links))
                                              )




                                            (println "D3 did enter"))}
                       traces-ratom)]
       [:hr]])))





;;;;;;;;;;;;;;;;;;;;;;;;;;











;(ns todomvc.subvis2
;  (:require [reagent.core :as r]))
;
;(defn prep-parent [parent]
;  (-> parent
;      (.selectAll "*")
;      .remove)
;  (.append parent "g"))
;
;(defn draw-circle [parent]
;  (let [OUTER_WIDTH  208
;        OUTER_HEIGHT 208
;        parent       (-> (js/d3.select parent)
;                         (.select "svg")
;                         (.attr "width" OUTER_WIDTH)
;                         (.attr "height" OUTER_HEIGHT))]
;
;    ; Clear everything under the parent node so we can re-render it.
;    (prep-parent parent)
;
;    (let [top-node     (.select parent "g")
;          COLOR_CIRCLE "#fdb74a"]
;      (.attr top-node
;             "transform"
;             (str "translate("
;                  (/ OUTER_WIDTH 2.0)
;                  ","
;                  (/ OUTER_HEIGHT 2.0)
;                  ")"))
;
;      (-> top-node
;          (.append "circle")
;          (.attr "cx" 0)
;          (.attr "cy" 0)
;          (.attr "r" 100)
;          (.style "stroke" COLOR_CIRCLE)))))
;
;(defn d3-gauge [args]
;  (let [dom-node (r/atom nil)]
;    (r/create-class
;      {:component-did-update
;       (fn [this old-argv]
;         (let [[_ args] (r/argv this)]
;           ;; This is where we get to actually draw the D3 gauge.
;           (draw-circle @dom-node)))
;
;       :component-did-mount
;       (fn [this]
;         (let [node (r/dom-node this)]
;           ;; This will trigger a re-render of the component.
;           (reset! dom-node node)))
;
;       :reagent-render
;       (fn [args]
;         ;; Necessary for Reagent to see that we depend on the dom-node r/atom.
;         ;; Note: we don't actually use any of the args here.  This is because
;         ;; we cannot render D3 at this point.  We have to wait for the update.
;         @dom-node
;         [:div.gauge [:svg]])})))
;
;
;(def circles (r/atom [{:name  "circle 1"
;                       :x     10
;                       :y     10
;                       :r     20
;                       :color "black"}
;                      {:name  "circle 2"
;                       :x     35
;                       :y     35
;                       :r     15
;                       :color "red"}
;                      {:name  "circle 3"
;                       :x     100
;                       :y     100
;                       :r     30
;                       :color "blue"}
;                      {:name "circle 4" :x 55 :y 55 :r 10 :color "red"}]))
;
;(defn new-circle []
;  {:name  (str (gensym "circle"))
;   :x     (rand-int 400)
;   :y     (rand-int 400)
;   :r     (+ 10 (rand-int 20))
;   :color (str "hsl(" (rand-int 360) ", 100%, 50%)")})
;
;(defn add-new [n]
;  (swap! circles conj (new-circle)))
;
;(defn d3-inner [data]
;  (r/create-class
;    {:reagent-render       (fn [] [:div [:svg {:width 400 :height 800}]])
;
;     :component-did-mount  (fn []
;                             (let [d3data (clj->js data)]
;                               (.interval js/d3 add-new 1000)
;                               (.. js/d3
;                                   (select "svg")
;                                   (selectAll "circle")
;                                   (data d3data (fn [d i] (.-name d)))
;                                   enter
;                                   (append "circle")
;                                   (attr "cx" (fn [d] (.-x d)))
;                                   (attr "cy" (fn [d] (.-y d)))
;                                   (attr "r" (fn [d] (.-r d)))
;                                   (attr "fill" (fn [d] (.-color d))))))
;
;     :component-did-update (fn [this]
;                             (let [[_ data] (r/argv this)
;                                   d3data  (clj->js data)
;                                   circles (.. js/d3
;                                               (select "svg")
;                                               (selectAll "circle")
;                                               (data d3data (fn [d i] (when d (.-name d)))))]
;                               (.. circles
;                                   ;(attr "cx" (fn [d] (.-x d)))
;                                   ;(attr "cy" (fn [d] (.-y d)))
;                                   ;(attr "r" (fn [d] (.-r d)))
;                                   enter
;                                   (append "circle")
;                                   (attr "cx" 200)
;                                   (attr "cy" 200)
;                                   (attr "r" 500)
;                                   (transition)
;                                   (attr "cx" (fn [d] (.-x d)))
;                                   (attr "cy" (fn [d] (.-y d)))
;                                   (attr "r" (fn [d] (.-r d)))
;                                   (attr "fill" (fn [d] (.-color d))))
;                               (.. circles
;                                   exit
;                                   remove)))}))
;
;(defn outer []
;  (let [data circles #_(subscribe [:circles])]
;    (fn []
;      [d3-inner @data])))
;
;
