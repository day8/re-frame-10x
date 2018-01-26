(ns mranderson047.reagent.v0v8v0-alpha2.reagent.dom.server
  (:require ["react-dom/server" :as dom-server]
            [mranderson047.reagent.v0v8v0-alpha2.reagent.impl.util :as util]
            [mranderson047.reagent.v0v8v0-alpha2.reagent.impl.template :as tmpl]
            [mranderson047.reagent.v0v8v0-alpha2.reagent.ratom :as ratom]
            [mranderson047.reagent.v0v8v0-alpha2.reagent.interop :refer-macros [$ $!]]))

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
