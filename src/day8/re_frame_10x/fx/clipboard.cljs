(ns day8.re-frame-10x.fx.clipboard)

(defn copy!
  [text]
  (let [el (.createElement js/document "textarea")]
    (set! (.-value el) text)
    (set! (-> el .-style .-position) "absolute")
    (set! (-> el .-style .-left) "-9999px")
    (.appendChild (.-body js/document) el)
    (.select el)
    (.execCommand js/document "copy")
    (.removeChild (.-body js/document) el)))