(ns day8.re-frame-10x.fx.scroll
  (:require
    [goog.fx.dom :as fx]))

(defn scroll! [el start end time]
  (.play (fx/Scroll. el (clj->js start) (clj->js end) time)))

(defn scroll-y-parent-to! [el time]
  ;; [IJ] TODO: This is not quite right...
  (let [parent     (.-parentNode el)
        start-y    (.-scrollTop parent)
        el-pos     (.getBoundingClientRect el)
        parent-pos (.getBoundingClientRect parent)
        end-y      (- (.-top el-pos) (.-top parent-pos))]
    (scroll! parent [0 start-y] [0 end-y] time)))

(defn end? [el tolerance]
  ;; at-end?: element.scrollHeight - element.scrollTop === element.clientHeight
  (> tolerance (- (.-scrollHeight el) (.-scrollTop el) (.-clientHeight el))))
