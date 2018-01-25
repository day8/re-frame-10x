(ns mranderson047.reagent.v0v7v0.reagent.dom.server
  (:require [cljsjs.react.dom.server]
            [mranderson047.reagent.v0v7v0.reagent.impl.util :as util]
            [mranderson047.reagent.v0v7v0.reagent.impl.template :as tmpl]
            [mranderson047.reagent.v0v7v0.reagent.ratom :as ratom]
            [mranderson047.reagent.v0v7v0.reagent.interop :refer-macros [$ $!]]))

(defonce ^:private imported nil)

(defn module []
  (cond
    (some? imported) imported
    (exists? js/ReactDOMServer) (set! imported js/ReactDOMServer)
    (exists? js/require) (or (set! imported (js/require "react-dom/server"))
                             (throw (js/Error.
                                     "require('react-dom/server') failed")))
    :else
    (throw (js/Error. "js/ReactDOMServer is missing"))))


(defn render-to-string
  "Turns a component into an HTML string."
  [component]
  (ratom/flush!)
  (binding [util/*non-reactive* true]
    ($ (module) renderToString (tmpl/as-element component))))

(defn render-to-static-markup
  "Turns a component into an HTML string, without data-react-id attributes, etc."
  [component]
  (ratom/flush!)
  (binding [util/*non-reactive* true]
    ($ (module) renderToStaticMarkup (tmpl/as-element component))))
