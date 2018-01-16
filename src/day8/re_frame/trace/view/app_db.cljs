(ns day8.re-frame.trace.view.app-db
  (:require [devtools.prefs]
            [devtools.formatters.core]
            [day8.re-frame.trace.view.components :as components]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [mranderson047.reagent.v0v6v0.reagent.core :as r]
            [day8.re-frame.trace.utils.re-com :as rc :refer [css-join]]
            [day8.re-frame.trace.common-styles :as common]
            [clojure.data])
  (:require-macros [day8.re-frame.trace.utils.macros :as macros]))

(def delete (macros/slurp-macro "day8/re_frame/trace/images/delete.svg"))
(def reload (macros/slurp-macro "day8/re_frame/trace/images/reload.svg"))
(def reload-disabled (macros/slurp-macro "day8/re_frame/trace/images/reload-disabled.svg"))
(def snapshot (macros/slurp-macro "day8/re_frame/trace/images/snapshot.svg"))
(def snapshot-ready (macros/slurp-macro "day8/re_frame/trace/images/snapshot-ready.svg"))
(def round-arrow (macros/slurp-macro "day8/re_frame/trace/images/round-arrow.svg"))
(def arrow-right (macros/slurp-macro "day8/re_frame/trace/images/arrow-right.svg"))
(def copy (macros/slurp-macro "day8/re_frame/trace/images/copy.svg"))
(def trash (macros/slurp-macro "day8/re_frame/trace/images/trash.svg"))

(def cljs-dev-tools-background "#e8ffe8")
(def pod-gap common/gs-19s)
(def pad-padding common/gs-7s)
(def border-radius "3px")

(def app-db-styles
  [:#--re-frame-trace--
   [:.app-db-path
    {:background-color           common/white-background-color
     :border-bottom-left-radius  border-radius
     :border-bottom-right-radius border-radius}]

   [:.app-db-path--header
    {:background-color        "#48494A"                     ; Name this navbar tint-lighter
     :color                   "white"
     :height                  common/gs-31
     :border-top-left-radius  border-radius
     :border-top-right-radius border-radius}]
   [:.app-db-path--header__closed
    {:border-bottom-left-radius  border-radius
     :border-bottom-right-radius border-radius}]

   [:.app-db-path--button
    {:width         "25px"
     :height        "25px"
     :padding       "0px"
     :border-radius border-radius
     :cursor        "pointer"}]


   [:.app-db-path--label
    {:color           "#2D9CDB"
     ;:font-variant   "small-caps"
     ;:text-transform "lowercase"
     :text-decoration "underline"
     :font-size       "11px"
     :margin-bottom   "2px"
     ;:height         common/gs-19
     }]
   [:.app-db-path--path-header
    {:background-color common/white-background-color
     :color            "#48494A"
     :margin           "3px"}]
   [:.app-db-path--path-text__empty
    {:font-style "italic"}]

   [:.app-db-path--link
    {:margin (css-join "0px" pad-padding)
     :height common/gs-19s}]

   [:.app-db-panel-button
    {:width   "129px"
     :padding "0px"}]

   [:.data-viewer
    {:background-color cljs-dev-tools-background
     :padding          common/gs-7s
     :margin           (css-join "0px" pad-padding)
     :min-width        "100px"
     }]

   ])

;; TODO: START ========== LOCAL DATA - REPLACE WITH SUBS AND EVENTS

(def *pods (r/atom [{:id (gensym) :path "[\"x\" \"y\"]" :open? true :diff? true}
                    {:id (gensym) :path "[:abc 123]" :open? true :diff? false}
                    {:id (gensym) :path "[:a :b :c]" :open? false :diff? true}
                    {:id (gensym) :path nil :open? false :diff? false}
                    {:id (gensym) :path [:boot-state] :open? true :diff? true}]))

(defn add-pod []
  (let [id (gensym)]
    ;(println "Added pod" id)
    (swap! *pods concat [{:id id :path "" :open? true :diff? false}])))

(defn delete-pod [id]
  ;(println "Deleted pod" id)
  (reset! *pods (filterv #(not= id (:id %)) @*pods)))

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

(defn panel-header []
  (let [app-db-after  (rf/subscribe [:app-db/current-epoch-app-db-after])
        app-db-before (rf/subscribe [:app-db/current-epoch-app-db-before])]
    [rc/h-box
     :justify :between
     :align :center
     :margin (css-join common/gs-19s "0px")
     :children [[rc/button
                 :class "app-db-panel-button bm-muted-button"
                 :label [rc/v-box
                         :align :center
                         :children ["+ path inspector"]]
                 :on-click #(rf/dispatch [:app-db/create-path])]
                [rc/h-box
                 :align :center
                 :gap common/gs-7s
                 :height "48px"
                 :padding (css-join "0px" common/gs-12s)
                 :style {:background-color "#fafbfc"
                         :border           "1px solid #e3e9ed"
                         :border-radius    border-radius}
                 :children [[rc/label :label "reset app-db to:"]
                            [rc/button
                             :class "app-db-panel-button bm-muted-button"
                             :label [rc/v-box
                                     :align :center
                                     :children ["initial epoch state"]]
                             :on-click #(rf/dispatch [:snapshot/load-snapshot @app-db-before])]
                            [rc/v-box
                             :width common/gs-81s
                             :align :center
                             :children [[rc/label
                                         :style {:font-size "9px"}
                                         :label "EVENT"]
                                        [:img {:src (str "data:image/svg+xml;utf8," arrow-right)}]
                                        [rc/label
                                         :style {:font-size  "9px"
                                                 :margin-top "-1px"}
                                         :label "PROCESSING"]]]
                            [rc/button
                             :class "app-db-panel-button bm-muted-button"
                             :label [rc/v-box
                                     :align :center
                                     :children ["end epoch state"]]
                             :on-click #(rf/dispatch [:snapshot/load-snapshot @app-db-after])]]]]]))

(defn pod-header [{:keys [id path path-str open? diff?]}]
  [rc/h-box
   :class (str "app-db-path--header " (when-not open? "app-db-path--header__closed"))
   :align :center
   :height common/gs-31s
   :children [[rc/box
               :width "36px"
               :height common/gs-31s
               :class "noselect"
               :style {:cursor "pointer"}
               :attr {:title    (str (if open? "Close" "Open") " the pod bay doors, HAL")
                      :on-click #(rf/dispatch [:app-db/set-path-visibility id (not open?)])}
               :child [rc/box
                       :margin "auto"
                       :child [:span.arrow (if open? "▼" "▶")]]]
              [rc/h-box
               :class "app-db-path--path-header"
               :size "auto"
               :children [[rc/input-text
                           :attr {:on-blur (fn [e] (rf/dispatch [:app-db/update-path-blur id]))}
                           :style {:height  "25px"
                                   :padding (css-join "0px" common/gs-7s)
                                   :width   "-webkit-fill-available"} ;; This took a bit of finding!
                           :width "100%"
                           :model path-str
                           :on-change #(rf/dispatch [:app-db/update-path id %]) ;;(fn [input-string] (rf/dispatch [:app-db/search-string input-string]))
                           :on-submit #()                   ;; #(rf/dispatch [:app-db/add-path %])
                           :change-on-blur? false
                           :placeholder "Showing all of app-db. Try entering a path like [:todos 1]"]]]
              [rc/gap-f :size common/gs-12s]
              [rc/box
               :class "app-db-path--button bm-muted-button noselect"
               :attr {:title    "Show diff"
                      :on-click #(rf/dispatch [:app-db/set-diff-visibility id (not diff?)])}
               :child [:img
                       {:src   (str "data:image/svg+xml;utf8," copy)
                        :style {:width  "19px"
                                :margin "0px 3px"}}]]
              [rc/gap-f :size common/gs-12s]
              [rc/box
               :class "app-db-path--button bm-muted-button noselect"
               :attr {:title    "Remove this pod"
                      :on-click #(rf/dispatch [:app-db/remove-path id])}
               :child [:img
                       {:src   (str "data:image/svg+xml;utf8," trash)
                        :style {:width  "13px"
                                :margin "0px 6px"}}]]
              [rc/gap-f :size common/gs-12s]]])

(defn pod [{:keys [id path open? diff?] :as pod-info}]
  (let [render-diff?  (and open? diff?)
        app-db-after  (rf/subscribe [:app-db/current-epoch-app-db-after])
        app-db-before (rf/subscribe [:app-db/current-epoch-app-db-before])
        [diff-before diff-after _] (when render-diff?
                                     (clojure.data/diff (get-in @app-db-before path)
                                                        (get-in @app-db-after path)))]
    [rc/v-box
     :class "app-db-path"
     :children [[pod-header pod-info]
                (when open?
                  [rc/v-box
                   :class "data-viewer"
                   :style {:margin (css-join pad-padding pad-padding "0px" pad-padding)}
                   :children [[components/simple-render
                               (get-in @app-db-after path)

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
                  (list
                    ^{:key "only-before"}
                    [rc/v-box
                     :class "app-db-path--link"
                     :justify :end
                     :children [[rc/hyperlink-href
                                 ;:class "app-db-path--label"
                                 :label "ONLY BEFORE"
                                 :href "https://github.com/Day8/re-frame-trace/wiki/app-db#diff"]]]

                    ^{:key "only-before-diff"}
                    [rc/v-box
                     :class "data-viewer"
                     :children [[components/simple-render
                                 diff-before]]]

                    ^{:key "only-after"}
                    [rc/v-box
                     :class "app-db-path--link"
                     :justify :end
                     :children [[rc/hyperlink-href
                                 ;:class "app-db-path--label"
                                 :label "ONLY AFTER"
                                 :href "https://github.com/Day8/re-frame-trace/wiki/app-db#diff"]]]

                    ^{:key "only-after-diff"}
                    [rc/v-box
                     :class "data-viewer"
                     :children [[components/simple-render
                                 diff-after]]]))
                (when open?
                  [rc/gap-f :size pad-padding])]]))

(defn no-pods []
  [rc/h-box
   :margin (css-join "0px 0px 0px" common/gs-19s)
   :gap common/gs-7s
   :align :start
   :align-self :start
   :children [[:img {:src (str "data:image/svg+xml;utf8," round-arrow)}]
              [rc/label
               :style {:width      "150px"
                       :margin-top "22px"}
               :label "add inspectors to show what happened to app-db"]]])

(defn pod-section []
  (let [pods @(rf/subscribe [:app-db/paths])]
    [rc/v-box
     :gap pod-gap
     :children (if (empty? pods)
                 [[no-pods]]
                 (doall (for [p pods]
                          ^{:key (:id pods)}
                          [pod p])))]))

;; TODO: OLD UI - REMOVE
(defn original-render [app-db]
  (let [subtree-input   (r/atom "")
        subtree-paths   (rf/subscribe [:app-db/paths])
        search-string   (rf/subscribe [:app-db/search-string])
        input-error     (r/atom false)
        snapshot-ready? (rf/subscribe [:snapshot/snapshot-ready?])]
    (fn []
      [:div
       {:style {:flex           "1 1 auto"
                :display        "flex"
                :flex-direction "column"
                :border         "1px solid lightgrey"}}
       [:div.panel-content-scrollable
        [rc/input-text
         :model search-string
         :on-change (fn [input-string] (rf/dispatch [:app-db/search-string input-string]))
         :on-submit #(rf/dispatch [:app-db/add-path %])
         :change-on-blur? false
         :placeholder ":path :into :app-db"]
        ;; TODO: check for input errors
        ; (if @input-error
        ;   [:div.input-error {:style {:color "red" :margin-top 5}}
        ;    "Please enter a valid path."])]]



        [:div.subtrees {:style {:margin "20px 0"}}
         (doall
           (map (fn [path]
                  ^{:key path}
                  [:div.subtree-wrapper {:style {:margin "10px 0"}}
                   [:div.subtree
                    [components/subtree
                     (get-in @app-db path)
                     [rc/h-box
                      :align :center
                      :children [[:button.subtree-button
                                  [:span.subtree-button-string
                                   (str path)]]
                                 [:img
                                  {:src      (str "data:image/svg+xml;utf8," delete)
                                   :style    {:cursor "pointer"
                                              :height "10px"}
                                   :on-click #(rf/dispatch [:app-db/remove-path path])}]]]
                     [path]]]])
                @subtree-paths))]
        [:div {:style {:margin-bottom "20px"}}
         [components/subtree @app-db [:span.label "app-db"] [:app-db]]]]])))

(defn render [app-db]
  [rc/v-box
   :style {:margin-right common/gs-19s}
   :children [[panel-header]
              [pod-section]
              [rc/gap-f :size pod-gap]]])
