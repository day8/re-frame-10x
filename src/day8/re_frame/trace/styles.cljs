(ns day8.re-frame.trace.styles
  (:require-macros [day8.re-frame.trace.utils.macros :as macros]))

(def panel-styles (macros/slurp-macro "day8/re_frame/trace/main.css"))

(defn inject-styles [document]
  (let [id            "--re-frame-trace-styles--"
        styles-el     (.getElementById document id)
        new-styles-el (.createElement document "style")
        new-styles    panel-styles]
    (.setAttribute new-styles-el "id" id)
    (-> new-styles-el
        (.-innerHTML)
        (set! new-styles))
    (if styles-el
      (-> styles-el
          (.-parentNode)
          (.replaceChild new-styles-el styles-el))
      (let []
        (.appendChild (.-head document) new-styles-el)
        new-styles-el))))
