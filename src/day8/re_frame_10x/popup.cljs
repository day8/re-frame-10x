(ns day8.re-frame-10x.popup
  (:require [day8.re-frame-10x.components.re-com                          :as rc]
            [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core :as rf]
            [day8.re-frame-10x.components.buttons                         :as buttons]
            [day8.re-frame-10x.panels.app-db.subs                         :as app-db.subs]
            [day8.re-frame-10x.panels.app-db.events                       :as app-db.events]

            [day8.re-frame-10x.fx.clipboard                               :as clipboard]
            [day8.re-frame-10x.material                                   :as material]))

(defn overlay []
  [:div {:style    {:position         "absolute"
                    :left             0
                    :top              0
                    :height           "100%"
                    :width            "100%"
                    :background-color "rgba(0,0,0,0.25)"}
         :on-click #(rf/dispatch [::app-db.events/close-popup-menu])}])

(defn menu []
  (let [{[left top] :position
         :keys      [showing? path data data-path]} @(rf/subscribe [::app-db.subs/popup-menu])]
    (when showing?
      [:<>
       [overlay]
       [rc/v-box
        :style {:position   "absolute"
                :left       left
                :top        top
                :text-align "center"}
        :children
        [[buttons/icon {:icon     [material/data-array]
                        :label    "Copy Path"
                        :on-click #(do (some-> data-path clipboard/copy!)
                                       (rf/dispatch [::app-db.events/close-popup-menu]))}]
         [buttons/icon {:icon     [material/data-object]
                        :label    "Copy Subtree"
                        :on-click #(do (some-> data
                                               (get-in (drop (count path) (pop data-path)))
                                               clipboard/copy!)
                                       (rf/dispatch [::app-db.events/close-popup-menu]))}]
         [buttons/icon {:icon     [material/clojure]
                        :label    "Copy REPL Cmd"
                        :on-click #(do (clipboard/copy!
                                        (str
                                         `(simple-render-with-path-annotations
                                           ~{:data data :path path})))
                                       (rf/dispatch [::app-db.events/close-popup-menu]))}]]]])))
