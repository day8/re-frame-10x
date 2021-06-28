(ns day8.re-frame-10x.tools.shadow-dom)

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
        (.setAttribute container "id" id)
        (.appendChild body container)
        (js/window.focus container)
        shadow-root))))