(ns day8.re-frame-10x.panels.app-db.views
  (:require-macros
    [day8.re-frame-10x.components.re-com                          :refer [handler-fn]])
  (:require
    [clojure.data]
    [devtools.prefs]
    [devtools.formatters.core]
    [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units   :refer [px]]
    [day8.re-frame-10x.inlined-deps.spade.git-sha-93ef290.core    :refer [defclass]]
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.components.buttons                         :as buttons]
    [day8.re-frame-10x.components.cljs-devtools                   :as cljs-devtools]
    [day8.re-frame-10x.components.re-com                          :as rc :refer [css-join]]
    [day8.re-frame-10x.svgs                                       :as svgs]
    [day8.re-frame-10x.material                                   :as material]
    [day8.re-frame-10x.styles                                     :as styles]
    [day8.re-frame-10x.panels.settings.subs                       :as settings.subs]
    [day8.re-frame-10x.panels.app-db.events                       :as app-db.events]
    [day8.re-frame-10x.panels.app-db.subs                         :as app-db.subs]
    [day8.re-frame-10x.panels.event.events                        :as event.events]
    [day8.re-frame-10x.tools.coll                                 :as tools.coll]
    [day8.re-frame-10x.fx.clipboard                               :as clipboard]))

(def pod-gap "-1px") ;; Overlap pods by 1px to avoid adjoining borders causing 2px borders
(def pod-padding "0px")

(defn path-inspector-button
  []
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [rc/button
     :class    (styles/button ambiance)
     :label    [rc/h-box
                :align    :center
                :children [[material/add
                            {:size styles/gs-19s}]
                           "path inspector"]]
     :on-click #(rf/dispatch [::app-db.events/create-path])]))

(defn panel-header []
  [rc/h-box
   :align    :center
   :gap "1"
   :children [[path-inspector-button]
              [buttons/icon
               {:icon     [material/content-copy]
                :label    "requires"
                :title    "Copy to the clipboard, the require form to set things up for the \"repl\" links below"
                :on-click #(do (clipboard/copy! "(require '[day8.re-frame-10x.components.cljs-devtools])")
                               (rf/dispatch [::event.events/repl-msg-state :start]))}]]])

(def pod-border-edge (str "1px solid " styles/nord4))

(defclass pod-header-section-style
  [_ last?]
  {:border-right (when-not last? [[(px 1) :solid styles/nord4]])
   #_#_:padding-left (px 3)})

(defn pod-header-section
  [& {:keys [size justify align gap width min-width children attr last?]
      :or   {size "none" justify :start align :center}}]
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
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

(defn pod-header [{:keys [id path-str open? diff? sort?]} data]
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [rc/h-box
     :class    (styles/section-header ambiance)
     :align    :center
     :height   styles/gs-31s
     :children
     [[pod-header-section
       :children
       [[rc/box
         :width  "30px"
         :height styles/gs-31s
         :justify :center
         :align :center
         :class  (styles/no-select)
         :style  {:cursor "pointer"}
         :attr   {:title    (str (if open? "Close" "Open") " the pod bay doors, HAL")
                  :on-click (handler-fn (rf/dispatch [::app-db.events/set-path-visibility id (not open?)]))}
         :child  [buttons/expansion {:open? open?
                                     :size styles/gs-31s}]]]]

      [rc/h-box
       :class (styles/path-header-style ambiance)
       :size  "auto"
       :style {:height       styles/gs-31s
               :border-right pod-border-edge}
       :align :center
       :children
       [[rc/input-text
         :class           (styles/path-text-input-style ambiance)
         :attr            {:on-blur #(rf/dispatch [::app-db.events/update-path-blur id])}
         :width           "100%"
         :model           path-str
         :on-change       #(rf/dispatch [::app-db.events/update-path id %]) ;;(fn [input-string] (rf/dispatch [:app-db/search-string input-string]))
         :on-submit       #()                   ;; #(rf/dispatch [::app-db.events/add-path %])
         :change-on-blur? false
         :placeholder     "enter an app-db path like [:todos 1]"]]]
      [pod-header-section
       :width    "49px"
       :justify  :center
       :align    :center
       :attr     {:on-click (handler-fn (rf/dispatch [::app-db.events/set-diff-visibility id (not diff?)]))}
       :children
       [[rc/checkbox
         :model diff?
         :label ""
         #_#_:style {:margin-left "6px"
                     :margin-top  "1px"}
         :on-change #(rf/dispatch [::app-db.events/set-diff-visibility id (not diff?)])]]]
      [pod-header-section
       :width    "49px"
       :justify  :center
       :align    :center
       :attr     {:on-click (handler-fn (rf/dispatch [::app-db.events/set-sort-form? id (not sort?)]))}
       :children
       [[rc/checkbox
         :model sort?
         :label ""
         :on-change #(rf/dispatch [::app-db.events/set-sort-form? id (not sort?)])]]]
      [pod-header-section
       :width    styles/gs-50s
       :justify  :center
       :children
       [[buttons/icon
         {:icon     [material/close]
          :title    "Remove this inspector"
          :on-click #(rf/dispatch [::app-db.events/remove-path id])}]]]
      [pod-header-section
       :width    styles/gs-31s
       :justify  :center
       :last?    true
       :children
       [[rc/box
         :style {:margin "auto"}
         :child
         [buttons/icon {:icon [material/print]
                        :on-click #(js/console.log data)}]]]]]]))

(def diff-url "https://github.com/day8/re-frame-10x/blob/master/docs/HyperlinkedInformation/Diffs.md")

(defn pod [{:keys [id path open? diff? sort?] :as pod-info}]
  (let [ambiance     @(rf/subscribe [::settings.subs/ambiance])
        render-diff? (and open? diff?)
        app-db-after (rf/subscribe [::app-db.subs/current-epoch-app-db-after])
        data         (tools.coll/get-in-with-lists @app-db-after path)]
    [rc/v-box
     :children
     [[pod-header pod-info data]
      [rc/v-box
       :class (when open? (styles/pod-border ambiance))
       :children
       [(when open?
          [rc/v-box
           :class (styles/pod-data ambiance)
           :style {:margin     (css-join pod-padding pod-padding "0px" pod-padding)
                   :overflow-x "auto"
                   :overflow-y "hidden"}
           :children
           [[cljs-devtools/simple-render-with-path-annotations
             data
             ["app-db-path" path]
             {:update-path-fn [::app-db.events/update-path id]
              :sort? sort?}]]])
        (when render-diff?
          (let [app-db-before (rf/subscribe [::app-db.subs/current-epoch-app-db-before])
                [diff-before diff-after _] (when render-diff?
                                             (clojure.data/diff (get-in @app-db-before path)
                                                                (get-in @app-db-after path)))]
            [rc/v-box
             :children
             [[rc/v-box
               :class    (styles/app-db-inspector-link ambiance)
               :justify  :end
               :children
               [[rc/hyperlink-href
                 :label "ONLY BEFORE"
                 :style {:margin-left styles/gs-7s}
                 :attr {:rel "noopener noreferrer"}
                 :target "_blank"
                 :href diff-url]]]
              [rc/v-box
               :style {:overflow-x "auto"
                       :overflow-y "hidden"}
               :children
               [[cljs-devtools/simple-render
                 diff-before
                 ["app-db-diff" path]]]]
              [rc/v-box
               :class    (styles/app-db-inspector-link ambiance)
               :justify  :end
               :children
               [[rc/hyperlink-href
                 ;:class  "app-db-path--label"
                 :label "ONLY AFTER"
                 :style {:margin-left styles/gs-7s}
                 :attr {:rel "noopener noreferrer"}
                 :target "_blank"
                 :href diff-url]]]
              [rc/v-box
               :style {:overflow-x "auto"
                       :overflow-y "hidden"}
               :children
               [[cljs-devtools/simple-render
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
   :children
   [[svgs/round-arrow]
    [rc/label
     :style {:width      "160px"
             :margin-top "22px"}
     :label "see the values in app-db by adding one or more inspectors"]]])


(defn pod-header-column-titles
  []
  [rc/h-box
   :height   styles/gs-19s
   :align    :center
   :children
   [[rc/gap-f :size styles/gs-31s]
    [rc/box
     :size  "1"
     :height "31px"
     :child ""]
    [rc/box
     :width   styles/gs-50s                                ;;  50px + 1 border
     :justify :center
     :child   [rc/label :style {:font-size "9px"} :label "DIFFS"]]
    [rc/box
     :width   styles/gs-50s                                ;;  50px + 1 border
     :justify :center
     :child   [rc/label :style {:font-size "9px"} :label "SORT"]]
    [rc/box
     :width   styles/gs-50s                                ;;  50px + 1 border
     :justify :center
     :child   [rc/label :style {:font-size "9px"} :label "DELETE"]]
    [rc/box
     :width   styles/gs-31s                                ;;  31px + 1 border
     :justify :center
     :child   [rc/label :style {:font-size "9px"} :label ""]]
    [rc/gap-f :size styles/gs-2s]
    #_[rc/gap-f :size "6px"]]])                     ;; Add extra space to look better when there is/aren't scrollbars

(defn pod-section []
  (let [pods @(rf/subscribe [::app-db.subs/paths])]
    [rc/v-box
     :size     "1"
     :children
     (into
       [(if (empty? pods)
          [no-pods]
          [pod-header-column-titles])]
       (for [p pods]
         [:<>
          [pod p]
          [rc/gap-f :size styles/gs-12s]]))]))

(defclass panel-style
  []
  {:margin-right styles/gs-5
   :overflow     :auto})

(defn panel [_]
  [rc/v-box
   :class    (panel-style)
   :size     "1"
   :children
   [[panel-header]
    [pod-section]
    [rc/gap-f :size styles/gs-19s]]])
