(ns day8.re-frame.trace.view.subs
  (:require [day8.re-frame.trace.view.app-db :refer [pod-gap pod-padding border-radius]]
            [day8.re-frame.trace.utils.utils :as utils]
            [day8.re-frame.trace.utils.animated :as animated]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [mranderson047.reagent.v0v8v0-alpha2.reagent.core :as r]
            [day8.re-frame.trace.utils.re-com :as rc :refer [css-join]]
            [day8.re-frame.trace.common-styles :as common]
            [day8.re-frame.trace.view.components :as components])
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
    :created   "rft-tag__subscription_created"
    :destroyed "rft-tag__subscription_destroyed"
    :re-run    "rft-tag__subscription_re_run"
    :not-run   "rft-tag__subscription_not_run"
    ""))

(def tag-types {:created   {:long "CREATED" :short "CREATED"}
                :destroyed {:long "DESTROYED" :short "DESTROY"}
                :re-run    {:long "RE-RUN" :short "RE-RUN"}
                :not-run   {:long "NOT-RUN" :short "NOT-RUN"}})

(def *finished-animation? (r/atom false))
(def animation-duration 150)

(defn long-tag-desc [type]
  (get-in tag-types [type :long] "???"))

(defn short-tag-desc [type]
  (get-in tag-types [type :short] "???"))

(defn sub-tag [type label]
  [components/tag (sub-tag-class type) label])

(defn title-tag [type title label]
  [rc/v-box
   :class    "noselect"
   :align    :center
   :gap      "2px"
   :children [[:span {:style {:font-size "9px"}} title]
              [components/tag (sub-tag-class type) label]]])

(defn panel-header []
  (let [created-count             (rf/subscribe [:subs/created-count])
        re-run-count              (rf/subscribe [:subs/re-run-count])
        destroyed-count           (rf/subscribe [:subs/destroyed-count])
        not-run-count             (rf/subscribe [:subs/not-run-count])
        ignore-unchanged?         (rf/subscribe [:subs/ignore-unchanged-subs?])
        ignore-unchanged-l2-count (rf/subscribe [:subs/unchanged-l2-subs-count])]
    [rc/h-box
     :justify :between
     :align :center
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
                            [title-tag :created (long-tag-desc :created) @created-count]
                            [title-tag :re-run (long-tag-desc :re-run) @re-run-count]
                            [title-tag :destroyed (long-tag-desc :destroyed) @destroyed-count]
                            ;; TODO: capture not-run traces
                            #_[title-tag :not-run (long-tag-desc :not-run) @not-run-count]]]
                [rc/h-box
                 :align :center
                 :gap common/gs-19s
                 :height "48px"
                 :padding (css-join "0px" common/gs-19s)
                 :style {:background-color "#fafbfc"
                         :border           "1px solid #e3e9ed"
                         :border-radius    border-radius}
                 :children [[rc/checkbox
                             :model ignore-unchanged?
                             ;; TODO: change from l2 subs to ignored l2 subs
                             :label [:span "Ignore " [:b {:style {:font-weight "700"}} @ignore-unchanged-l2-count] #_ " unchanged" [:br] "layer 2 subs "

                                     [:a
                                      {:rel    "noopener noreferrer"
                                       :class "rc-hyperlink-href noselect "
                                       :href "https://github.com/Day8/re-frame-trace/blob/master/docs/HyperlinkedInformation/UnchangedLayer2.md"
                                       :target "_blank"}
                                      "?"]]
                             :style {:margin-top "6px"}
                             :on-change #(rf/dispatch [:subs/ignore-unchanged-subs? %])]]]]]))

(defn pod-header [{:keys [id type layer path open? diff? run-times]}]
  [rc/h-box
   :class    (str "app-db-path--header " (when-not open? "rounded-bottom"))
   :align    :center
   :height   common/gs-31s
   :children [[rc/box
               :width  "36px"
               :height common/gs-31s
               :class  "noselect"
               :style  {:cursor "pointer"}
               :attr   {:title    (str (if open? "Close" "Open") " the pod bay doors, HAL")
                        :on-click #(rf/dispatch [:subs/open-pod? id (not open?)])}
               :child  [rc/box
                        :margin "auto"
                        :child  [:span.arrow (if open? "▼" "▶")]]]
              [rc/box
               :width "64px" ;; (100-36)px from box above
               :child [sub-tag type (short-tag-desc type)]]
              ;; TODO: report if a sub was run multiple times
              #_(when run-times
                [:span "Warning: run " run-times " times"])
              [rc/h-box
               :class    "app-db-path--path-header"
               :size     "auto"
               :children [[rc/input-text
                           :style {:height  "25px"
                                   :padding (css-join "0px" common/gs-7s)
                                   :width   "-webkit-fill-available"} ;; This took a bit of finding!
                           :width     "100%"
                           :model     path
                           :disabled? true]]]
              [rc/gap-f :size common/gs-12s]
              [rc/label :label (str "Layer " layer)]

              ;; TODO: capture previous sub run value and allow diffing it.
              #_[rc/gap-f :size common/gs-12s]
              #_[rc/box
               :class "bm-muted-button app-db-path--button noselect"
               :attr  {:title    "Show diff"
                       :on-click #(when open? (rf/dispatch [:subs/diff-pod? id (not diff?)]))}
               :child [:img
                       {:src   (str "data:image/svg+xml;utf8," copy)
                        :style {:width  "19px"
                                :margin "0px 3px"}}]]
              [rc/gap-f :size common/gs-12s]]])

(defn pod [{:keys [id type layer path open? diff?] :as pod-info}]
  (let [render-diff? (and open? diff?)
        #_#_app-db-after  (rf/subscribe [:app-db/current-epoch-app-db-after])
        #_#_app-db-before (rf/subscribe [:app-db/current-epoch-app-db-before])
        #_#_[diff-before diff-after _] (when render-diff?
                                     (clojure.data/diff (get-in @app-db-before path)
                                                        (get-in @app-db-after path)))]
    [rc/v-box
     :style    {:margin-bottom pod-gap
                :margin-right  "1px"}
     :children [[pod-header pod-info]
                [rc/v-box
                 :class    (when open? "app-db-path--pod-border")
                 :children [[animated/component
                             (animated/v-box-options {:enter-animation "accordionVertical"
                                                      :leave-animation "accordionVertical"
                                                      :duration        animation-duration})
                             (when open?
                               [rc/v-box
                                :class (str "data-viewer" (when-not diff? " rounded-bottom"))
                                :style {:margin     (css-join pod-padding pod-padding "0px" pod-padding)
                                        :overflow-x "auto"
                                        :overflow-y "hidden"}
                                :children [[components/simple-render
                                            (:value pod-info)]]])]
                            [animated/component
                             (animated/v-box-options {:enter-animation "accordionVertical"
                                                      :leave-animation "accordionVertical"
                                                      :duration        animation-duration})
                             (when render-diff?
                               [rc/v-box
                                :children [[rc/v-box
                                            :class    "app-db-path--link"
                                            :justify  :end
                                            :children [[rc/hyperlink-href
                                                        ;:class  "app-db-path--label"
                                                        :label "ONLY BEFORE"
                                                        :style {:margin-left common/gs-7s}
                                                        :attr {:rel "noopener noreferrer"}
                                                        :target "_blank"
                                                        :href utils/diff-link]]]
                                           [rc/v-box
                                            :class    "data-viewer data-viewer--top-rule"
                                            :style    {:overflow-x "auto"
                                                       :overflow-y "hidden"}
                                            :height   "50px"
                                            :children ["---before-diff---"]]
                                           [rc/v-box
                                            :class    "app-db-path--link"
                                            :justify  :end
                                            :children [[rc/hyperlink-href
                                                        ;:class  "app-db-path--label"
                                                        :label "ONLY AFTER"
                                                        :style {:margin-left common/gs-7s}
                                                        :attr {:rel "noopener noreferrer"}
                                                        :target "_blank"
                                                        :href utils/diff-link]]]
                                           [rc/v-box
                                            :class    "data-viewer data-viewer--top-rule rounded-bottom"
                                            :style    {:overflow-x "auto"
                                                       :overflow-y "hidden"}
                                            :height   "50px"
                                            :children ["---after-diff---"]]]])]
                            (when open?
                              [rc/gap-f :size pod-padding])]]]]))

(defn no-pods []
  [rc/h-box
   :margin     (css-join "0px 0px 0px" common/gs-19s)
   :gap        common/gs-7s
   :align      :start
   :align-self :start
   :children   [[rc/label :label "There are no subscriptions to show"]]])

(defn pod-section []
  (let [all-subs       @(rf/subscribe [:subs/visible-subs])
        sub-expansions @(rf/subscribe [:subs/sub-expansions])]
    [rc/v-box
     :size "1"
     ;:gap pod-gap

     ;:children (if (empty? all-subs)
     ;            [[no-pods]]
     ;            (doall (for [p all-subs]
     ;                     ^{:key (:id p)}
     ;                     [pod (merge p (get sub-expansions (:id p)))])))

     :children [(if (and (empty? all-subs) @*finished-animation?)
                  [no-pods]
                  [rc/box :width "0px" :height "0px"])
                [animated/component
                 (animated/v-box-options {:on-finish #(reset! *finished-animation? true)
                                          :duration  animation-duration
                                          :style     {:flex     "1 1 0px"
                                                      :overflow-x "hidden"
                                                      :overflow-y "auto"}})
                 (for [p all-subs]
                   ^{:key (:id p)}
                   [pod (merge p (get sub-expansions (:id p)))])]]


     ]))

(defn render []
  []
  [rc/v-box
   :size     "1"
   :style    {:margin-right common/gs-19s
              ;:overflow     "hidden"
              }
   :children [[panel-header]
              [pod-section]
              [rc/gap-f :size pod-gap]

              ;; TODO: OLD UI - REMOVE
              #_[:div.panel-content-scrollable
               {:style {:border "1px solid lightgrey"
                        :margin "0px"}}
               [:div.subtrees
                {:style {:margin "20px 0"}}
                (doall
                  (->> @subs/query->reaction
                       (sort-by (fn [me] (ffirst (key me))))
                       (map (fn [me]
                              (let [[query-v dyn-v :as inputs] (key me)]
                                ^{:key query-v}
                                [:div.subtree-wrapper {:style {:margin "10px 0"}}
                                 [:div.subtree
                                  [components/subscription-render
                                   (rc/deref-or-value-peek (val me))
                                   [:button.subtree-button {:on-click #(rf/dispatch [:app-db/remove-path (key me)])}
                                    [:span.subtree-button-string
                                     (prn-str (first (key me)))]]
                                   (into [:subs] query-v)]]]))
                            )))
                (do @re-frame.db/app-db
                    nil)]]]])

