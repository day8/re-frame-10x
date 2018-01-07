(ns day8.re-frame.trace.view.app-db
  (:require [clojure.string :as str]
            [devtools.prefs]
            [devtools.formatters.core]
            [day8.re-frame.trace.view.components :as components]
            [day8.re-frame.trace.utils.re-com :as re-com]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [mranderson047.reagent.v0v6v0.reagent.core :as r]
            [day8.re-frame.trace.utils.re-com :as rc]
            [day8.re-frame.trace.common-styles :as common])
  (:require-macros [day8.re-frame.trace.utils.macros :as macros]))

(def delete (macros/slurp-macro "day8/re_frame/trace/images/delete.svg"))
(def reload (macros/slurp-macro "day8/re_frame/trace/images/reload.svg"))
(def reload-disabled (macros/slurp-macro "day8/re_frame/trace/images/reload-disabled.svg"))
(def snapshot (macros/slurp-macro "day8/re_frame/trace/images/snapshot.svg"))
(def snapshot-ready (macros/slurp-macro "day8/re_frame/trace/images/snapshot-ready.svg"))

(defn top-buttons []
  [rc/h-box
   :justify :between
   :children [[:button "+ path viewer"]
              [rc/h-box
               :align :center
               :children
               [[rc/label :label "reset app-db to:"]
                [:button "initial state"]
                [rc/v-box
                 :width common/gs-81s
                 :align :center
                 :children [[rc/label :label "event"]
                            ;; TODO: arrow doesn't show up when there is an alignment
                            [rc/line]
                            [rc/label :label "processing"]]]
                [:button "end state"]]]]])

(defn path-header [p]
  [rc/h-box
   :class "app-db-path--header"
   :align :center
   :gap common/gs-12s
   :children [">"
              [rc/h-box
               :size "auto"
               :class "app-db-path--path-header"
               :children
               [[rc/label
                 :class (str "app-db-path--path-text " (when (nil? p) "app-db-path--path-text__empty"))
                 :label (if (some? p)
                          (prn-str p)
                          "Showing all of app-db. Try entering a path like [:todos 1]")]]]
              [:button "diff"]
              [:button "trash"]]])

(defn app-db-path [p]
  ^{:key (str p)}
  [rc/v-box
   :class "app-db-path"
   :children
   [[path-header p]
    [rc/label :label "Main data"]
    ;; TODO: Make these into hyperlinks
    [rc/label :class "app-db-path--label" :label "Only Before:"]
    [rc/label :label "Before diff"]
    [rc/label :class "app-db-path--label" :label "Only After"]
    [rc/label :label "After diff"]]])

(defn paths []
  [rc/v-box
   :gap common/gs-31s
   :children
   (doall (for [p [["x" "y"] [:abc 123] nil]]
              [app-db-path p]))])


(defn render-state [data]
  [rc/v-box
   :gap common/gs-31s
   :children
   [[top-buttons]
    [paths]]])


(defn old-render-state [data]
  (let [subtree-input   (r/atom "")
        subtree-paths   (rf/subscribe [:app-db/paths])
        search-string   (rf/subscribe [:app-db/search-string])
        input-error     (r/atom false)
        snapshot-ready? (rf/subscribe [:snapshot/snapshot-ready?])]
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

        [rc/h-box
         :children
         [[:img.nav-icon
           {:title    "Load app-db snapshot"
            :class    (when-not @snapshot-ready? "inactive")
            :src      (str "data:image/svg+xml;utf8,"
                           (if @snapshot-ready?
                             reload
                             reload-disabled))
            :on-click #(when @snapshot-ready? (rf/dispatch-sync [:snapshot/load-snapshot]))}]
          [:img.nav-icon
           {:title    "Snapshot app-db"
            :class    (when @snapshot-ready? "active")
            :src      (str "data:image/svg+xml;utf8,"
                           (if @snapshot-ready?
                             snapshot-ready
                             snapshot))
            :on-click #(rf/dispatch-sync [:snapshot/save-snapshot])}]]]

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
                         :style    {:cursor "pointer"
                                    :height "10px"}
                         :on-click #(rf/dispatch [:app-db/remove-path path])}]]]
                     [path]]]])
                @subtree-paths))]
        [:div {:style {:margin-bottom "20px"}}
         [components/subtree @data [:span.label "app-db"] [:app-db]]]]])))
