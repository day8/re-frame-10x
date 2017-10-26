(ns day8.re-frame.trace.components
  (:require [reagent.core :as r]
            [clojure.string :as str]
            [goog.fx.dom :as fx]))

(defn search-input [{:keys [title placeholder on-save on-change on-stop]}]
  (let [val  (r/atom title)
        save #(let [v (-> @val str str/trim)]
                (when (pos? (count v))
                  (on-save v)))]
    (fn []
      [:input {:type        "text"
               :value       @val
               :auto-focus  true
               :placeholder placeholder
               :size        (if (> 20 (count (str @val)))
                              25
                              (count (str @val)))
               :on-change   #(do (reset! val (-> % .-target .-value))
                                 (on-change %))
               :on-key-down #(case (.-which %)
                               13 (do
                                    (save)
                                    (reset! val ""))
                               nil)}])))

(defn scroll! [el start end time]
  (.play (fx/Scroll. el (clj->js start) (clj->js end) time)))

(defn scrolled-to-end? [el tolerance]
  ;; at-end?: element.scrollHeight - element.scrollTop === element.clientHeight
  (> tolerance (- (.-scrollHeight el) (.-scrollTop el) (.-clientHeight el))))

(defn autoscroll-list [{:keys [class scroll?]} child]
  "Reagent component that enables scrolling for the elements of its child dom-node.
   Scrolling is only enabled if the list is scrolled to the end.
   Scrolling can be set as option for debugging purposes.
   Thanks to Martin Klepsch! Original code can be found here:
       https://gist.github.com/martinklepsch/440e6fd96714fac8c66d892e0be2aaa0"
  (let [node          (r/atom nil)
        should-scroll (r/atom true)]
    (r/create-class
      {:display-name "autoscroll-list"
       :component-did-mount
                     (fn [_]
                       (scroll! @node [0 (.-scrollTop @node)] [0 (.-scrollHeight @node)] 0))
       :component-will-update
                     (fn [_]
                       (reset! should-scroll (scrolled-to-end? @node 100)))
       :component-did-update
                     (fn [_]
                       (when (and scroll? @should-scroll)
                         (scroll! @node [0 (.-scrollTop @node)] [0 (.-scrollHeight @node)] 500)))
       :reagent-render
                     (fn [{:keys [class]} child]
                       [:div {:class class :ref (fn [dom-node]
                                                  (reset! node dom-node))}
                        child])})))
