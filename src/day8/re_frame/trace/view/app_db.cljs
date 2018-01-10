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
(def round-arrow (macros/slurp-macro "day8/re_frame/trace/images/round-arrow.svg"))
(def arrow-right (macros/slurp-macro "day8/re_frame/trace/images/arrow-right.svg"))
(def copy (macros/slurp-macro "day8/re_frame/trace/images/copy.svg"))
(def trash (macros/slurp-macro "day8/re_frame/trace/images/trash.svg"))

(def cljs-dev-tools-background "#e8ffe8")
(def pod-gap common/gs-19s)

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
               :on-click #(println "Clicked [+ path inspector]")]
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
                           :on-click #(println "Clicked [initial state]")]
                          [rc/v-box
                           :width common/gs-81s
                           :align :center
                           :children [[rc/label
                                       :style {:font-size  "9px"}
                                       :label "EVENT"]
                                      [:img {:src (str "data:image/svg+xml;utf8," arrow-right)}]
                                      [rc/label
                                       :style {:font-size  "9px"
                                               :margin-top "-1px"}
                                       :label "PROCESSING"]]]
                          [rc/button
                           :class    "bm-muted-button"
                           :style    {:width   "79px"
                                      :padding "0px"}
                           :label    [rc/v-box
                                      :align    :center
                                      :children ["end state"]]
                           :on-click #(println "Clicked [end state]")]]]]])

(defn path-header [p]
  (let [search-string (rf/subscribe [:app-db/search-string])
        *pod-open     (r/atom true)]
    (fn [p]
      [rc/h-box
       :class "app-db-path--header"
       :style {:border-top-left-radius  "3px"
               :border-top-right-radius "3px"}
       :align :center
       :height common/gs-31s
       :children [[rc/box
                   :width  "36px"
                   :height "31px"
                   :class  "noselect"
                   :style  {:cursor "pointer"}
                   :attr   {:title    (str (if @*pod-open "close" "open") " this pod")
                            :on-click (rc/handler-fn
                                        (swap! *pod-open not)
                                        (println "Clicked [arrow]"))}
                   :child  [rc/box
                            :margin "auto"
                            :child  [:span.arrow (if @*pod-open "▼" "▶")]]]
                  [rc/h-box
                   :size "auto"
                   :class "app-db-path--path-header"
                   ;:style {:background-color "yellow"}
                   :children [[rc/input-text
                               ;:class           (str "app-db-path--path-text " (when (nil? p) "app-db-path--path-text__empty"))
                               :style {:height  "25px"
                                       :padding "0px 7px"
                                       ;:background-color "lightgreen"
                                       :width   "-webkit-fill-available"} ;; This took a bit of finding!
                               :width "100%"
                               :model search-string
                               :on-change (fn [input-string] (rf/dispatch [:app-db/search-string input-string]))
                               :on-submit #(rf/dispatch [:app-db/add-path %])
                               :change-on-blur? false
                               :placeholder "Showing all of app-db. Try entering a path like [:todos 1]"]]]
                  [rc/gap-f :size common/gs-12s]
                  [rc/box
                   :class "bm-muted-button noselect"
                   :style {:width         "25px"
                           :height        "25px"
                           :padding       "0px"
                           :border-radius "3px"
                           :cursor        "pointer"}
                   :attr  {:title    "Show diff"
                           :on-click (rc/handler-fn (println "Clicked [copy]"))}
                   :child [:img
                           {:src   (str "data:image/svg+xml;utf8," copy)
                            :style {:width  "21px"
                                    :margin "6px 2px"}}]]
                  [rc/gap-f :size common/gs-12s]
                  [rc/box
                   :class "bm-muted-button noselect"
                   :style {:width         "25px"
                           :height        "25px"
                           :padding       "0px"
                           :border-radius "3px"
                           :cursor        "pointer"}
                   :attr  {:title    "Remove this pod"
                           :on-click (rc/handler-fn
                                       ;(rf/dispatch [:app-db/remove-path %])
                                       (println "Clicked [delete]"))}
                   :child [:img
                           {:src   (str "data:image/svg+xml;utf8," trash)
                            :style {:width  "16px"
                                    :margin "3px 4px"}}]]
                  [rc/gap-f :size common/gs-12s]]])))

(defn app-db-path [p]
  ^{:key (str p)}
  [rc/v-box
   :class "app-db-path"
   :style {:border-bottom-left-radius "3px"
           :border-bottom-right-radius "3px"}
   :children [[path-header p]
              [rc/v-box
               :height    "90px"
               :min-width "100px"
               :style     {:background-color cljs-dev-tools-background
                           :padding  "7px"
                           :margin "12px 12px 0px 12px"}
               :children  ["---main-section---"]]

              [rc/v-box
               :height  "19px"
               :justify  :end
               :style    {:margin "0px 12px"}
               :children [[rc/hyperlink
                           ;:class "app-db-path--label"
                           :label "ONLY BEFORE"
                           :on-click #(println "Clicked [ONLY BEFORE]")]]]
              [rc/v-box
               :height    "60px"
               :min-width "100px"
               :style     {:background-color cljs-dev-tools-background
                           :padding  "7px"
                           :margin "0px 12px"}
               :children  ["---before-diff---"]]

              [rc/v-box
               :height   "19px"
               :justify  :end
               :style    {:margin "0px 12px"}
               :children [[rc/hyperlink
                           ;:class "app-db-path--label"
                           :label "ONLY AFTER"
                           :on-click #(println "Clicked [ONLY AFTER]")]]]
              [rc/v-box
               :height    "60px"
               :min-width "100px"
               :style     {:background-color cljs-dev-tools-background
                           :padding  "7px"
                           :margin "0px 12px"}
               :children  ["---after-diff---"]]
              [rc/gap-f :size "12px"]]])

(defn paths []
  (let [
        pods [["x" "y"] [:abc 123] nil]
        ;pods nil
        ]
    [rc/v-box
     :gap      pod-gap
     :children (if (empty? pods)
                 [[rc/h-box
                   :margin     "0px 0px 0px 19px"
                   :gap        common/gs-7s
                   :align      :start
                   :align-self :start
                   :children   [[:img {:src (str "data:image/svg+xml;utf8," round-arrow)}]
                                [rc/label
                                 :style {:width "150px"
                                         :margin-top "22px"}
                                 :label "add inspectors to show what happened to app-db"]]]]
                 (doall (for [p pods] [app-db-path p])))]))


(defn render-state [data]
  [rc/v-box
   :style    {:margin-right common/gs-19s}
   :children [[top-buttons]
              [paths]
              [rc/gap-f :size pod-gap]]])


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
