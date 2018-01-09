(ns day8.re-frame.trace.view.app-db
  (:require [clojure.string :as str]
            [devtools.prefs]
            [devtools.formatters.core]
            [day8.re-frame.trace.view.components :as components]
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

(def cljs-dev-tools-background "#e8ffe8")

(defn top-buttons []
  [rc/h-box
   :justify  :between
   :margin   "19px 0px"
   :align    :center
   :children [[rc/button
               :class    "bm-muted-button"
               :style    {:width   "129px"
                          :padding "0px"}
               :label    [rc/v-box
                          :align    :center
                          :children ["+ path inspector"]]
               :on-click #()]
              [rc/h-box
               :align :center
               :gap "7px"
               :height  "48px"
               :padding "0px 12px"
               :style {:background-color "#fafbfc"
                       :border "1px solid #e8edf1"         ;; TODO: Need to get proper color from Figma
                       :border-radius "3px"}
               :children [[rc/label :label "reset app-db to:"]
                          [rc/button
                           :class    "bm-muted-button"
                           :style    {:width   "79px"
                                      :padding "0px"}
                           :label    [rc/v-box
                                      :align    :center
                                      :children ["initial state"]]
                           :on-click #()]
                          [rc/v-box
                           :width common/gs-81s
                           :align :center
                           :children [[rc/label :label "event"]
                                      [rc/line :style {:align-self "stretch"}] ;; TODO: Add arrow head
                                      [rc/label :label "processing"]]]
                          [rc/button
                           :class    "bm-muted-button"
                           :style    {:width   "79px"
                                      :padding "0px"}
                           :label    [rc/v-box
                                      :align    :center
                                      :children ["end state"]]
                           :on-click #()]]]]])

(defn path-header [p]
  (let [search-string (rf/subscribe [:app-db/search-string])]
    [rc/h-box
     :class "app-db-path--header"
     :style {:border-top-left-radius "3px"
             :border-top-right-radius "3px"}
     :align :center
     :height common/gs-31s
     :children [[rc/box
                 :width "36px"
                 :height "31px"
                 :child  [rc/box :margin "auto" :child [:span.arrow "▶"]]]
                [rc/h-box
                 :size "auto"
                 :class "app-db-path--path-header"
                 ;:style {:background-color "yellow"}
                 :children [[rc/input-text
                             ;:class           (str "app-db-path--path-text " (when (nil? p) "app-db-path--path-text__empty"))
                             :style           {:height           "25px"
                                               :padding          "0px 7px"
                                               ;:background-color "lightgreen"
                                               :width            "-webkit-fill-available"} ;; This took a bit of finding!
                             :width           "100%"
                             :model           search-string
                             :on-change       (fn [input-string] (rf/dispatch [:app-db/search-string input-string]))
                             :on-submit       #(rf/dispatch [:app-db/add-path %])
                             :change-on-blur? false
                             :placeholder     "Showing all of app-db. Try entering a path like [:todos 1]"]]]
                [rc/gap-f :size common/gs-12s]
                [rc/button
                 :class "bm-muted-button"
                 :style {:width "25px"
                         :height "25px"
                         :padding "0px"}
                 :label [:img
                         {:src      (str "data:image/svg+xml;utf8," snapshot-ready)
                          :style    {:cursor "pointer"
                                     :height "19px"
                                     :margin "3px"}
                          :on-click #() #_#(rf/dispatch [:app-db/remove-path path])}]
                 :on-click #()]
                [rc/gap-f :size common/gs-12s]
                [rc/button
                 :class "bm-muted-button"
                 :style {:width "25px"
                         :height "25px"
                         :padding "0px"}
                 :label [:img
                         {:src      (str "data:image/svg+xml;utf8," delete)
                          :style    {:cursor "pointer"
                                     :height "19px"
                                     :margin "3px"}
                          :on-click #() #_#(rf/dispatch [:app-db/remove-path path])}]
                 :on-click #()]
                [rc/gap-f :size common/gs-12s]]]))

(defn app-db-path [p]
  ^{:key (str p)}
  [rc/v-box
   :class "app-db-path"
   :style {:border-bottom-left-radius "3px"
           :border-bottom-right-radius "3px"}
   :children [[path-header p]
              [rc/v-box
               :height "90px"
               :style {:background-color cljs-dev-tools-background
                       :padding  "7px"
                       :margin "12px 12px 0px 12px"}
               :children ["---main-section---"]]

              [rc/v-box
               :height  "19px"
               :justify  :end
               :style    {:margin "0px 12px"}
               :children [[rc/label :class "app-db-path--label" :label "ONLY BEFORE"]]]
              [rc/v-box
               :height "60px"
               :style {:background-color cljs-dev-tools-background
                       :padding  "7px"
                       :margin "0px 12px"}
               :children ["---before-diff---"]]

              [rc/v-box
               :height   "19px"
               :justify  :end
               :style    {:margin "0px 12px"}
               :children [[rc/label :class "app-db-path--label" :label "ONLY AFTER"]]]
              [rc/v-box
               :height "60px"
               :style {:background-color cljs-dev-tools-background
                       :padding  "7px"
                       :margin "0px 12px"}
               :children ["---after-diff---"]]
              [rc/gap-f :size "12px"]]])

(defn paths []
  [rc/v-box
   :gap common/gs-31s
   :children (doall (for [p [["x" "y"] [:abc 123] nil]]
                      [app-db-path p]))])


(defn render-state [data]
  [rc/v-box
   :style    {:margin-right common/gs-19s}
   :children [[top-buttons]
              [paths]
              [rc/gap-f :size common/gs-19s]]])


(comment
  (defn old-render-state [data]
    (let [subtree-input   (r/atom "")
          subtree-paths   (rf/subscribe [:app-db/paths])
          search-string   (rf/subscribe [:app-db/search-string])
          input-error     (r/atom false)
          snapshot-ready? (rf/subscribe [:snapshot/snapshot-ready?])]
      (fn []
        [:div {:style {:flex "1 1 auto" :display "flex" :flex-direction "column"}}
         [:div.panel-content-scrollable
          [rc/input-text
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
           :children [[:img.nav-icon
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
                        :children [[:button.subtree-button
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
           [components/subtree @data [:span.label "app-db"] [:app-db]]]]]))))
