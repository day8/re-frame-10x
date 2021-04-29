(ns day8.re-frame-10x.fx.scroll
  (:require
    [goog.fx.dom :as fx]))

(defn scroll! [el start end time]
  (.play (fx/Scroll. el (clj->js start) (clj->js end) time)))

(defn end? [el tolerance]
  ;; at-end?: element.scrollHeight - element.scrollTop === element.clientHeight
  (> tolerance (- (.-scrollHeight el) (.-scrollTop el) (.-clientHeight el))))
