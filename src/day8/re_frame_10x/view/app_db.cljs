(ns day8.re-frame-10x.view.app-db
  (:require
    [clojure.data]
    [devtools.prefs]
    [devtools.formatters.core]
    [day8.re-frame-10x.utils.utils                                :as utils]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units   :refer [em px percent]]
    [day8.re-frame-10x.inlined-deps.spade.v1v1v0.spade.core       :refer [defclass defglobal]]
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.inlined-deps.reagent.v1v0v0.reagent.core   :as r]
    [day8.re-frame-10x.utils.re-com                               :as rc  :refer [close-button css-join]]
    [day8.re-frame-10x.svgs                                       :as svgs]
    [day8.re-frame-10x.material                                   :as material]
    [day8.re-frame-10x.styles                                     :as styles]
    [day8.re-frame-10x.view.cljs-devtools                         :as cljs-devtools]
    [day8.re-frame-10x.view.components                            :as components])
  (:require-macros
    [day8.re-frame-10x.utils.re-com :refer [handler-fn]]))

(def pod-gap "-1px") ;; Overlap pods by 1px to avoid adjoining borders causing 2px borders
(def pod-padding "0px")

(def border-radius "3px")

(defn path-inspector-button
  []
  (let [ambiance @(rf/subscribe [:settings/ambiance])]
    [rc/button
     :class    (styles/button ambiance)
     :label    [rc/h-box
                :align    :center
                :children [[material/add
                            {:size styles/gs-19s}]
                           "path inspector"]]
     :on-click #(rf/dispatch [:app-db/create-path])]))

(defn panel-header []
  [rc/h-box
   :align    :center
   :children [[path-inspector-button]]])

(def pod-border-edge (str "1px solid " styles/nord4))

(defclass pod-header-section-style
  [ambiance last?]
  {:border-right (when-not last? [[(px 1) :solid styles/nord4]])})

(defn pod-header-section
  [& {:keys [size justify align gap width min-width background-color children attr last?]
      :or   {size "none" justify :start align :center}}]
  (let [ambiance @(rf/subscribe [:settings/ambiance])]
    [rc/h-box
     :class      (pod-header-section-style ambiance last?)
     :size       size
     :justify    justify
     :align      align
     :gap        gap
     :width      width
     :min-width  min-width
     :height     styles/gs-31s
     :attr       attr
     :children   children]))

(defn pod-header [{:keys [id path path-str open? diff?]}]
  (let [ambiance @(rf/subscribe [:settings/ambiance])]
    [rc/h-box
     :class    (styles/section-header ambiance)
     :align    :center
     :height   styles/gs-31s
     :children [[pod-header-section
                 :children [[rc/box
                             :width  styles/gs-31s
                             :height styles/gs-31s
                             :class  (styles/no-select ambiance)
                             :style  {:cursor "pointer"}
                             :attr   {:title    (str (if open? "Close" "Open") " the pod bay doors, HAL")
                                      :on-click (handler-fn (rf/dispatch [:app-db/set-path-visibility id (not open?)]))}
                             :child  [rc/box
                                      :margin "auto"
                                      :child [:span.arrow (if open?
                                                            [material/arrow-drop-down
                                                             :fill styles/nord7]
                                                            [material/arrow-right
                                                             :fill styles/nord7])]]]]]

                [rc/h-box
                 :class (styles/path-header-style ambiance)
                 :size  "auto"
                 :style {:height       styles/gs-31s
                         :border-right pod-border-edge}
                 :align :center
                 :children [[rc/input-text
                             :class           (styles/path-text-input-style ambiance)
                             :attr            {:on-blur (fn [e] (rf/dispatch [:app-db/update-path-blur id]))}
                             :width           "100%"
                             :model           path-str
                             :on-change       #(rf/dispatch [:app-db/update-path id %]) ;;(fn [input-string] (rf/dispatch [:app-db/search-string input-string]))
                             :on-submit       #()                   ;; #(rf/dispatch [:app-db/add-path %])
                             :change-on-blur? false
                             :placeholder     "Showing all of app-db. Try entering a path like [:todos 1]"]]]
                [pod-header-section
                 :width    styles/gs-50s
                 :attr     {:on-click (handler-fn (rf/dispatch [:app-db/set-diff-visibility id (not diff?)]))}
                 :children [[rc/box
                             :style {:margin "auto"}
                             :child [rc/checkbox
                                     :model diff?
                                     :label ""
                                     :style {:margin-left "6px"
                                             :margin-top  "1px"}
                                     :on-change #(rf/dispatch [:app-db/set-diff-visibility id (not diff?)])]]]]
                [pod-header-section
                 :width    styles/gs-50s
                 :justify  :center
                 :last?    true
                 :children [[components/icon-button
                             {:icon     [material/close]
                              :title    "Remove this inspector"
                              :on-click #(rf/dispatch [:app-db/remove-path id])}]]]]]))


(defn pod [{:keys [id path open? diff?] :as pod-info}]
  (let [ambiance     @(rf/subscribe [:settings/ambiance])
        render-diff? (and open? diff?)
        app-db-after (rf/subscribe [:app-db/current-epoch-app-db-after])]
    [rc/v-box
     :style {:margin-bottom pod-gap
             :margin-right  "1px"}
     :children [[pod-header pod-info]
                [rc/v-box
                 :class (when open? (styles/pod-border ambiance))
                 :children [(when open?
                              [rc/v-box
                               :class (styles/pod-data ambiance)
                               :style {:margin     (css-join pod-padding pod-padding "0px" pod-padding)
                                       :overflow-x "auto"
                                       :overflow-y "hidden"}
                               :children [[cljs-devtools/simple-render
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

                                          #_"---main-section---"]])
                            (when render-diff?
                              (let [app-db-before (rf/subscribe [:app-db/current-epoch-app-db-before])
                                    [diff-before diff-after _] (when render-diff?
                                                                 (clojure.data/diff (get-in @app-db-before path)
                                                                                    (get-in @app-db-after path)))]
                                [rc/v-box
                                 :children [[rc/v-box
                                             :class    (styles/app-db-inspector-link ambiance)
                                             :justify  :end
                                             :children [[rc/hyperlink-href
                                                         ;:class  "app-db-path--label"
                                                         :label "ONLY BEFORE"
                                                         :style {:margin-left styles/gs-7s}
                                                         :attr {:rel "noopener noreferrer"}
                                                         :target "_blank"
                                                         :href utils/diff-link]]]
                                            [rc/v-box
                                             ;:class "data-viewer data-viewer--top-rule"
                                             :style {:overflow-x "auto"
                                                     :overflow-y "hidden"}
                                             :children [[cljs-devtools/simple-render
                                                         diff-before
                                                         ["app-db-diff" path]]]]
                                            [rc/v-box
                                             :class    (styles/app-db-inspector-link ambiance)
                                             :justify  :end
                                             :children [[rc/hyperlink-href
                                                         ;:class  "app-db-path--label"
                                                         :label "ONLY AFTER"
                                                         :style {:margin-left styles/gs-7s}
                                                         :attr {:rel "noopener noreferrer"}
                                                         :target "_blank"
                                                         :href utils/diff-link]]]
                                            [rc/v-box
                                             :class "data-viewer data-viewer--top-rule"
                                             :style {:overflow-x "auto"
                                                     :overflow-y "hidden"}
                                             :children [[cljs-devtools/simple-render
                                                         diff-after
                                                         ["app-db-diff" path]]]]]]))
                            (when open?
                              [rc/gap-f :size pod-padding])]]]]))


(defn no-pods []
  [rc/h-box
   :margin     (css-join styles/gs-19s " 0px 0px 50px")
   :gap        styles/gs-12s
   :align      :start
   :align-self :start
   :children   [[svgs/round-arrow]
                [rc/label
                 :style {:width      "160px"
                         :margin-top "22px"}
                 :label "see the values in app-db by adding one or more inspectors"]]])


(defn pod-header-column-titles
  []
  [rc/h-box
   :height   styles/gs-19s
   :align    :center
   :style    {:margin-right "1px"}
   :children [[rc/box
               :size  "1"
               :child ""]
              [rc/box
               :width   "51px"                                ;;  50px + 1 border
               :justify :center
               :child   [rc/label :style {:font-size "9px"} :label "DIFFS"]]
              [rc/box
               :width   "51px"                                ;;  50px + 1 border
               :justify :center
               :child   [rc/label :style {:font-size "9px"} :label "DELETE"]]
              [rc/gap-f :size "6px"]]])                     ;; Add extra space to look better when there is/aren't scrollbars


(defn pod-section []
  (let [pods @(rf/subscribe [:app-db/paths])]
    [rc/v-box
     :size     "1"
     :children [(if (empty? pods)
                  [no-pods]
                  [pod-header-column-titles])
                (for [p pods]
                  ^{:key (:id p)}
                  [pod p])]]))


(defn render [app-db]
  [rc/v-box
   :size "1"
   :style {:margin-right styles/gs-19s
           :overflow "auto"}
           ;:overflow     "hidden"

   :children [[panel-header]
              [pod-section]
              [rc/gap-f :size styles/gs-19s]]])
