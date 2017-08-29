(ns day8.re-frame.trace.components
  (:require [reagent.core :as r]
            [goog.fx.dom :as fx]))

(defn icon-add []
  [:svg.icon.icon-add
    {:viewBox "0 0 32 32"}
    [:title "add"]
    [:path
      {:d "M31 12h-11v-11c0-0.552-0.448-1-1-1h-6c-0.552 0-1 0.448-1 1v11h-11c-0.552 0-1 0.448-1 1v6c0 0.552 0.448 1 1 1h11v11c0 0.552 0.448 1 1 1h6c0.552 0 1-0.448 1-1v-11h11c0.552 0 1-0.448 1-1v-6c0-0.552-0.448-1-1-1z"}]])

(defn icon-remove []
  [:svg.icon.icon-remove
    {:viewBox "0 0 32 32"}
    [:title "remove"]
    [:path
      {:d "M31.708 25.708c-0-0-0-0-0-0l-9.708-9.708 9.708-9.708c0-0 0-0 0-0 0.105-0.105 0.18-0.227 0.229-0.357 0.133-0.356 0.057-0.771-0.229-1.057l-4.586-4.586c-0.286-0.286-0.702-0.361-1.057-0.229-0.13 0.048-0.252 0.124-0.357 0.228 0 0-0 0-0 0l-9.708 9.708-9.708-9.708c-0-0-0-0-0-0-0.105-0.104-0.227-0.18-0.357-0.228-0.356-0.133-0.771-0.057-1.057 0.229l-4.586 4.586c-0.286 0.286-0.361 0.702-0.229 1.057 0.049 0.13 0.124 0.252 0.229 0.357 0 0 0 0 0 0l9.708 9.708-9.708 9.708c-0 0-0 0-0 0-0.104 0.105-0.18 0.227-0.229 0.357-0.133 0.355-0.057 0.771 0.229 1.057l4.586 4.586c0.286 0.286 0.702 0.361 1.057 0.229 0.13-0.049 0.252-0.124 0.357-0.229 0-0 0-0 0-0l9.708-9.708 9.708 9.708c0 0 0 0 0 0 0.105 0.105 0.227 0.18 0.357 0.229 0.356 0.133 0.771 0.057 1.057-0.229l4.586-4.586c0.286-0.286 0.362-0.702 0.229-1.057-0.049-0.13-0.124-0.252-0.229-0.357z"}]])

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
                         (scroll! @node [0 (.-scrollTop @node)] [0 (.-scrollHeight @node)] 1600)))
       :reagent-render
                     (fn [{:keys [class]} child]
                       [:div {:class class :ref (fn [dom-node]
                                                  (reset! node dom-node))}
                        child])})))