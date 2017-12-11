(ns day8.re-frame.trace.view.app-db
  (:require [reagent.core :as r]
            [clojure.string :as str]
            [devtools.prefs]
            [devtools.formatters.core]
            [day8.re-frame.trace.view.components :as components]
            [day8.re-frame.trace.utils.re-com :as re-com]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [day8.re-frame.trace.utils.re-com :as rc])
  (:require-macros [day8.re-frame.trace.utils.macros :as macros]))

(def delete (macros/slurp-macro "day8/re_frame/trace/images/delete.svg"))

(defn render-state [data]
  (let [subtree-input (r/atom "")
        subtree-paths (rf/subscribe [:app-db/paths])
        search-string (rf/subscribe [:app-db/search-string])
        input-error   (r/atom false)]
    (fn []
      [:div {:style {:flex "1 1 auto" :display "flex" :flex-direction "column"}}
       [:div.panel-content-scrollable
        [re-com/input-text
         :model search-string
         :on-change (fn [input-string] (rf/dispatch [:app-db/search-string input-string]))
         :on-submit #(rf/dispatch [:app-db/add-path %])
         :change-on-blur? false
         :placeholder ":path :into :app-db"]
        ;; TODO: check for input errors
        ; (if @input-error
        ;   [:div.input-error {:style {:color "red" :margin-top 5}}
        ;    "Please enter a valid path."])]]

        [:div.subtrees {:style {:margin "20px 0"}}
         (doall
           (map (fn [path]
                  ^{:key path}
                  [:div.subtree-wrapper {:style {:margin "10px 0"}}
                   [:div.subtree
                    [components/subtree
                     (get-in @data path)
                     [rc/h-box
                      :align :center
                      :children
                      [[:button.subtree-button
                        [:span.subtree-button-string
                         (str path)]]
                       [:img
                        {:src      (str "data:image/svg+xml;utf8," delete)
                         :style {:cursor "pointer"
                                 :height "10px"}
                         :on-click #(rf/dispatch [:app-db/remove-path path])}]]]
                     [path]]]])
                @subtree-paths))]
        [:div {:style {:margin-bottom "20px"}}
         [components/subtree @data [:span.label "app-db"] [:app-db]]]]])))
