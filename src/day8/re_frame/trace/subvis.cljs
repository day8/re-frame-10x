(ns day8.re-frame.trace.subvis
  (:require cljsjs.d3
            [day8.re-frame.trace.d3 :as d3t]
            [day8.re-frame.trace.graph :as graph]
            [reagent.core :as r]
            [re-frame.utils :as rutils]
            [re-frame.db :as db]
            [goog.object :as gob]
            [clojure.set :as set]))

(def width 400)
(def height 400)

(def mygraph (r/atom {:nodes [{:id 1 :group 1}
                              {:id 2 :group 1}
                              {:id 3 :group 2}]
                      :links [{:source 1 :target 2 :value 1}]}))

(def app-db-node {:id    (rutils/reagent-id db/app-db)
                  :title "app-db"
                  :group 1
                  :r     20
                  :fx    15
                  :fy    (/ height 2)})


(defn min-max
  "Returns x if it is within min-val and max-val
  otherwise returns min-val if x is less than min-val
  and max-val if x is greater than max-val.

  Essentially this provides a bounding box/clamp around x."
  [min-val x max-val]
  (assert (<= min-val max-val))
  (cljs.core/max min-val (cljs.core/min x max-val)))


(defn ticked [selector]
  (fn []
    ;(println "ticked")
    (let [link-sel   (.. selector
                         (selectAll "g.links > line"))
          circle-sel (.. selector
                         (selectAll "g.node > circle"))
          label-sel  (.. selector
                         (selectAll "g.node > text"))]
      (.. link-sel
          (attr "x1" (fn [d] (.. d -source -x)))
          (attr "y1" (fn [d] (.. d -source -y)))
          (attr "x2" (fn [d] (.. d -target -x)))
          (attr "y2" (fn [d] (.. d -target -y))))
      (.. circle-sel
          (attr "cx" (fn [d] (set! (.-x d) (min-max (.. d -r) (.. d -x) (- width (.. d -r))))))
          (attr "cy" (fn [d] (set! (.-y d) (min-max (.. d -r) (.. d -y) (- height (.. d -r)))))))
      (.. label-sel
          (attr "x" (fn [d] (+ 2 (.-x d) (.-r d))))
          (attr "y" #(+ (.-y %)))))))

(defn render-subvis [traces-ratom]
  (let [color-a      (atom nil)
        svg-a        (atom nil)
        simulation-a (atom nil)
        run?         (atom false)]
    (fn []
      (println "Render subvis")
      [:div
       {:style {:padding "10px"}}
       [:h1 "SUBVIS"]
       [d3t/create-d3
        {:render-component (fn [ratom]
                             [:svg#d3cmp {:width width :height height}])
         :d3-once          (fn [ratom]
                             (let [color      (reset! color-a (.scaleOrdinal js/d3 (.-schemeCategory20 js/d3)))
                                   svg        (reset! svg-a (. js/d3 select "#d3cmp"))
                                   simulation (reset! simulation-a
                                                      (.. js/d3
                                                          (forceSimulation)
                                                          (force "link" (.. js/d3 (forceLink)
                                                                            (id #(.-id %))
                                                                            (distance (constantly 100))))
                                                          (force "charge" (.. js/d3 (forceManyBody)
                                                                              (strength (constantly -100))))
                                                          (force "center" (. js/d3 forceCenter (/ width 2) (/ height 2)))))

                                   link       (.. (. svg append "g")
                                                  (attr "class" "links"))

                                   node       (.. (. svg append "g")
                                                  (attr "class" "nodes"))]))
         :d3-update        (fn [ratom]
                             (when-not false #_@run?
                               #_(reset! run? true)
                               (let [graph       (graph/trace->sub-graph @ratom [app-db-node])
                                     nodes       (clj->js (:nodes graph))
                                     links       (clj->js (:links graph))
                                     svg         @svg-a
                                     color       @color-a
                                     simulation  @simulation-a
                                     dragstarted (fn [d]
                                                   (when (zero? (.. js/d3 -event -active))
                                                     (.. simulation
                                                         (alphaTarget 0.3)
                                                         (restart)))

                                                   #_(set! (.-fx d) (.. js/d3 -event -x)) ; (.-x d)
                                                   #_(set! (.-fy d) (.. js/d3 -event -y)))

                                     dragged     (fn [d]
                                                   (set! (.-fx d) (.. js/d3 -event -x))
                                                   (set! (.-fy d) (.. js/d3 -event -y)))

                                     dragended   (fn [d]
                                                   (when (zero? (.. js/d3 -event -active))
                                                     (.. simulation
                                                         (alphaTarget 0.0)))
                                                   (set! (.-fx d) nil)
                                                   (set! (.-fy d) nil))

                                     link        (.. svg
                                                     (select ".links")
                                                     (selectAll "line")
                                                     (data links (fn [d] (.-id d))))

                                     new-link    (.. link
                                                     (enter)
                                                     (append "line")
                                                     (attr "stroke-width" (fn [d] (Math/sqrt (.-value d)))))

                                     node        (.. svg
                                                     (select ".nodes")
                                                     (selectAll "circle")
                                                     (data nodes))

                                     new-node    (.. node
                                                     (enter)
                                                     (append "g")
                                                     (attr "class" "node"))

                                     circle      (.. new-node
                                                     (append "circle")
                                                     (attr "r" #(or (gob/get % "r" 10)))
                                                     (attr "fill" (fn [d] (color (.-group d))))
                                                     (call (.. (. js/d3 drag)
                                                               (on "start" dragstarted)
                                                               (on "drag" dragged)
                                                               (on "end" dragended))))

                                     label       (.. new-node
                                                     (append "text")
                                                     (attr "dy" ".35em")
                                                     (text #(gob/get % "title" "")))

                                     exit-node   (.. node
                                                     (exit)
                                                     (remove "g"))

                                     exit-link   (.. link
                                                     (exit)
                                                     (remove "line"))

                                     ]
                                 (println "Nodes count" (count nodes))
                                 (println "Links count" (count links))

                                 (.. simulation
                                     (nodes nodes)
                                     (on "tick" (ticked svg)))
                                 (.. simulation
                                     (force "link")
                                     (links links))
                                 (.. simulation
                                     (alphaTarget 0.3)
                                     (restart))

                                 )))}
        traces-ratom]
       [:hr]])))

