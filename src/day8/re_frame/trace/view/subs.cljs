(ns day8.re-frame.trace.view.subs
  (:require [re-frame.subs :as subs]
            ;[cljs.spec.alpha :as s]
            [day8.re-frame.trace.view.components :as components]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [mranderson047.reagent.v0v6v0.reagent.core :as r]
            [day8.re-frame.trace.utils.re-com :as rc :refer [css-join]]
            [day8.re-frame.trace.common-styles :as common])
  (:require-macros [day8.re-frame.trace.utils.macros :as macros]))

;(s/def ::query-v any?)
;(s/def ::dyn-v any?)
;(s/def ::query-cache-params (s/tuple ::query-v ::dyn-v))
;(s/def ::deref #(satisfies? IDeref %))
;(s/def ::query-cache (s/map-of ::query-cache-params ::deref))
;(assert (s/valid? ::query-cache (rc/deref-or-value-peek subs/query->reaction)))

(def copy (macros/slurp-macro "day8/re_frame/trace/images/copy.svg"))

(def cljs-dev-tools-background "#e8ffe8")
(def pod-gap common/gs-19s) ;; or 31?
(def pad-padding common/gs-7s)

(defn tag-color [type]
  (let [types {:created   "#9b51e0"
               :destroyed "#f2994a"
               :re-run    "#219653"
               :not-run   "#bdbdbd"}]
    (get types type "black")))

(defn tag-desc [type]
  (let [types {:created   {:long "CREATED"   :short "CREATED"}
               :destroyed {:long "DESTROYED" :short "DESTROY"}
               :re-run    {:long "RE-RUN"    :short "RE-RUN" }
               :not-run   {:long "NOT-RUN"   :short "NOT-RUN"}}]
    (get types type "???")))

(defn tag [type label]
  [rc/box
   :style {:color            "white"
           :background-color (tag-color type)
           :width            common/gs-50s
           :height           common/gs-19s
           :font-size        "10px"
           :font-weight      "bold"
           :border-radius    "3px"}
   :child  [:span {:style {:margin "auto"}} label]])

(defn title-tag [type title label]
  [rc/v-box
   :align    :center
   :gap      "2px"
   :children [[:span {:style {:font-size "9px"}} title]
              [tag type label]]])

(defn panel-header []
  [rc/h-box
   :justify  :between
   :align    :center
   :margin   (css-join common/gs-19s "0px")
   :children [[rc/h-box
               :align    :center
               :gap      common/gs-19s
               :height   "48px"
               :padding  (css-join "0px" common/gs-19s)
               :style    {:background-color "#fafbfc"
                          :border "1px solid #e3e9ed"
                          :border-radius "3px"}
               :children [[:span {:style {:color       "#828282"
                                          :font-size   "18px"
                                          :font-weight "lighter"}}
                           "Summary:"]
                          [title-tag :created   (-> :created   tag-desc :long) 2]
                          [title-tag :re-run    (-> :re-run    tag-desc :long) 44]
                          [title-tag :destroyed (-> :destroyed tag-desc :long) 1]
                          [title-tag :not-run   (-> :not-run   tag-desc :long) 12]]]
              [rc/h-box
               :align    :center
               :gap      common/gs-19s
               :height   "48px"
               :padding  (css-join "0px" common/gs-19s)
               :style    {:background-color "#fafbfc"
                          :border "1px solid #e3e9ed"
                          :border-radius "3px"}
               :children [[rc/checkbox
                           :model     true
                           :label     [:span "Ignore unchanged" [:br] "layer 2 subs"]
                           :style     {:margin-top "6px"}
                           :on-change #()]]]]])

(defn path-header [{:keys [type layer path]}]
  (let [search-string (r/atom (when (some? path) (prn-str path))) ;;(rf/subscribe [:app-db/search-string])
        *pod-open     (r/atom true)]
    (fn [{:keys [type layer path]}]
      [rc/h-box
       :class "app-db-path--header"
       :style {:border-top-left-radius  "3px"
               :border-top-right-radius "3px"}
       :align :center
       :height common/gs-31s
       :children [[rc/box
                   :width  "36px"
                   :height common/gs-31s
                   :class  "noselect"
                   :style  {:cursor "pointer"}
                   :attr   {:title    (str (if @*pod-open "Close" "Open") " the pod bay doors, HAL")
                            :on-click (rc/handler-fn
                                        (swap! *pod-open not)
                                        (println "Clicked [arrow]"))}
                   :child  [rc/box
                            :margin "auto"
                            :child  [:span.arrow (if @*pod-open "▼" "▶")]]]
                  [rc/box
                   :width "64px" ;; (100-36)px from box above
                   :child [tag type (-> type tag-desc :short)]]
                  [rc/h-box
                   :size "auto"
                   :class "app-db-path--path-header"
                   :children [[rc/input-text
                               ;:class           (str "app-db-path--path-text " (when (nil? path) "app-db-path--path-text__empty"))
                               :style           {:height  "25px"
                                                 :padding (css-join "0px" common/gs-7s)
                                                 :width   "-webkit-fill-available"} ;; This took a bit of finding!
                               :width           "100%"
                               :model           search-string
                               :disabled?       true
                               :on-change       (fn [input-string] (rf/dispatch [:app-db/search-string input-string]))
                               :on-submit       #(rf/dispatch [:app-db/add-path %])
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
                           :on-click (rc/handler-fn (println "Clicked [copy]"))}
                   :child [:img
                           {:src   (str "data:image/svg+xml;utf8," copy)
                            :style {:width  "19px"
                                    :margin "0px 3px"}}]]
                  [rc/gap-f :size common/gs-12s]]])))

(defn pod [pod-info]
  [rc/v-box
   :class "app-db-path"
   :style {:border-bottom-left-radius "3px"
           :border-bottom-right-radius "3px"}
   :children [[path-header pod-info]
              [rc/v-box
               :height    "90px"
               :min-width "100px"
               :style     {:background-color cljs-dev-tools-background
                           :padding  common/gs-7s
                           :margin (css-join pad-padding pad-padding "0px" pad-padding)}
               :children  ["---main-section---"]]

              [rc/v-box
               :height  common/gs-19s
               :justify  :end
               :style    {:margin (css-join "0px" pad-padding)}
               :children [[rc/hyperlink
                           ;:class "app-db-path--label"
                           :label "ONLY BEFORE"
                           :on-click #(println "Clicked [ONLY BEFORE]")]]]
              [rc/v-box
               :height    "60px"
               :min-width "100px"
               :style     {:background-color cljs-dev-tools-background
                           :padding  common/gs-7s
                           :margin (css-join "0px" pad-padding)}
               :children  ["---before-diff---"]]

              [rc/v-box
               :height   common/gs-19s
               :justify  :end
               :style    {:margin (css-join "0px" pad-padding)}
               :children [[rc/hyperlink
                           ;:class "app-db-path--label"
                           :label "ONLY AFTER"
                           :on-click #(println "Clicked [ONLY AFTER]")]]]
              [rc/v-box
               :height    "60px"
               :min-width "100px"
               :style     {:background-color cljs-dev-tools-background
                           :padding  common/gs-7s
                           :margin (css-join "0px" pad-padding)}
               :children  ["---after-diff---"]]
              [rc/gap-f :size pad-padding]]])

(defn no-pods []
  [rc/h-box
   :margin (css-join "0px 0px 0px" common/gs-19s)
   :gap common/gs-7s
   :align :start
   :align-self :start
   :children [[rc/label :label "There are no subscriptions to show"]]])

(defn pod-section []
  (let [
        pods [{:type :destroyed :layer "3" :path [:todo/blah]}
              {:type :created   :layer "3" :path [:todo/completed]}
              {:type :re-run    :layer "3" :path [:todo/completed]}
              {:type :re-run    :layer "2" :path [:todo/blah]}
              {:type :not-run   :layer "3" :path [:todo/blah]}]
        ;pods nil
        ]
    (fn []
      [rc/v-box
       :gap pod-gap
       :children (if (empty? pods)
                   [[no-pods]]
                   (doall (for [p pods]
                            ^{:key (str p)}
                            [pod p])))])))

(defn render []
  []
  [rc/v-box
   :style    {:margin-right common/gs-19s}
   :children [[panel-header]
              [pod-section]
              [rc/gap-f :size pod-gap]

              ;; TODO: OLD UI - REMOVE
              [:div.panel-content-scrollable {:style {:border "1px solid lightgrey"}}
               [:div.subtrees {:style {:margin "20px 0"}}
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

