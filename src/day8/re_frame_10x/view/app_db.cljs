(ns day8.re-frame-10x.view.app-db
  (:require [devtools.prefs]
            [devtools.formatters.core]
            [day8.re-frame-10x.utils.utils :as utils]
            [day8.re-frame-10x.utils.animated :as animated]
            [day8.re-frame-10x.view.components :as components]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [mranderson047.reagent.v0v7v0.reagent.core :as r]
            [day8.re-frame-10x.utils.re-com :as rc :refer [close-button css-join]]
            [day8.re-frame-10x.common-styles :as common]
            [clojure.data])
  (:require-macros [day8.re-frame-10x.utils.macros :as macros]
                   [day8.re-frame-10x.utils.re-com :refer [handler-fn]]))

(def delete (macros/slurp-macro "day8/re_frame_10x/images/delete.svg"))
(def snapshot (macros/slurp-macro "day8/re_frame_10x/images/snapshot.svg"))
(def snapshot-ready (macros/slurp-macro "day8/re_frame_10x/images/snapshot-ready.svg"))
(def round-arrow (macros/slurp-macro "day8/re_frame_10x/images/round-arrow.svg"))
(def arrow-right (macros/slurp-macro "day8/re_frame_10x/images/arrow-right.svg"))
(def copy (macros/slurp-macro "day8/re_frame_10x/images/copy.svg"))
(def trash (macros/slurp-macro "day8/re_frame_10x/images/trash.svg"))

(def cljs-dev-tools-background "#e8ffe8")
(def pod-gap "-1px") ;; Overlap pods by 1px to avoid adjoining borders causing 2px borders
(def pod-padding "0px")
(def pod-border-color "#e3e9ed")
(def pod-border-edge (str "1px solid " pod-border-color))
(def border-radius "3px")

(def *finished-animation? (r/atom true))
(def animation-duration 150)


(def app-db-styles
  [:#--re-frame-10x--
   #_[:.app-db-path
    {:background-color           common/white-background-color
     :border-bottom-left-radius  border-radius
     :border-bottom-right-radius border-radius}]

   [:.app-db-path--pod-border
    {:border-left                pod-border-edge
     :border-right               pod-border-edge
     :border-bottom              pod-border-edge}]

   [:.app-db-path--header
    {:background-color        "#fafbfc"
     :color                   "#b0b2b4"
     :border                  pod-border-edge
     :height                  common/gs-31}]

   [:.app-db-path--button
    {:width         "25px"
     :height        "25px"
     :padding       "0px"
     :border-radius "50%"
     :cursor        "pointer"}]

   [:.app-db-path--path-header
    {:background-color common/white-background-color
     :color            "#48494A"
     :margin           "3px"}]
   [:.app-db-path--path-text__empty
    {:font-style "italic"}]

   [:.app-db-path--link
    {:font-size "11px"
     :margin    (css-join "0px" pod-padding)
     :min-width "100px"
     :height    common/gs-19s}]
   #_[:.app-db-path--label
    {:color           "#2D9CDB"
     :text-decoration "underline"
     :font-size       "11px"
     :margin-bottom   "2px"}]

   [:.app-db-panel (common/panel-style border-radius)]

   [:.app-db-panel-button
    {:width   "129px"
     :padding "0px"}]
   [:.data-viewer
    {:background-color cljs-dev-tools-background
     :padding          "0px 2px"
     :margin           (css-join "0px" pod-padding)
     :min-width        "100px"}]
   [:.data-viewer--top-rule
    {#_#_:border-top  pod-border-edge}]])


(defn panel-header []
  [rc/h-box
   :align      :center
   :style      {:margin-top common/gs-19s}
   :children   [[rc/button
                 :class "bm-muted-button app-db-panel-button"
                 :label [rc/v-box
                         :align :center
                         :children ["+ path inspector"]]
                 :on-click #(rf/dispatch [:app-db/create-path])]]])


(defn pod-header-section
  [& {:keys [size justify align gap width min-width background-color children style attr last?]
      :or   {size "none" justify :start align :center}}]
  [rc/h-box
   :size      size
   :justify   justify
   :align     align
   :gap       gap
   :width     width
   :min-width min-width
   :height    common/gs-31s
   :style     (merge {:border-right     (when-not last? pod-border-edge)
                      :background-color background-color}
                     style)
   :attr      attr
   :children  children])


(defn pod-header [{:keys [id path path-str open? diff?]}]
  [rc/h-box
   :class    "app-db-path--header"
   :align    :center
   :height   common/gs-31s
   :children [[pod-header-section
               :children [[rc/box
                           :width  common/gs-31s
                           :height common/gs-31s
                           :class  "noselect"
                           :style  {:cursor "pointer"}
                           :attr   {:title    (str (if open? "Close" "Open") " the pod bay doors, HAL")
                                    :on-click (handler-fn (rf/dispatch [:app-db/set-path-visibility id (not open?)]))}
                           :child  [rc/box
                                    :margin "auto"
                                    :child [:span.arrow (if open? "▼" "▶")]]]]]

              [rc/h-box
               :background-color "white"
               :class    "app-db-path--path-header"
               :size     "auto"
               :style    {:height       common/gs-31s
                          :border-right pod-border-edge}
               :align    :center
               :children [[rc/input-text
                           :class (when (empty? path-str) "app-db-path--path-text__empty")
                           :style {:height  "25px"
                                   :border  "none"
                                   :padding (css-join "0px" common/gs-7s)
                                   :width   "-webkit-fill-available"} ;; This took a bit of finding!
                           :attr {:on-blur (fn [e] (rf/dispatch [:app-db/update-path-blur id]))}
                           :width "100%"
                           :model path-str
                           :on-change #(rf/dispatch [:app-db/update-path id %]) ;;(fn [input-string] (rf/dispatch [:app-db/search-string input-string]))
                           :on-submit #()                   ;; #(rf/dispatch [:app-db/add-path %])
                           :change-on-blur? false
                           :placeholder "Showing all of app-db. Try entering a path like [:todos 1]"]]]
              [pod-header-section
               :width "50px"
               :attr     {:on-click (handler-fn (rf/dispatch [:app-db/set-diff-visibility id (not diff?)]))}
               :children [[rc/box
                           :style {:margin "auto"}
                           :child [rc/checkbox
                                   :model     diff?
                                   :label     ""
                                   :style     {:margin-left "6px"
                                               :margin-top  "1px"}
                                   :on-change #(rf/dispatch [:app-db/set-diff-visibility id (not diff?)])]]]]
              [pod-header-section
               :width    "50px"
               :justify  :center
               :last?    true
               :children [[close-button
                           :div-size    31
                           :font-size   31
                           :left-offset 3
                           :top-offset  -4
                           :tooltip     "Remove this inspector"
                           :on-click    #(do (reset! *finished-animation? false)
                                             (rf/dispatch [:app-db/remove-path id]))]]]]])


(defn pod [{:keys [id path open? diff?] :as pod-info}]
  (let [render-diff?  (and open? diff?)
        app-db-after  (rf/subscribe [:app-db/current-epoch-app-db-after])]
    [rc/v-box
     :style    {:margin-bottom pod-gap
                :margin-right  "1px"}
     :children [[pod-header pod-info]
                [rc/v-box
                 :class    (when open? "app-db-path--pod-border")
                 :children [[animated/component
                             (animated/v-box-options
                               {:enter-animation "accordionVertical"
                                :leave-animation "accordionVertical"
                                :duration        animation-duration})
                             (when open?
                               [rc/v-box
                                :class "data-viewer"
                                :style {:margin     (css-join pod-padding pod-padding "0px" pod-padding)
                                        :overflow-x "auto"
                                        :overflow-y "hidden"}
                                :children [[components/simple-render
                                            (get-in @app-db-after path)
                                            ["app-db-path" path]

                                            #_{:todos [1 2 3]}
                                            #_(get-in @app-db path)
                                            #_[rc/h-box
                                               :align :center
                                               :children [[:button.subtree-button
                                                           [:span.subtree-button-string
                                                            (str path)]]
                                                          [:img
                                                           {:src      (str "data:image/svg+xml;utf8," delete)
                                                            :style    {:cursor "pointer"
                                                                       :height "10px"}
                                                            :on-click #(rf/dispatch [:app-db/remove-path path])}]]]
                                            #_[path]]

                                           #_"---main-section---"]])]
                            [animated/component
                             (animated/v-box-options
                               {:enter-animation "accordionVertical"
                                :leave-animation "accordionVertical"
                                :duration        animation-duration})
                             (when render-diff?
                               (let [app-db-before (rf/subscribe [:app-db/current-epoch-app-db-before])
                                     [diff-before diff-after _] (when render-diff?
                                                                  (clojure.data/diff (get-in @app-db-before path)
                                                                                     (get-in @app-db-after path)))]
                                 [rc/v-box
                                  :children [[rc/v-box
                                              :class    "app-db-path--link"
                                              :style    {:background-color cljs-dev-tools-background}
                                              :justify  :end
                                              :children [[rc/hyperlink-href
                                                          ;:class  "app-db-path--label"
                                                          :label "ONLY BEFORE"
                                                          :style {:margin-left common/gs-7s}
                                                          :attr {:rel "noopener noreferrer"}
                                                          :target "_blank"
                                                          :href utils/diff-link]]]
                                             [rc/v-box
                                              :class "data-viewer data-viewer--top-rule"
                                              :style {:overflow-x "auto"
                                                      :overflow-y "hidden"}
                                              :children [[components/simple-render
                                                          diff-before
                                                          ["app-db-diff" path]]]]
                                             [rc/v-box
                                              :class    "app-db-path--link"
                                              :style    {:background-color cljs-dev-tools-background}
                                              :justify  :end
                                              :children [[rc/hyperlink-href
                                                          ;:class  "app-db-path--label"
                                                          :label "ONLY AFTER"
                                                          :style {:margin-left common/gs-7s}
                                                          :attr {:rel "noopener noreferrer"}
                                                          :target "_blank"
                                                          :href utils/diff-link]]]
                                             [rc/v-box
                                              :class    "data-viewer data-viewer--top-rule"
                                              :style    {:overflow-x "auto"
                                                         :overflow-y "hidden"}
                                              :children [[components/simple-render
                                                          diff-after
                                                          ["app-db-diff" path]]]]]]))]
                            (when open?
                              [rc/gap-f :size pod-padding])]]]]))


(defn no-pods []
  [rc/h-box
   :margin (css-join common/gs-19s " 0px 0px 50px")
   :gap common/gs-12s
   :align :start
   :align-self :start
   :children [[:img {:src (str "data:image/svg+xml;utf8," round-arrow)}]
              [rc/label
               :style {:width      "160px"
                       :margin-top "22px"}
               :label "see the values in app-db by adding one or more inspectors"]]])


(defn pod-header-column-titles
  []
  [rc/h-box
   :height common/gs-19s
   :align :center
   :style {:margin-right "1px"}
   :children [[rc/box
               :size "1"
               :child ""]
              [rc/box
               :width "51px" ;;  50px + 1 border
               :justify :center
               :child [rc/label :style {:font-size "9px"} :label "DIFFS"]]
              [rc/box
               :width "51px" ;;  50px + 1 border
               :justify :center
               :child [rc/label :style {:font-size "9px"} :label "DELETE"]]
              [rc/gap-f :size "6px"]]]) ;; Add extra space to look better when there is/aren't scrollbars


(defn pod-section []
  (let [pods @(rf/subscribe [:app-db/paths])]
    [rc/v-box
     :size     "1"
     :children [(if (and (empty? pods) @*finished-animation?)
                  [no-pods]
                  [pod-header-column-titles])
                [animated/component
                 (animated/v-box-options
                   {:on-finish #(reset! *finished-animation? true)
                    :duration  animation-duration
                    :style     {:flex       "1 1 0px"
                                :overflow-x "hidden"
                                :overflow-y "auto"}})
                 (for [p pods]
                   ^{:key (:id p)}
                   [pod p])]]]))


(defn render [app-db]
  [rc/v-box
   :size  "1"
   :style {:margin-right common/gs-19s
           ;:overflow     "hidden"
           }
   :children [[panel-header]
              [pod-section]
              [rc/gap-f :size common/gs-19s]]])
