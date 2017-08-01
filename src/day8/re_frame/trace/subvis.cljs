(ns day8.re-frame.trace.subvis
  (:require cljsjs.d3
            [day8.re-frame.trace.d3 :as d3t]
            [day8.re-frame.trace.graph :as graph]
            [reagent.core :as r]
            [re-frame.interop :as interop]
            [re-frame.db :as db]
            [goog.object :as gob]
            [clojure.set :as set]))

(def width 400)
(def height 400)

(def prev-graph (atom nil))
(def mygraph (r/atom {:nodes [{:id 1 :group 1}
                              {:id 2 :group 1}
                              {:id 3 :group 2}]
                      :links [{:source 1 :target 2 :value 1}]}))

(def app-db-node {:id    (interop/reagent-id db/app-db)
                  :title "app-db"
                  :group 1
                  :r     20
                  :fx    15
                  :fy    (/ height 2)})

(defn render-node? [d]
  (= "render" (gob/getValueByKeys d "data" "type")))

(defn min-max
  "Returns x if it is within min-val and max-val
  otherwise returns min-val if x is less than min-val
  and max-val if x is greater than max-val.

  Essentially this provides a bounding box/clamp around x."
  [min-val x max-val]
  (assert (<= min-val max-val))
  (cljs.core/max min-val (cljs.core/min x max-val)))

(defn render-subvis [traces-ratom]
  (let [color-a      (atom nil)
        svg-a        (atom nil)
        simulation-a (atom nil)]
    (fn []
      [:div
       [d3t/create-d3
        {:render-component (fn [ratom]
                             [:svg#d3cmp {:width width :height height}])
         :d3-once          (fn [ratom]
                             (let [svg (reset! svg-a (. js/d3 select "#d3cmp"))]
                               (reset! color-a (.scaleOrdinal js/d3 (.-schemeCategory20 js/d3)))
                               (reset! simulation-a
                                       (.. js/d3
                                           (forceSimulation)
                                           (force "link" (.. js/d3 (forceLink)
                                                             (id #(.-id %))
                                                             (distance (constantly 100))))
                                           (force "charge" (.. js/d3 (forceManyBody)
                                                               (strength (constantly -100))))
                                           (force "center" (. js/d3 forceCenter (/ width 2) (/ height 2)))))
                               (.. (. svg append "g")
                                   (attr "class" "links"))
                               (.. (. svg append "g")
                                   (attr "class" "nodes"))))
         :d3-update        (fn [ratom]
                             (let [old-g @prev-graph        ;; TODO: is this working?
                                   graph (reset! prev-graph (graph/trace->sub-graph @ratom [app-db-node]))]
                               (when (not= old-g graph)
                                 (let [simulation   @simulation-a
                                       color        @color-a
                                       svg          @svg-a
                                       graph        (graph/trace->sub-graph @ratom [app-db-node])
                                       nodes        (clj->js (:nodes graph))
                                       links        (clj->js (:links graph))
                                       drag-started (fn [d]
                                                      (when (zero? (.. js/d3 -event -active))
                                                        (.. simulation
                                                            (alphaTarget 0.3)
                                                            (restart))))
                                       dragged      (fn [d]
                                                      (set! (.-fx d) (.. js/d3 -event -x))
                                                      (set! (.-fy d) (.. js/d3 -event -y)))
                                       drag-ended   (fn [d]
                                                      (when (zero? (.. js/d3 -event -active))
                                                        (.. simulation
                                                            (alphaTarget 0.0)))
                                                      (set! (.-fx d) nil)
                                                      (set! (.-fy d) nil))

                                       ;; Links
                                       link         (.. svg
                                                        (select "g.links")
                                                        (selectAll "line")
                                                        (data links #(.-id %)))
                                       enter-link   (.. link
                                                        (enter)
                                                        (append "line")
                                                        (attr "stroke-width" (fn [d] (Math/sqrt (.-value d)))))
                                       merged-link  (.. enter-link (merge link))
                                       _            (.. link
                                                        (exit)
                                                        (remove "line"))

                                       ;; Nodes
                                       node         (.. svg
                                                        (select "g.nodes")
                                                        (selectAll ".node")
                                                        (data nodes #(.-id %)))
                                       enter-node   (.. node
                                                        (enter)
                                                        (append "g")
                                                        (attr "class" "node")
                                                        (call (.. js/d3 (drag)
                                                                  (on "start" drag-started)
                                                                  (on "drag" dragged)
                                                                  (on "end" drag-ended))))
                                       circle       (.. enter-node
                                                        (append "circle")
                                                        (attr "r" (fn [d] (.-r d)))
                                                        (attr "fill" (fn [d] (color (.-group d)))))
                                       text         (.. enter-node
                                                        (append "text")
                                                        (attr "dx" (fn [d] (if (render-node? d)
                                                                             -12
                                                                             12)))
                                                        (attr "dy" "0.35em")
                                                        (attr "text-anchor" (fn [d]
                                                                              (if (render-node? d)
                                                                                "end"
                                                                                "start")))
                                                        (attr "opacity" 1)
                                                        (text (fn [d] (.-title d))))

                                       merged-node  (.. enter-node (merge node))

                                       ticked       (fn []
                                                      (.. merged-node
                                                          (attr "transform"
                                                                (fn [d]
                                                                  (let [r (.-r d)
                                                                        x (min-max r (.-x d) (- width r))
                                                                        y (min-max r (.-y d) (- height r))]
                                                                    (set! (.-x d) x)
                                                                    (set! (.-y d) y)
                                                                    (str "translate(" x "," y ")")))))

                                                      (.. merged-link
                                                          (attr "x1" (fn [d] (.. d -source -x)))
                                                          (attr "y1" (fn [d] (.. d -source -y)))
                                                          (attr "x2" (fn [d] (.. d -target -x)))
                                                          (attr "y2" (fn [d] (.. d -target -y)))))

                                       node-exit-t  (.. node
                                                        (exit)
                                                        (transition)
                                                        (delay (fn [d i] (* i 30)))
                                                        (duration 500))


                                       _            (.. node-exit-t
                                                        (select "circle")
                                                        (attr "transform" "scale(0,0)")
                                                        (attr "fill" "#000000"))
                                       _            (.. node-exit-t
                                                        (select "text")
                                                        (attr "opacity" 0))
                                       _            (.. node-exit-t
                                                        (on "end" (fn [] (this-as this
                                                                                       (.. js/d3
                                                                                           (select this)
                                                                                           (remove))))))
                                       _            (.. node-exit-t
                                                        (transition)
                                                        (call (fn []
                                                                (.. simulation
                                                                    (nodes nodes)
                                                                    (on "tick" ticked))

                                                                (.. simulation
                                                                    (force "link")
                                                                    (links links))

                                                                (.. simulation
                                                                    (restart)
                                                                    (alpha 0.3)))))]))))}


        traces-ratom]
       [:hr]])))
