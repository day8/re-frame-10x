(ns day8.re-frame-10x.panels.app-db.views
  (:require-macros
   [day8.re-frame-10x.components.re-com                          :refer [handler-fn]])
  (:require
   [clojure.data]
   [devtools.prefs]
   [devtools.formatters.core]
   [re-frame.core :as userland.re-frame]
   [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.units   :refer [px]]
   [day8.re-frame-10x.inlined-deps.spade.git-sha-5197e54.core    :refer [defclass]]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core :as rf]
   [day8.re-frame-10x.components.buttons                         :as buttons]
   [day8.re-frame-10x.components.cljs-devtools                   :as cljs-devtools]
   [day8.re-frame-10x.components.hyperlinks                      :as hyperlinks]
   [day8.re-frame-10x.components.re-com                          :as rc :refer [css-join]]
   [day8.re-frame-10x.svgs                                       :as svgs]
   [day8.re-frame-10x.material                                   :as material]
   [day8.re-frame-10x.styles                                     :as styles]
   [day8.re-frame-10x.panels.settings.subs                       :as settings.subs]
   [day8.re-frame-10x.panels.app-db.events                       :as app-db.events]
   [day8.re-frame-10x.panels.app-db.subs                         :as app-db.subs]
   [day8.re-frame-10x.panels.event.events                        :as event.events]
   [day8.re-frame-10x.tools.datafy                               :refer [pr-str-safe serialize-special-types]]
   [day8.re-frame-10x.fx.clipboard                               :as clipboard]))

(def pod-gap "-1px") ;; Overlap pods by 1px to avoid adjoining borders causing 2px borders
(def pod-padding "0px")

(defn path-inspector-button
  []
  (let [ambiance             @(rf/subscribe [::settings.subs/ambiance])
        open-new-inspectors? @(rf/subscribe [::settings.subs/open-new-inspectors?])]
    [rc/button
     :class    (styles/button ambiance)
     :label    [rc/h-box
                :align    :center
                :children [[material/add
                            {:size styles/gs-19s}]
                           "path inspector"]]
     :on-click #(rf/dispatch [::app-db.events/create-path open-new-inspectors?])]))

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

(defn data-path-annotations []
  (let [render-path-annotations? @(rf/subscribe [::app-db.subs/data-path-annotations?])]
    [rc/h-box
     :align    :center
     :children [[rc/checkbox
                 :model     render-path-annotations?
                 :label     "data path annotations"
                 :on-change #(rf/dispatch [::app-db.events/set-data-path-annotations? %])]
                [rc/gap-f :size styles/gs-7s]
                [rc/box
                 :attr  {:title "When ticked, you can right-click on the rendered data (below) to obtain path data \n and cause focus etc. But this feature comes with a performance hit on rendering which \n is proportional to the size/depth of app-db. So, if your app-db is large and you are \n noticing a delay/pause in rendering app-db, untick this option to get better performance."}
                 :child [hyperlinks/info]]]]))

(def pod-border-edge (str "1px solid " styles/nord4))

(defn pod-header-section
  [& {:keys [size justify align gap width min-width children attr last?]
      :or   {size "none" justify :start align :center}}]
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [rc/h-box
     :class      "pod-header-section"
     :size       size
     :justify    justify
     :align      align
     :gap        gap
     :width      width
     :min-width  min-width
     :height     styles/gs-31s
     :attr       attr
     :children   children]))

(defn pod-header [{:keys [id path path-str open? diff? sort? editing? edit-str expand?]} data]
  (let [ambiance       @(rf/subscribe [::settings.subs/ambiance])
        log-any?       @(rf/subscribe [::settings.subs/any-log-outputs?])
        refresh-editor #(rf/dispatch-sync [::app-db.events/set-edit-str
                                           {:id       id
                                            :value    (serialize-special-types data)
                                            :refresh? true}])
        big-data?      (not @(rf/subscribe [::app-db.subs/small-data? {:id id}]))]
    [rc/v-box
     :children
     [[rc/h-box
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
           ;;:on-change     (fn [input-string] (rf/dispatch [:app-db/search-string input-string]))
           :on-change       #(rf/dispatch [::app-db.events/update-path {:path-str % :id id}])
           :on-submit       #()                   ;; #(rf/dispatch [::app-db.events/add-path %])
           :change-on-blur? false
           :placeholder     "enter an app-db path like [:todos 1]"]]]

        (when (> (count path) 0)
          [buttons/icon
           {:icon     [material/clear]
            :title    "Clear path in current inspector"
            :on-click #(rf/dispatch [::app-db.events/update-path {:id id :path ""}])}])

        (when (> (count path) 0)
          [rc/gap-f :size styles/gs-7s])

        (when (> (count path) 0)
          [buttons/icon
           {:icon     [material/arrow-drop-up]
            :title    "Open parent path in current inspector"
            :on-click #(rf/dispatch [::app-db.events/update-path
                                     {:id id :path (str (if (> (count path) 1) (pop path) ""))}])}])

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
         :width    "49px"
         :justify  :center
         :align    :center
         :children
         [(when (or (not big-data?) expand?)
            [buttons/icon {:icon     [(if expand? material/unfold-less material/unfold-more)]
                           :title    (str (if expand? "Close" "Expand") " all nodes in this inspector")
                           :on-click #(rf/dispatch [::app-db.events/expand {:id id}])}])]]
        [pod-header-section
         :width    styles/gs-50s
         :justify  :center
         :children
         [[buttons/icon
           {:icon     [material/close]
            :title    "Remove this inspector"
            :on-click #(rf/dispatch [::app-db.events/remove-path id])}]]]
        [pod-header-section
         :width styles/gs-50s
         :justify :center
         :children
         [[buttons/icon {:icon     [(if editing? material/unfold-less material/edit)]
                         :title    (str (if expand? "Close" "Expand") " the node editor")
                         :on-click (if editing?
                                     #(rf/dispatch [::app-db.events/finish-edit id])
                                     #(do (rf/dispatch [::app-db.events/start-edit id])
                                          (when-not big-data? (refresh-editor))))}]]]
        [pod-header-section
         :width    styles/gs-31s
         :justify  :center
         :last?    true
         :children
         [[rc/box
           :style {:margin "auto"}
           :child
           (when log-any?
             [buttons/icon {:icon [material/print]
                            :title    "Dump inspector data into DevTools"
                            :on-click #(rf/dispatch [:global/log data])}])]]]]]
      (when (and open? editing?)
        [rc/v-box
         :width "100%"
         :children
         [[rc/h-box
           :width "100%"
           :children
           [[:input {:type "file"
                     :accept ".edn"
                     :id (str "editor-file-" id)
                     :placeholder "Upload a file"
                     :style {:display "none" :width 0}
                     :on-change #(do (rf/dispatch [::app-db.events/open-file
                                                   (first (.-files (.-target %)))
                                                   [::app-db.events/set-edit-str
                                                    {:id id :refresh? true}]])
                                     (set! (.-value (.-target %)) ""))}]
            [:label {:for (str "editor-file-" id)}
             [:div {:style {:pointer-events "none"}}
              [buttons/icon
               {:title "Import an EDN file from disk"
                :label "Import"
                :icon [material/upload]}]]]

            [buttons/icon
             {:icon [material/download]
              :title "Export an EDN file for download"
              :label "Export"
              :on-click #(rf/dispatch [::app-db.events/save-to-file
                                       (cond-> "re-frame-db" path (str "__" path) :do (str ".edn"))
                                       (serialize-special-types data)])}]
            [buttons/icon
             {:icon [material/refresh]
              :label "Refresh"
              :title "Synchronize with the current value in app-db"
              :on-click refresh-editor}]
            [buttons/icon
             {:icon [material/check-circle-outline]
              :title "Set the value of app-db to the editor value."
              :label "Set!"
              :on-click #(userland.re-frame/dispatch
                          [::app-db.events/edit path edit-str])}]]]
          [:textarea
           {:default-value edit-str
            :style {:height 100
                    :width "100%"
                    :white-space "pre"
                    :font-family "monospace"}
            :key @(rf/subscribe [::app-db.events/editor-key id])
            :on-change #(rf/dispatch [::app-db.events/set-edit-str
                                      {:id id :value (.-value (.-target %))}])}]]])]]))

(def diff-url "https://github.com/day8/re-frame-10x/blob/master/docs/HyperlinkedInformation/Diffs.md")

(defn pod [{:keys [id path open? diff? sort? expand?] :as pod-info}]
  (let [ambiance     @(rf/subscribe [::settings.subs/ambiance])
        render-diff? (and open? diff?)
        app-db-after (rf/subscribe [::app-db.subs/current-epoch-app-db-after])
        data         @(rf/subscribe [::app-db.subs/path-data {:id id}])]
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
             {:data         data
              :expand?      expand?
              :path         path
              :path-id      id
              :sort?        sort?
              :db           @app-db-after}]]])
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
                 ["app-db-diff" path]
                 {:sort? sort?}]]]
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
                 ["app-db-diff" path]
                 {:sort? sort?}]]]]]))
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
   :height styles/gs-19s
   :align  :center
   :children
   [[rc/gap-f :size styles/gs-31s]
    [rc/box
     :size   "1"
     :height "31px"
     :child  ""]
    [rc/box
     :width   styles/gs-50s                                  ;;  50px + 1 border
     :justify :center
     :child   [rc/label :style {:font-size "9px"} :label "DIFFS"]]
    [rc/box
     :width   styles/gs-50s                                  ;;  50px + 1 border
     :justify :center
     :child   [rc/label :style {:font-size "9px"} :label "SORT"]]
    [rc/box
     :width   styles/gs-50s                                  ;;  50px + 1 border
     :justify :center
     :child   [rc/label :style {:font-size "9px"} :label "EXPAND"]]
    [rc/box
     :width   styles/gs-50s                                  ;;  50px + 1 border
     :justify :center
     :child   [rc/label :style {:font-size "9px"} :label "DELETE"]]
    [rc/box
     :width   styles/gs-50s                                  ;;  50px + 1 border
     :justify :center
     :child   [rc/label :style {:font-size "9px"} :label "EDIT"]]
    [rc/box
     :width   styles/gs-31s                                  ;;  31px + 1 border
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
   [[data-path-annotations]
    [rc/gap-f :size styles/gs-19s]
    [panel-header]
    [pod-section]
    [rc/gap-f :size styles/gs-19s]]])
