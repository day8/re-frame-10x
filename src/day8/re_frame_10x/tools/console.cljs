(ns day8.re-frame-10x.tools.console)

(defn spy
  ([x]
   (js/console.log x)
   x)
  ([label x]
   (js/console.log label x)
   x))