(ns mranderson048.reagent.v0v8v0.reagent.dom.server
  (:require ["react-dom/server" :as dom-server]
            [mranderson048.reagent.v0v8v0.reagent.impl.util :as util]
            [mranderson048.reagent.v0v8v0.reagent.impl.template :as tmpl]
            [mranderson048.reagent.v0v8v0.reagent.ratom :as ratom]
            [mranderson048.reagent.v0v8v0.reagent.interop :refer-macros [$ $!]]))

(defonce ^:private imported nil)

(defn render-to-string
  "Turns a component into an HTML string."
  [component]
  (ratom/flush!)
  (binding [util/*non-reactive* true]
    (dom-server/renderToString (tmpl/as-element component))))

(defn render-to-static-markup
  "Turns a component into an HTML string, without data-react-id attributes, etc."
  [component]
  (ratom/flush!)
  (binding [util/*non-reactive* true]
    (dom-server/renderToStaticMarkup (tmpl/as-element component))))
