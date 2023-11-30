(ns day8.re-frame-10x.fx.clipboard
  (:require
   [day8.re-frame-10x.fx.window :refer [popout-window]]))

(defn copy!
  [text]
  (let [doc (or (some-> @popout-window .-document) js/document)
        el (.createElement doc "textarea")]
    (set! (.-value el) text)
    (set! (-> el .-style .-position) "absolute")
    (set! (-> el .-style .-left) "-9999px")
    (.appendChild (.-body doc) el)
    (.select el)
    (.execCommand doc "copy")
    (.removeChild (.-body doc) el)))
