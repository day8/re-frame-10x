(ns day8.re-frame.trace.view.subs
  (:require [day8.re-frame.trace.view.app-db :refer [pod-gap pod-padding border-radius]]
            [day8.re-frame.trace.utils.utils :as utils]
            [day8.re-frame.trace.utils.animated :as animated]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [mranderson047.reagent.v0v7v0.reagent.core :as r]
            [day8.re-frame.trace.utils.re-com :as rc :refer [css-join]]
            [day8.re-frame.trace.common-styles :as common]
            [day8.re-frame.trace.view.components :as components]
            [mranderson047.garden.v1v3v3.garden.units :as units])
  (:require-macros [day8.re-frame.trace.utils.macros :as macros]))

;(s/def ::query-v any?)
;(s/def ::dyn-v any?)
;(s/def ::query-cache-params (s/tuple ::query-v ::dyn-v))
;(s/def ::deref #(satisfies? IDeref %))
;(s/def ::query-cache (s/map-of ::query-cache-params ::deref))
;(assert (s/valid? ::query-cache (rc/deref-or-value-peek subs/query->reaction)))

(def copy (macros/slurp-macro "day8/re_frame/trace/images/copy.svg"))

(defn sub-tag-class [type]
  (case type
    :sub/create "rft-tag__subscription_created"
    :sub/dispose "rft-tag__subscription_destroyed"
    :sub/run "rft-tag__subscription_re_run"
    :sub/not-run "rft-tag__subscription_not_run"
    ""))

(def tag-types {:sub/create  {:long "CREATED" :short "C"}
                :sub/dispose {:long "DISPOSED" :short "D"}
                :sub/run     {:long "RUN" :short "R"}
                :sub/not-run {:long "NOT-RUN" :short "N"}
                nil          {:long "NIL" :short "NIL"}})

(def *finished-animation? (r/atom false))
(def animation-duration 150)

(defn long-tag-desc [type]
  (get-in tag-types [type :long] (str type)))

(defn short-tag-desc [type]
  (get-in tag-types [type :short] (str type)))

(defn sub-tag [type label]
  [components/tag (sub-tag-class type) label])

(defn short-sub-tag [type label]
  [components/tag (str (sub-tag-class type) " rft-tag__short") label])

(defn title-tag [type title label]
  [rc/v-box
   :class "noselect"
   :align :center
   :gap "2px"
   :children [[:span {:style {:font-size "9px"}} title]
              [components/tag (sub-tag-class type) label]]])

(defn panel-header []
  (let [created-count             (rf/subscribe [:subs/created-count])
        re-run-count              (rf/subscribe [:subs/re-run-count])
        destroyed-count           (rf/subscribe [:subs/destroyed-count])
        not-run-count             (rf/subscribe [:subs/not-run-count])
        ignore-unchanged-l2-subs? (rf/subscribe [:subs/ignore-unchanged-l2-subs?])
        ignore-unchanged-l2-count (rf/subscribe [:subs/unchanged-l2-subs-count])]
    [rc/h-box
     :justify :between
     :align :center
     :style {:flex-flow "row wrap"}
     :margin (css-join common/gs-19s "0px")
     :children [[rc/h-box
                 :align :center
                 :gap common/gs-19s
                 :height "48px"
                 :padding (css-join "0px" common/gs-19s)
                 :style {:background-color "#fafbfc"
                         :border           "1px solid #e3e9ed"
                         :border-radius    border-radius}
                 :children [[:span {:style {:color       "#828282"
                                            :font-size   "18px"
                                            :font-weight "lighter"}}
                             "Summary:"]
                            [title-tag :sub/create (long-tag-desc :sub/create) @created-count]
                            [title-tag :sub/run (long-tag-desc :sub/run) @re-run-count]
                            [title-tag :sub/dispose (long-tag-desc :sub/dispose) @destroyed-count]
                            [title-tag :sub/not-run (long-tag-desc :sub/not-run) @not-run-count]]]
                [rc/h-box
                 :align :center
                 :gap common/gs-19s
                 :height "48px"
                 :padding (css-join "0px" common/gs-19s)
                 :style {:background-color "#fafbfc"
                         :border           "1px solid #e3e9ed"
                         :border-radius    border-radius}
                 :children [[rc/checkbox
                             :model ignore-unchanged-l2-subs?
                             :label [:span "Ignore " [:b {:style {:font-weight "700"}} @ignore-unchanged-l2-count] " unchanged" [:br]
                                     [rc/link {:label (str "layer 2 " (utils/pluralize- @ignore-unchanged-l2-count "sub"))
                                               :href  "https://github.com/Day8/re-frame-trace/blob/master/docs/HyperlinkedInformation/UnchangedLayer2.md"}]]
                             :style {:margin-top "6px"}
                             :on-change #(rf/dispatch [:subs/ignore-unchanged-l2-subs? %])]]]]]))

(defn pod-header [{:keys [id layer path open? diff? run-times order]}]
  [rc/h-box
   :class (str "app-db-path--header " (when-not open? "rounded-bottom"))
   :align :center
   :height common/gs-31s
   :children [[rc/box
               :width "36px"
               :height common/gs-31s
               :class "noselect"
               :style {:cursor "pointer"}
               :attr {:title    (str (if open? "Close" "Open") " the pod bay doors, HAL")
                      :on-click #(rf/dispatch [:subs/open-pod? id (not open?)])}
               :child [rc/box
                       :margin "auto"
                       :child [:span.arrow (if open? "▼" "▶")]]]
              [rc/h-box
               :width "75px"
               :gap common/gs-5s
               :children (into []
                               (comp
                                 (take 3)
                                 (map (fn [o] [short-sub-tag o (short-tag-desc o)])))
                               order)]

              #_[rc/box
                 ;:width "64px"                                ;; (100-36)px from box above
                 :child [sub-tag (first order) (short-tag-desc (first order))]]

              ;; TODO: report if a sub was run multiple times
              #_(when run-times
                  [:span "Warning: run " run-times " times"])
              [rc/h-box
               :class "app-db-path--path-header"
               :size "auto"
               :children [[rc/input-text
                           :style {:height  "25px"
                                   :padding (css-join "0px" common/gs-7s)
                                   :width   "-webkit-fill-available"} ;; This took a bit of finding!
                           :width "100%"
                           :model path
                           :disabled? true]]]
              (when @(rf/subscribe [:settings/debug?])
                [rc/label :label (str id)])
              [rc/gap-f :size common/gs-12s]
              [rc/label :label (if (some? layer)
                                 (str "Layer " layer)
                                 [rc/link {:label "Layer ?"
                                           :href  "https://github.com/Day8/re-frame-trace/blob/master/docs/HyperlinkedInformation/UnchangedLayer2.md#why-do-i-sometimes-see-layer--when-viewing-a-subscription"}])]

              [rc/gap-f :size common/gs-12s]
              [rc/box
               :class "bm-muted-button app-db-path--button noselect"
               :attr {:title    "Show diff"
                      :on-click #(when open? (rf/dispatch [:subs/diff-pod? id (not diff?)]))}
               :child [:img
                       {:src   (str "data:image/svg+xml;utf8," copy)
                        :style {:width  "19px"
                                :margin "0px 3px"}}]]
              [rc/gap-f :size common/gs-12s]]])

(def no-prev-value-msg [:p {:style {:font-style "italic"}} "No previous value exists to diff"])
(def unchanged-value-msg [:p {:style {:font-style "italic"}} "Subscription value is unchanged"])
(def sub-not-run-msg [:p {:style {:font-style "italic"}} "Subscription not run, so no diff is available"])
(def not-run-yet-msg [rc/label :style {:font-style "italic"} :label "Subscription not run yet, so no value is available"])

(defn pod [{:keys [id layer path open? diff?] :as pod-info}]
  (let [render-diff?    (and open? diff?)
        value?          (contains? pod-info :value)
        previous-value? (contains? pod-info :previous-value)]
    [rc/v-box
     :style {:margin-bottom pod-gap
             :margin-right  "1px"}
     :children [[pod-header pod-info]
                [rc/v-box
                 :class (when open? "app-db-path--pod-border")
                 :children [[animated/component
                             (animated/v-box-options {:enter-animation "accordionVertical"
                                                      :leave-animation "accordionVertical"
                                                      :duration        animation-duration})
                             (when open?
                               (let [main-value (:value pod-info)
                                     #_(cond value? (:value pod-info)
                                             previous-value? (:previous-value pod-info)
                                             :else nil)]
                                 [rc/v-box
                                  :class (str "data-viewer" (when-not diff? " rounded-bottom"))
                                  :style {:margin     (css-join pod-padding pod-padding "0px" pod-padding)
                                          :overflow-x "auto"
                                          :overflow-y "hidden"}
                                  :children [(if (or value? #_ previous-value?)
                                               [components/simple-render
                                                main-value
                                                ["sub-path" path]]
                                               not-run-yet-msg
                                               )]]))]
                            [animated/component
                             (animated/v-box-options {:enter-animation "accordionVertical"
                                                      :leave-animation "accordionVertical"
                                                      :duration        animation-duration})
                             (when render-diff?
                               (let [diffable?        (and value? previous-value?)
                                     not-run?         (= (:order pod-info) [:sub/not-run])
                                     previous-value   (:previous-value pod-info)
                                     value            (:value pod-info)
                                     unchanged-value? (get-in pod-info [:sub/traits :unchanged?] false)
                                     [diff-before diff-after _] (clojure.data/diff previous-value value)]
                                 [rc/v-box
                                  :children [[rc/v-box
                                              :class "app-db-path--link"
                                              :justify :end
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
                                              :children [(cond
                                                           not-run? sub-not-run-msg
                                                           unchanged-value? unchanged-value-msg
                                                           diffable? [components/simple-render
                                                                      diff-before
                                                                      ["app-db-diff" path]]
                                                           :else no-prev-value-msg)]]
                                             [rc/v-box
                                              :class "app-db-path--link"
                                              :justify :end
                                              :children [[rc/hyperlink-href
                                                          ;:class  "app-db-path--label"
                                                          :label "ONLY AFTER"
                                                          :style {:margin-left common/gs-7s}
                                                          :attr {:rel "noopener noreferrer"}
                                                          :target "_blank"
                                                          :href utils/diff-link]]]
                                             [rc/v-box
                                              :class "data-viewer data-viewer--top-rule rounded-bottom"
                                              :style {:overflow-x "auto"
                                                      :overflow-y "hidden"}
                                              :children [(cond
                                                           not-run? sub-not-run-msg
                                                           unchanged-value? unchanged-value-msg
                                                           diffable? [components/simple-render
                                                                      diff-after
                                                                      ["app-db-diff" path]]
                                                           :else no-prev-value-msg)]]]]))]
                            (when open?
                              [rc/gap-f :size pod-padding])]]]]))

(defn no-pods []
  [rc/h-box
   :margin (css-join "0px 0px 0px" common/gs-19s)
   :gap common/gs-7s
   :align :start
   :align-self :start
   :children [[rc/label :label "There are no subscriptions to show"]]])

(defn pod-section []
  (let [visible-subs     @(rf/subscribe [:subs/visible-subs])
        inter-epoch-subs @(rf/subscribe [:subs/inter-epoch-subs])
        sub-expansions   @(rf/subscribe [:subs/sub-expansions])
        all-subs         (if @(rf/subscribe [:settings/debug?])
                           (cons {:path [:subs/current-epoch-sub-state] :id "debug" :value @(rf/subscribe [:subs/current-epoch-sub-state])} visible-subs)
                           visible-subs)]
    [rc/v-box
     ;:size "1"
     ;:gap pod-gap

     ;:children (if (empty? all-subs)
     ;            [[no-pods]]
     ;            (doall (for [p all-subs]
     ;                     ^{:key (:id p)}
     ;                     [pod (merge p (get sub-expansions (:id p)))])))

     :children [(if (and (empty? all-subs) @*finished-animation?)
                  [no-pods]
                  [rc/box :width "0px" :height "0px"])

                (for [p all-subs]
                  ^{:key (:id p)}
                  [pod (merge p (get sub-expansions (:id p)))])
                #_[animated/component
                   (animated/v-box-options {:on-finish #(reset! *finished-animation? true)
                                            :duration  animation-duration
                                            :style     {:flex       "1 1 0px"
                                                        :overflow-x "hidden"
                                                        :overflow-y "auto"}})

                   ]
                (when (seq inter-epoch-subs)
                  (list
                    ^{:key "inter-epoch-line"}
                    [rc/line :size "5px"
                     :style {:margin "19px 0px"}]
                    ^{:key "inter-epoch-title"}
                    [:h2 {:class "bm-heading-text"
                          :style {:margin "19px 0px"}}
                     [rc/link
                      {:href "https://github.com/Day8/re-frame-trace/blob/master/docs/HyperlinkedInformation/InterEpoch.md"
                       :label "Inter-Epoch Subscriptions"}]]

                    (for [p inter-epoch-subs]
                      ^{:key (:id p)}
                      [pod (merge p (get sub-expansions (:id p)))])))


                #_[animated/component
                   (animated/v-box-options {:on-finish #(reset! *finished-animation? true)
                                            :duration  animation-duration
                                            :style     {:flex       "1 1 0px"
                                                        :overflow-x "hidden"
                                                        :overflow-y "auto"}})

                   ]
                ]

     ]))

(defn render []
  []
  [rc/v-box
   :size "1"
   :style {:margin-right common/gs-19s
           ;:overflow     "hidden"
           }
   :children [[panel-header]
              [pod-section]
              [rc/gap-f :size pod-gap]]])

