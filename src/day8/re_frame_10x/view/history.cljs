(ns day8.re-frame-10x.view.history
  (:require [day8.re-frame-10x.utils.re-com :as rc]
            [day8.re-frame-10x.inlined-deps.re-frame.v0v10v9.re-frame.core :as rf]
            [day8.re-frame-10x.common-styles :as common]
            [day8.re-frame-10x.utils.pretty-print-condensed :as pp]))

(def history-styles
  [:#--re-frame-10x--
   [:.history-list
    {:background-color common/history-background-color
     :overflow-y       "scroll"
     :overflow-x       "hidden"
     :resize           "vertical"}
    [:.history-item
     {:color            common/history-item-text-color
      :background-color common/history-item-background-color
      :margin           "2px"
      :padding          "5px"
      :font-weight      "600"
      :cursor           "pointer"
      :text-overflow    "ellipsis"
      :white-space      "nowrap"
      :overflow         "hidden"
      :flex-shrink      0}
     [:&:hover
      {:color common/history-item-hover-color}]]
    [:.history-item.active
     {:color  common/history-item-active-color
      :cursor "default"}]
    [:.history-item.inactive
     {:color common/history-item-inactive-color}
     [:&:hover
      {:color common/history-item-hover-color}]]]])

(defn history-item [event id current-id]
  (let [event-str (pp/truncate 400 :end event)
        active?   (= id current-id)
        inactive? (> id current-id)]
    [:span
     (merge
       {:class (str "history-item"
                    (when active?   " active")
                    (when inactive? " inactive"))}
       (when-not active?
         {:on-click #(rf/dispatch [:epochs/load-epoch id])
          :title    "Jump to this epoch"}))
     event-str]))

(defn render []
  (let [all-events @(rf/subscribe [:epochs/all-events-by-id])
        current-id @(rf/subscribe [:epochs/current-epoch-id])]
    [rc/v-box
     :class "history-list"
     :height "20%"
     :children [(for [[id event] all-events
                      :when (not-empty event)]
                     ^{:key id}
                     [history-item event id current-id])]]))

