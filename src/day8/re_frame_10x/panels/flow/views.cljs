(ns day8.re-frame-10x.panels.flow.views
  (:require
   [clojure.string :as string]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core :as rf]
   [day8.re-frame-10x.components.re-com                          :as rc]
   [day8.re-frame-10x.material                                   :as material]
   [day8.re-frame-10x.components.data                            :as data]
   [day8.re-frame-10x.components.buttons                         :as buttons]
   [day8.re-frame-10x.components.inputs                          :as inputs]
   [day8.re-frame-10x.styles                                     :as styles]
   [day8.re-frame-10x.tools.datafy                               :as tools.datafy]
   [day8.re-frame-10x.panels.app-db.views                        :as app-db.views :refer [pod-gap pod-padding pod-border-edge
                                                                                          pod-header-section]]
   [day8.re-frame-10x.panels.flow.subs                           :as flow.subs]
   [day8.re-frame-10x.panels.flow.events                         :as flow.events]
   [day8.re-frame-10x.panels.settings.subs                       :as settings.subs]
   [day8.re-frame-10x.panels.traces.events                       :as traces.events]
   [day8.re-frame-10x.panels.traces.subs                         :as traces.subs]
   [day8.re-frame-10x.panels.subs.views                          :as subs.views]
   [day8.re-frame-10x.navigation.epochs.events                   :as epochs.events]
   [day8.re-frame-10x.panels.traces.views                        :as traces.views]
   [day8.re-frame-10x.tools.pretty-print-condensed               :as pp]
   [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.color   :as color]
   [day8.re-frame-10x.components.cljs-devtools                   :as cljs-devtools]))

(defn filter-section []
  [inputs/search
   {:placeholder "filter flows"
    :on-change #(rf/dispatch [::flow.events/set-filter (-> % .-target .-value)])}])

(defn table-header
  []
  (let [ambiance            @(rf/subscribe [::settings.subs/ambiance])
        flow-traces         @(rf/subscribe [::flow.subs/visible-flows])
        {:keys [show-all?]} @(rf/subscribe [::traces.subs/expansions])]
    [rc/h-box
     :height   styles/gs-31s
     :children
     [[rc/box
       :class   (traces.views/table-header-expansion-style ambiance)
       :align   :center
       :justify :center
       :width   styles/gs-31s
       :attr    {:on-click #(rf/dispatch [::traces.events/toggle-expansions])}
       :child
       (if show-all?
         [material/unfold-less]
         [material/unfold-more])]
      [rc/h-box
       :class     (traces.views/table-header-style ambiance)
       :align     :center
       :justify   :center
       :size      "1"
       :children
       [[rc/label :label (str (count flow-traces) " flows")]
        [rc/gap-f :size styles/gs-5s]]]
      [rc/box
       :class     (traces.views/table-header-style ambiance)
       :align     :center
       :justify   :center
       :width     styles/gs-81s
       :child
       [rc/label :label "meta"]]
      [rc/box
       :width styles/gs-31s
       :class (traces.views/table-header-style ambiance)]
      [rc/box
       :class (traces.views/table-header-style ambiance)
       :width "17px" ;; y scrollbar width
       :child ""]]]))

(defn alias-tree [m]
  (let [ns->alias @(rf/subscribe [::settings.subs/ns->alias])
        alias?    (and (seq ns->alias)
                       @(rf/subscribe [::settings.subs/alias-namespaces?]))]
    (cond-> m
      alias? (tools.datafy/alias-namespaces ns->alias))))

(defn colored-bardo [k]
  (let [colors {:live       styles/nord14
                :dead       styles/nord12
                :registered styles/nord15
                :cleared    styles/nord9}]
    [data/tag {:style {:background-color (colors k styles/nord5)
                       :color            :white}
               :label (name k)}]))

(defn flow-id-label [{:keys [tags op-type operation]}]
  [rc/h-box
   :size      "1"
   :class     (traces.views/clickable-table-cell-style op-type)
   :attr      {:on-click
               (fn [ev]
                 (rf/dispatch [::traces.events/add-query {:query (name operation) :type :contains}])
                 (.stopPropagation ev))}
   :children
   [[:span (pp/truncate 80 :middle (alias-tree (pp/str->namespaced-sym operation)))]
    (when-let [[_ & params] (or (get tags :query-v)
                                (get tags :event))]
      [:span
       (->> (map pp/pretty-condensed params)
            (string/join ", ")
            (pp/truncate-string :middle 40))])]])

(defn table-row
  [{:keys [op-type id operation tags duration] :as trace}]
  (let [ambiance            @(rf/subscribe [::settings.subs/ambiance])
        syntax-color-scheme @(rf/subscribe [::settings.subs/syntax-color-scheme])
        debug?              @(rf/subscribe [::settings.subs/debug?])
        expansions          @(rf/subscribe [::traces.subs/expansions])
        log-any?            @(rf/subscribe [::settings.subs/any-log-outputs?])
        expanded?           (get-in expansions [:overrides {:type      ::flow
                                                            :operation operation}]
                                    (:show-all? expansions))
        diff-inputs?        (get-in expansions [:overrides {:type      ::diff-inputs
                                                            :operation operation}])
        diff-live-inputs?   (get-in expansions [:overrides {:type      ::diff-live-inputs
                                                            :operation operation}])]
    [:<>
     [rc/h-box
      :class    (traces.views/table-row-style op-type)
      :height   styles/gs-19s
      :children
      [[rc/box
        :width   styles/gs-31s
        :class   (traces.views/table-row-expansion-style ambiance)
        :attr    {:on-click #(rf/dispatch [::traces.events/toggle-expansion
                                           {:type      ::flow
                                            :operation operation}])}
        :justify :center
        :child
        (if expanded?
          [material/arrow-drop-down]
          [material/arrow-right])]
       [flow-id-label trace]
       [colored-bardo (first (:transition tags))]
       "→"
       [colored-bardo (last (:transition tags))]
       [rc/gap-f :size "31px"]
       [rc/box
        :width styles/gs-81s
        :child
        (if debug?
          [:span (:reaction (:tags trace)) "/" id]
          [:span (.toFixed duration 1) " ms"])]
       [rc/box
        :align   :center
        :justify :center
        :size    styles/gs-31s
        :child
        (when log-any?
          [buttons/icon {:icon     [material/print]
                         :on-click #(rf/dispatch [:global/log tags])}])]]]
     (when expanded?
       [rc/h-box
        :children
        [[rc/gap-f :size styles/gs-31s]
         [rc/v-box :size  "1"
          :children
          [[rc/box :class (traces.views/table-row-expanded-style ambiance syntax-color-scheme)
            :child [cljs-devtools/simple-render (-> tags :new-db (get-in (:path (:flow-spec tags)))) []]]
           [rc/h-box :children
            ["Input values"
             [rc/gap-f :size "1"]
             [:input.toggle
              {:type      "checkbox"
               :checked   diff-inputs?
               :on-change #(rf/dispatch [::traces.events/toggle-expansion
                                         {:type      ::diff-inputs
                                          :operation operation}])}]
             [rc/label :label "diff?"]]]
           [rc/box :class (traces.views/table-row-expanded-style ambiance syntax-color-scheme)
            :child [cljs-devtools/simple-render (-> tags :id->in) []]]
           (when diff-inputs?
             (let [[before after _] @(rf/subscribe [::flow.subs/inputs-diff id])]
               [:<>
                [data/diff-label :before]
                [cljs-devtools/simple-render before]
                [data/diff-label :after]
                [cljs-devtools/simple-render after]]))
           [rc/h-box :children
            ["Live-input values"
             [rc/gap-f :size styles/gs-31s]
             [:input.toggle
              {:type      "checkbox"
               :checked   diff-live-inputs?
               :on-change #(rf/dispatch [::traces.events/toggle-expansion
                                         {:type      ::diff-live-inputs
                                          :operation operation}])}]
             [rc/label :label "diff?"]]]
           [rc/box :class (traces.views/table-row-expanded-style ambiance syntax-color-scheme)
            :child [cljs-devtools/simple-render (-> tags :id->live-in) []]]
           (when diff-live-inputs?
             (let [[before after _] @(rf/subscribe [::flow.subs/live-inputs-diff id])]
               [:<>
                [data/diff-label :before]
                [cljs-devtools/simple-render before]
                [data/diff-label :after]
                [cljs-devtools/simple-render after]]))]]]])]))

(defn pod-header [{:keys [op-type id operation tags duration] :as trace}]
  (let [ambiance          @(rf/subscribe [::settings.subs/ambiance])
        log-any?          @(rf/subscribe [::settings.subs/any-log-outputs?])
        expansions        @(rf/subscribe [::traces.subs/expansions])
        expanded?         (get-in expansions [:overrides {:type      ::flow
                                                          :operation operation}]
                                  (:show-all? expansions))
        diff-inputs?      (get-in expansions [:overrides {:type      ::diff-inputs
                                                          :operation operation}])
        diff-live-inputs? (get-in expansions [:overrides {:type      ::diff-live-inputs
                                                          :operation operation}])]
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
           :attr   {:title    (str (if expanded? "Close" "Open") " the pod bay doors, HAL")
                    :on-click #(rf/dispatch [::traces.events/toggle-expansion
                                             {:type      ::flow
                                              :operation operation}])}
           :child  [buttons/expansion {:open? expanded?
                                       :size  styles/gs-31s}]]]]

        [rc/h-box
         :class (styles/path-header-style ambiance)
         :size  "auto"
         :style {:height       styles/gs-31s
                 :border-right pod-border-edge}
         :align :center
         :children
         [[flow-id-label trace]]]
        [pod-header-section
         :width    "131px"
         :justify  :center
         :align    :center
         :children
         [[colored-bardo (first (:transition tags))]
          "→"
          [colored-bardo (last (:transition tags))]]]
        [pod-header-section
         :width    "49px"
         :justify  :center
         :align    :center
         :attr     {:on-click #(rf/dispatch [::traces.events/toggle-expansion
                                             {:type      ::diff-inputs
                                              :operation operation}])}
         :children
         [[rc/checkbox
           :model diff-inputs?
           :label ""
           :on-change  #(rf/dispatch [::traces.events/toggle-expansion
                                      {:type      ::diff-live-inputs
                                       :operation operation}])]]]
        ;; [pod-header-section
        ;;  :width    "49px"
        ;;  :justify  :center
        ;;  :align    :center
        ;;  :children
        ;;  [(when (or (not big-data?) expand?)
        ;;     [buttons/icon {:icon     [(if expand? material/unfold-less material/unfold-more)]
        ;;                    :title    (str (if expand? "Close" "Expand") " all nodes in this inspector")
        ;;                    :on-click #(rf/dispatch [::app-db.events/expand {:id id}])}])]]
        [pod-header-section
         :width    styles/gs-31s
         :justify  :center
         :last?    true
         :children
         [[rc/box
           :style {:margin "auto"}
           :child
           (when log-any?
             [buttons/icon {:icon     [material/print]
                            :title    "Dump inspector data into DevTools"
                            :on-click #(rf/dispatch [:global/log trace])}])]]]]]]]))

(defn pod [{:keys [id operation tags] :as trace}]
  (let [expansions          @(rf/subscribe [::traces.subs/expansions])
        expanded?           (get-in expansions [:overrides {:type      ::flow
                                                            :operation operation}]
                                    (:show-all? expansions))
        syntax-color-scheme @(rf/subscribe [::settings.subs/syntax-color-scheme])
        ambiance            @(rf/subscribe [::settings.subs/ambiance])
        diff-inputs?        (get-in expansions [:overrides {:type      ::diff-inputs
                                                            :operation operation}])
        diff-live-inputs?   (get-in expansions [:overrides {:type      ::diff-live-inputs
                                                            :operation operation}])]
    [rc/v-box
     :children
     [[pod-header trace]
      [rc/v-box
       :class (when expanded? (styles/pod-border ambiance))
       :children
       [(when expanded?
          [rc/v-box
           :class (styles/pod-data ambiance)
           :style {:margin     (rc/css-join pod-padding pod-padding "0px" pod-padding)
                   :overflow-x "auto"
                   :overflow-y "hidden"}
           :children
           [[cljs-devtools/simple-render (-> tags :new-db (get-in (:path (:flow-spec tags)))) []]
            (when expanded? [rc/gap-f :size "12px"])
            [:strong {:style {:background-color :unset :margin-bottom "5px"}} "Inputs:"]
            [rc/box :class (traces.views/table-row-expanded-style ambiance syntax-color-scheme)
             :child [cljs-devtools/simple-render (:id->in tags) []]]]])
       (when (and expanded? diff-inputs?)
         (let [[before after _] @(rf/subscribe [::flow.subs/inputs-diff id])]
            [rc/v-box
             :children
             [[data/diff-label :before]
              [rc/box :style {:overflow-x "auto" :overflow-y "hidden"}
               :child
               [cljs-devtools/simple-render before]]
              [data/diff-label :after]
              [rc/box :style {:overflow-x "auto" :overflow-y "hidden"}
               :child
               [cljs-devtools/simple-render after]]]]))
        (when expanded? [rc/gap-f :size "12px"])
        (when expanded?
          [rc/v-box
           :children
           [[:strong {:style {:background-color :unset :margin-bottom "5px"}} "Live Inputs:"]
            [rc/box :class (traces.views/table-row-expanded-style ambiance syntax-color-scheme)
             :child [cljs-devtools/simple-render (:id->live-in tags) []]]]])
        (when (and expanded? diff-inputs?)
          (let [[before after _] @(rf/subscribe [::flow.subs/live-inputs-diff id])]
            [rc/v-box
             :children
             [[data/diff-label :before]
              [rc/box :style {:overflow-x "auto" :overflow-y "hidden"}
               :child
               [cljs-devtools/simple-render before]]
              [data/diff-label :after]
              [rc/box :style {:overflow-x "auto" :overflow-y "hidden"}
               :child
               [cljs-devtools/simple-render after]]]]))
        (when expanded?
          [rc/gap-f :size pod-padding])]]]]))

#_(defn panel []
  (let [ambiance    @(rf/subscribe [::settings.subs/ambiance])
        flow-traces @(rf/subscribe [::flow.subs/visible-flows])]
    [rc/v-box
     :size "1 1 auto"
     :background-color :red
     :children
     []
     #_[[filter-section]
      [rc/gap-f :size "31px"]
      [rc/v-box
       :size "1 1 auto"
       :style {:background-color :red}
       :class    (traces.views/table-style ambiance)
       :children
       [[table-header]
        [rc/v-box
         :style {:overflow-y :auto}
         :class    (traces.views/table-body-style ambiance)
         :children (into [] (->> flow-traces (map (fn [trace] [pod trace]))))]]]]]))

(defn no-pods []
  [rc/h-box
   :margin     (rc/css-join "0px 0px 0px" styles/gs-19s)
   :gap        styles/gs-7s
   :align      :start
   :align-self :start
   :children   [[rc/label :label "There are no flows to show."]]])

(defn pod-header-column-titles
  []
  [rc/h-box
   :height   styles/gs-19s
   :align    :center
   :style    {:margin-right "1px"}
   :children [[rc/box
               :width styles/gs-31s
               :child ""]
              [rc/box
               :size    "1"
               :justify :center
               :child
               [rc/label :class (subs.views/column-title-label-style) :label "ID"]]
              [rc/box
               :width styles/gs-50s
               :child ""]
              [rc/box
               :width   "51px"                                ;;  50px + 1 border
               :justify :center
               :child
               [rc/label :class (subs.views/column-title-label-style) :label "LIFECYCLE"]]
              [rc/box
               :width   "51px"                                ;;  50px + 1 border
               :justify :center
               :child
               [rc/label :class (subs.views/column-title-label-style) :label "DIFFS"]]
              [rc/box
               :width   "32px"                                ;; styles/gs-31s + 1 border
               :justify :center
               :child
               [rc/label :class (subs.views/column-title-label-style) :label ""]]
              [rc/gap-f :size "6px"]]])

(defn pod-section []
  (let [ambiance    @(rf/subscribe [::settings.subs/ambiance])
        flow-traces @(rf/subscribe [::flow.subs/visible-flows])]
    [rc/v-box
     :size     "1"
     :class    "pod-section"
     :children
     [(if (empty? flow-traces)
        [no-pods]
        [pod-header-column-titles])
      [rc/v-box
       :size "auto"
       :style {:overflow-x "hidden"
               :overflow-y "auto"}
       :children
       [[rc/v-box
         :children
         (for [trace flow-traces]
           ^{:key (:id trace)}
           [pod trace])]]]]]))

(defn panel []
  (let [ambiance @(rf/subscribe [::settings.subs/ambiance])]
    [rc/v-box
     :class    (subs.views/panel-style ambiance)
     :size     "1"
     :children
     [[filter-section]
      [pod-section]
      [rc/gap-f :size styles/gs-19s]]]))
