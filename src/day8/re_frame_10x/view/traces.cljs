(ns day8.re-frame-10x.view.traces
  (:require
    [clojure.string :as string]
    [day8.re-frame-10x.components :as components]
    [day8.re-frame-10x.utils.pretty-print-condensed :as pp]
    [day8.re-frame-10x.inlined-deps.reagent.v1v0v0.reagent.core :as r]
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.utils.re-com :as rc]
    [day8.re-frame-10x.styles :as styles]
    [day8.re-frame-10x.svgs :as svgs]
    [day8.re-frame-10x.material :as material]
    [day8.re-frame-10x.view.cljs-devtools :as cljs-devtools]
    [day8.re-frame-10x.traces.subs :as subs]
    [day8.re-frame-10x.traces.events :as events]
    [day8.re-frame-10x.epochs.subs :as epochs.subs]
    [day8.re-frame-10x.epochs.events :as epochs.events]
    [day8.re-frame-10x.settings.subs :as settings.subs]))



(defn add-filter [filter-items filter-input filter-type]
  (rf/dispatch [::events/add-query filter-input filter-type]))



(defn manual-filter
  []
  (let [ambiance                (rf/subscribe [::settings.subs/ambiance])
        filter-type             (r/atom :contains)
        filter-input            (r/atom "")
        input-error             (r/atom false)
        filter-items (rf/subscribe [::subs/queries])
        save-query         (fn [_]
                             (if (and (= @filter-type :slower-than)
                                      (js/isNaN (js/parseFloat @filter-input)))
                               (reset! input-error true)
                               (do
                                 (reset! input-error false)
                                 (add-filter filter-items @filter-input @filter-type))))]
    (fn []
      [rc/v-box
       :children
       [[rc/h-box
         :class    (styles/trace-filter-fields @ambiance)
         :children
         [[:select  {:value     @filter-type
                     :on-change #(reset! filter-type (keyword (.. % -target -value)))}
           [:option {:value "contains"} "contains"]
           [:option {:value "slower-than"} "slower than"]]
          [:div.search
           [components/search {:on-save           save-query
                               :on-change   #(reset! filter-input (.. % -target -value))
                               :placeholder "Type to filter traces"}]
           (if @input-error
             [:div.input-error {:style {:color "red" :margin-top 5}}
              "Please enter a valid number."])]]]
        [:ul.filter-items
         (map (fn [item]
                ^{:key (:id item)}
                [:li.filter-item
                 [:button.button
                  {:style    {:margin 0}
                   :on-click #(rf/dispatch [::events/remove-query (:id item)])}
                  (:filter-type item) ": " [:span.filter-item-string (:query item)]]])
              @filter-items)]]])))

(defn filters
  []
  [rc/v-box
   :children
   [[manual-filter]]])

(defn render-traces [visible-traces filter-items filter-input trace-detail-expansions]
  (let [debug? @(rf/subscribe [::settings.subs/debug?])]
    (doall
      (->>
        visible-traces
        (map-indexed (fn [index {:keys [op-type id operation tags duration] :as trace}]
                       (let [ambiance  (rf/subscribe [::settings.subs/ambiance])
                             show-row? (get-in @trace-detail-expansions [:overrides id]
                                               (:show-all? @trace-detail-expansions))
                             op-name   (if (vector? operation)
                                         (second operation)
                                         operation)]
                         (list [:tr {:key      id
                                     :on-click #(rf/dispatch [::events/toggle-expansion id])
                                     :class    (styles/trace-item @ambiance op-type)}
                                [:td
                                 [components/expansion-button
                                  {:open? show-row?}]]
                                [:td.trace--op
                                 [:span.op-string {}
                                  (str op-type)]]
                                [:td.trace--op-string
                                 [:span.op-string
                                  (pp/truncate 20 :middle (pp/str->namespaced-sym op-name)) " "
                                  [:span
                                   {:style {:opacity 0.5
                                            :display "inline-block"}}
                                   (when-let [[_ & params] (or (get tags :query-v)
                                                               (get tags :event))]
                                     (->> (map pp/pretty-condensed params)
                                          (string/join ", ")
                                          (pp/truncate-string :middle 40)))]]]
                                (if debug?
                                  [:td.trace--meta
                                   (:reaction (:tags trace)) "/" id]
                                  [:td.trace--meta

                                   (.toFixed duration 1) " ms"])]
                               (when show-row?
                                 [:tr.trace--details {:key       (str id "-details")
                                                      :tab-index 0}
                                  [:td]
                                  [:td
                                   {:col-span 2
                                    :on-click #(.log js/console trace)}
                                   [cljs-devtools/simple-render tags []]]
                                  #_[:td
                                     [components/icon-button
                                      {:icon [material/content-copy]
                                       :on-click #(js/console.log tags)}]]])))))))))

(defn traces-table
  []
  (let [ambiance                (rf/subscribe [::settings.subs/ambiance])
        filter-input            (r/atom "")
        filter-items            (rf/subscribe [::subs/queries])
        visible-traces          @(rf/subscribe [::subs/sorted])
        trace-detail-expansions (rf/subscribe [::subs/expansions])]
    [rc/box
     :size  "1"
     :class (styles/trace-table @ambiance)
     :child [:table
             [:thead>tr
              [:th {:style {:padding 0}}
               [:button.text-button
                {:style    {:cursor "pointer"}
                 :on-click #(rf/dispatch [::events/toggle-expansions])}
                (if (:show-all? @trace-detail-expansions) "-" "+")]]
              [:th "operations"]
              [:th
               (str (count visible-traces) " traces")
               [:span "(" [:button.text-button {:on-click #(rf/dispatch [::epochs.events/reset])} "clear"] ")"]]
              [:th {:style {:text-align "right"}} "meta"]]
             [:tbody (render-traces visible-traces filter-items filter-input trace-detail-expansions)]]]))

(defn render []
  (let [ambiance                (rf/subscribe [::settings.subs/ambiance])
        beginning               (rf/subscribe [::epochs.subs/beginning-trace-id])
        end                     (rf/subscribe [::epochs.subs/ending-trace-id])]
    [rc/v-box
     :size     "1"
     :children
     [[filters]
      [traces-table]]]))
