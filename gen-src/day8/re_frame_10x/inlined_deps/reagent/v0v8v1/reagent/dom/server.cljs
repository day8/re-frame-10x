(ns ^{:mranderson/inlined true} day8.re-frame-10x.inlined-deps.reagent.v0v8v1.reagent.dom.server
  (:require ["react-dom/server" :as dom-server]
            [day8.re-frame-10x.inlined-deps.reagent.v0v8v1.reagent.impl.util :as util]
            [day8.re-frame-10x.inlined-deps.reagent.v0v8v1.reagent.impl.template :as tmpl]
            [day8.re-frame-10x.inlined-deps.reagent.v0v8v1.reagent.ratom :as ratom]
            [day8.re-frame-10x.inlined-deps.reagent.v0v8v1.reagent.interop :refer-macros [$ $!]]))

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
