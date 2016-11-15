(ns day8.re-frame.trace.d3
  (:require [reagent.core :as r]))

(defn no-op [desc]
  (fn [& args] nil))

(defn component-did-update [{:keys [d3-update]} ratoms]
  (apply d3-update ratoms))

(defn component-did-mount [{:keys [d3-once] :as lifecycle-fns} ratoms]
  (apply d3-once ratoms)
  (component-did-update lifecycle-fns ratoms))

(defn create-d3
  "Creates a bridging component from Reagent to D3. Takes a map of
  lifecycle functions, and reactive data sources.

  :render-component - Render the outer Reagent component, and a place for your D3 component to mount to (probably an SVG element)
  :d3-once - Function called after the component has been rendered, for you to setup anything you need in D3 (e.g. adding <g> or classes)
  :d3-update - Run the D3 general update pattern: https://bl.ocks.org/mbostock/3808218
  "
  [{:keys [render-component d3-once d3-update]
    :or   {render-component (no-op :render)
           d3-once          (no-op :d3-did-mount)
           d3-update        (no-op :d3-update)}}
   & ratoms]
  (let [lifecycle-fns {:render-component render-component
                       :d3-once          d3-once
                       :d3-update        d3-update}]
    (r/create-class
      {:reagent-render       (fn []
                               (doseq [r ratoms] (deref r))
                               (apply render-component ratoms))
       :component-did-mount  (fn [this]
                               (component-did-mount lifecycle-fns ratoms))
       :component-did-update (fn [this old-argv]
                               (component-did-update lifecycle-fns ratoms))})))
