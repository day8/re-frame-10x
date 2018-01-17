(ns day8.re-frame.trace.view.subs
  (:require [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [mranderson047.reagent.v0v6v0.reagent.core :as r]
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

(def cljs-dev-tools-background "#e8ffe8")
(def pod-gap common/gs-19s)
(def pad-padding common/gs-7s)

;; TODO: START ========== LOCAL DATA - REPLACE WITH SUBS AND EVENTS

(def *pods (r/atom [{:id (gensym) :type :destroyed :layer "3" :path "[:todo/blah]"      :open? true :diff? false}
                    {:id (gensym) :type :created   :layer "3" :path "[:todo/completed]" :open? true :diff? true}
                    {:id (gensym) :type :re-run    :layer "3" :path "[:todo/completed]" :open? true :diff? false}
                    {:id (gensym) :type :re-run    :layer "2" :path "[:todo/blah]"      :open? true :diff? false}
                    {:id (gensym) :type :not-run   :layer "3" :path "[:todo/blah]"      :open? true :diff? false}]))

(defn update-pod-field
  [id field new-val]
  (let [f (fn [pod]
            (if (= id (:id pod))
              (do
                ;(println "Updated" field "in" (:id pod) "from" (get pod field) "to" new-val)
                (assoc pod field new-val))
              pod))]
    (reset! *pods (mapv f @*pods))))

;; TODO: END ========== LOCAL DATA - REPLACE WITH SUBS AND EVENTS

(defn tag-color [type]
  (let [types {:created   "#9b51e0"
               :destroyed "#f2994a"
               :re-run    "#219653"
               :not-run   "#bdbdbd"}]
    (get types type "black")))

(def tag-types {:created   {:long "CREATED" :short "CREATED"}
                :destroyed {:long "DESTROYED" :short "DESTROY"}
                :re-run    {:long "RE-RUN" :short "RE-RUN"}
                :not-run   {:long "NOT-RUN" :short "NOT-RUN"}})

(defn long-tag-desc [type]
  (get-in tag-types [type :long] "???"))

(defn short-tag-desc [type]
  (get-in tag-types [type :short] "???"))

(defn tag [type label]
  [rc/box
   :class "noselect"
   :style {:color            "white"
           :background-color (tag-color type)
           :width            "48px" ;common/gs-50s
           :height           "17px" ;common/gs-19s
           :font-size        "10px"
           :font-weight      "bold"
           :border           "1px solid #bdbdbd"
           :border-radius    "3px"}
   :child  [:span {:style {:margin "auto"}} label]])

(defn title-tag [type title label]
  [rc/v-box
   :class    "noselect"
   :align    :center
   :gap      "2px"
   :children [[:span {:style {:font-size "9px"}} title]
              [tag type label]]])

(defn panel-header []
  (let [created-count (rf/subscribe [:subs/created-count])
        re-run-count (rf/subscribe [:subs/re-run-count])
        destroyed-count (rf/subscribe [:subs/destroyed-count])
        not-run-count (rf/subscribe [:subs/not-run-count])
        ignore-unchanged? (rf/subscribe [:subs/ignore-unchanged-subs?])
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
                         :border-radius    "3px"}
                 :children [[:span {:style {:color       "#828282"
                                            :font-size   "18px"
                                            :font-weight "lighter"}}
                             "Summary:"]
                            [title-tag :created (long-tag-desc :created) @created-count]
                            [title-tag :re-run (long-tag-desc :re-run) @re-run-count]
                            [title-tag :destroyed (long-tag-desc :destroyed) @destroyed-count]
                            [title-tag :not-run (long-tag-desc :not-run) @not-run-count]]]
                [rc/h-box
                 :align :center
                 :gap common/gs-19s
                 :height "48px"
                 :padding (css-join "0px" common/gs-19s)
                 :style {:background-color "#fafbfc"
                         :border           "1px solid #e3e9ed"
                         :border-radius    "3px"}
                 :children [[rc/checkbox
                             :model ignore-unchanged?
                             :label [:span "Ignore " [:b {:style {:font-weight "700"}} @ignore-unchanged-l2-count] " unchanged" [:br] "layer 2 subs"]
                             :style {:margin-top "6px"}
                             :on-change #(rf/dispatch [:subs/ignore-unchanged-subs? %])]]]]]))

(defn pod-header [{:keys [id type layer path open? diff?]}]
  [rc/h-box
   :class    "app-db-path--header"
   :style    (merge {:border-top-left-radius  "3px"
                     :border-top-right-radius "3px"}
                    (when-not open?
                      {:border-bottom-left-radius  "3px"
                       :border-bottom-right-radius "3px"}))
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
               :child [tag type (short-tag-desc type)]]
              [rc/h-box
               :size "auto"
               :class "app-db-path--path-header"
               :children [[rc/input-text
                           :style           {:height  "25px"
                                             :padding (css-join "0px" common/gs-7s)
                                             :width   "-webkit-fill-available"} ;; This took a bit of finding!
                           :width           "100%"
                           :model           path
                           :disabled?       true
                           :on-change       #(update-pod-field id :path %)  ;;(fn [input-string] (rf/dispatch [:app-db/search-string input-string]))
                           :on-submit       #()                             ;; #(rf/dispatch [:app-db/add-path %])
                           :change-on-blur? false
                           :placeholder     "Showing all of app-db. Try entering a path like [:todos 1]"]]]
              [rc/gap-f :size common/gs-12s]
              [rc/label :label (str "Layer " layer)]
              [rc/gap-f :size common/gs-12s]
              [rc/box
               :class "bm-muted-button noselect"
               :style {:width         "25px"
                       :height        "25px"
                       :padding       "0px"
                       :border-radius "3px"
                       :cursor        "pointer"}
               :attr  {:title    "Show diff"
                       :on-click #(rf/dispatch [:subs/diff-pod? id (not diff?)])}
               :child [:img
                       {:src   (str "data:image/svg+xml;utf8," copy)
                        :style {:width  "19px"
                                :margin "0px 3px"}}]]
              [rc/gap-f :size common/gs-12s]]])

(defn pod [{:keys [id type layer path open? diff?] :as pod-info}]
  [rc/v-box
   :class "app-db-path"
   :style {:border-bottom-left-radius "3px"
           :border-bottom-right-radius "3px"}
   :children [[pod-header pod-info]
              (when open?
                [rc/v-box
                 :min-width "100px"
                 :style {:background-color cljs-dev-tools-background
                         :padding          common/gs-7s
                         :margin           (css-join pad-padding pad-padding "0px" pad-padding)}
                 :children [[components/simple-render
                             (:value pod-info)]]])
              (when (and open? diff?)
                [rc/v-box
                 :height common/gs-19s
                 :justify :end
                 :style {:margin (css-join "0px" pad-padding)}
                 :children [[rc/hyperlink
                             ;:class "app-db-path--label"
                             :label "ONLY BEFORE"
                             :on-click #(println "Clicked [ONLY BEFORE]")]]])
              (when (and open? diff?)
                [rc/v-box
                 :height "60px"
                 :min-width "100px"
                 :style {:background-color cljs-dev-tools-background
                         :padding          common/gs-7s
                         :margin           (css-join "0px" pad-padding)}
                 :children ["---before-diff---"]])
              (when (and open? diff?)
                [rc/v-box
                 :height common/gs-19s
                 :justify :end
                 :style {:margin (css-join "0px" pad-padding)}
                 :children [[rc/hyperlink
                             ;:class "app-db-path--label"
                             :label "ONLY AFTER"
                             :on-click #(println "Clicked [ONLY AFTER]")]]])
              (when (and open? diff?)
                [rc/v-box
                 :height "60px"
                 :min-width "100px"
                 :style {:background-color cljs-dev-tools-background
                         :padding          common/gs-7s
                         :margin           (css-join "0px" pad-padding)}
                 :children ["---after-diff---"]])
              (when open?
                [rc/gap-f :size pad-padding])]])

(defn no-pods []
  [rc/h-box
   :margin (css-join "0px 0px 0px" common/gs-19s)
   :gap common/gs-7s
   :align :start
   :align-self :start
   :children [[rc/label :label "There are no subscriptions to show"]]])

(defn pod-section []
  (let [all-subs       @(rf/subscribe [:subs/visible-subs])
        sub-expansions @(rf/subscribe [:subs/sub-expansions])]
    (js/console.log sub-expansions)
    [rc/v-box
     :gap pod-gap
     :children (if (empty? all-subs)
                 [[no-pods]]
                 (doall (for [p all-subs]
                          ^{:key (:id p)}
                          [pod (merge p (get sub-expansions (:id p)))])))]))

(defn render []
  []
  [rc/v-box
   :style    {:margin-right common/gs-19s
              :overflow     "hidden"}
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

