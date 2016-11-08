(ns day8.re-frame.trace.d3
  (:require [reagent.core :as r]))

(defn component-did-update [{:keys [d3-enter d3-update d3-exit]} ratoms]
  (apply d3-enter ratoms)
  (apply d3-update ratoms)
  (apply d3-exit ratoms))

(defn component-did-mount [{:keys [d3-did-mount] :as lifecycle-fns} ratoms]
  (apply d3-did-mount ratoms)
  (component-did-update lifecycle-fns ratoms))

(defn no-op [desc]
  (fn [& args]
    (println "No-op" desc)))

(defn create-d3
  "Creates a bridging component from Reagent to D3. Takes a map of
  lifecycle functions, and reactive data sources.

  :render-component - Render the outer Reagent component, and a place for your D3 component to mount to (probably an SVG element)
  :d3-did-mount - Function called after the component has been rendered, for you to setup anything you need in D3 (e.g. adding <g> or classes)
  :d3-enter, :d3-update, :d3-exit - correspond to functions in the D3 general update pattern: https://bl.ocks.org/mbostock/3808218
  "
  [{:keys [render-component d3-did-mount d3-enter d3-update d3-exit]
    :or   {render-component no-op
           d3-did-mount     (no-op :d3-did-mount)
           d3-enter         (no-op :d3-enter)
           d3-update        (no-op :d3-update)
           d3-exit          (no-op :d3-exit)}}
   & ratoms]
  (let [lifecycle-fns {:render-component render-component
                       :d3-did-mount     d3-did-mount
                       :d3-enter         d3-enter
                       :d3-update        d3-update
                       :d3-exit          d3-exit}]
    (r/create-class
      {:reagent-render       (fn []
                               (doseq [r ratoms] (deref r))
                               (apply render-component ratoms))
       :component-did-mount  (fn [this] (component-did-mount lifecycle-fns ratoms))
       :component-did-update (fn [this old-argv] (component-did-update lifecycle-fns ratoms))}))
  )


;;;;
;;;;
;;; app
;
;
;(def data (r/atom {}))
;
;(defn my-render [ratom]
;  (let [width  100
;        height 100]
;    [:div
;     {:id "barchart"}
;     [:svg
;      {:width  width
;       :height height}]]))
;
;(defn bars-did-mount [ratom]
;  (-> (js/d3.select "#barchart svg")
;      (.append "g")
;      (.attr "class" "container")
;      (.append "g")
;      (.attr "class" "bars")))
;
;(defn bars-enter [ratom]
;  (let [data (get-data ratom)]
;    (-> (js/d3.select "#barchart svg .container .bars")
;        (.selectAll "rect")
;        (.data (clj->js data))
;        .enter
;        (.append "rect"))))
;
;(defn bars-update [ratom]
;  (let [width       (get-width ratom)
;        height      (get-height ratom)
;        data        (get-data ratom)
;        data-n      (count data)
;        rect-height (/ height data-n)
;        x-scale     (-> js/d3
;                        .scaleLinear
;                        (.domain #js [0 5])
;                        (.range #js [0 width]))]
;    (-> (js/d3.select "#barchart svg .container .bars")
;        (.selectAll "rect")
;        (.data (clj->js data))
;        (.attr "fill" "green")
;        (.attr "x" (x-scale 0))
;        (.attr "y" (fn [_ i]
;                     (* i rect-height)))
;        (.attr "height" (- rect-height 1))
;        (.attr "width" (fn [d]
;                         (x-scale (aget d "x")))))))
;
;(defn bars-exit [ratom]
;  (let [data (get-data ratom)]
;    (-> (js/d3.select "#barchart svg .container .bars")
;        (.selectAll "rect")
;        (.data (clj->js data))
;        .exit
;        .remove)))
;
;(create-d3 data {:reagent-render my-render
;                 :d3-did-mount   bars-did-mount
;                 :d3-enter       bars-enter
;                 :d3-update      bars-update
;                 :d3-exit        bars-exit})
