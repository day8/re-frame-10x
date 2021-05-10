(ns day8.re-frame-10x.tools.shadow-dom
  (:require [day8.re-frame-10x.inlined-deps.spade.v1v1v0.spade.runtime :as spade.runtime]))

(defn shadow-root
  "Creates a new div element with the id attached to the js-document <body>,
   attaches a shadow DOM tree and returns a reference to its ShadowRoot."
  [js-document id]
  (let [container (.getElementById js-document id)]
    (if container
      (.-shadowRoot container)
      (let [body        (.-body js-document)
            container   (.createElement js-document "div")
            shadow-root (.attachShadow container #js {:mode "open"})]
        ;; Reset the Spade target to the ShadowRoot and empty the record of
        ;; previously injected styles so everything is injected again as
        ;; rendering occurs.
        (reset! spade.runtime/*dom* shadow-root)
        (reset! spade.runtime/*injected* {})
        (.setAttribute container "id" id)
        (.appendChild body container)
        (js/window.focus container)
        shadow-root))))