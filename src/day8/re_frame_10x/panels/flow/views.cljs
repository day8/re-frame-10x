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
   {:placeholder "filter subs"
    :on-change   #(rf/dispatch [::flow.events/set-filter (-> % .-target .-value)])}])

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
        [rc/gap-f :size styles/gs-5s]
        [rc/label
         :class    (styles/hyperlink ambiance)
         :on-click #(rf/dispatch [::epochs.events/reset])
         :label    "clear"]]]
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
                 (pp/truncate-string :middle 40))])]]
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
          ["Value"
           [rc/box :class (traces.views/table-row-expanded-style ambiance syntax-color-scheme)
            :child [cljs-devtools/simple-render (-> tags :new-db (get-in (:path (:flow-spec tags)))) []]]
           [rc/h-box :children
            ["Input values"
             [rc/gap-f :size styles/gs-31s]
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

(defn panel []
  (let [ambiance    @(rf/subscribe [::settings.subs/ambiance])
        flow-traces @(rf/subscribe [::flow.subs/visible-flows])]
    [rc/v-box
     :size     "1"
     :class    (traces.views/table-style ambiance)
     :children
     [[filter-section]
      [table-header]
      [rc/v-box
       :size     "1"
       :class    (traces.views/table-body-style ambiance)
       :children (into [] (->> flow-traces (map (fn [trace] [table-row trace]))))]]]))
