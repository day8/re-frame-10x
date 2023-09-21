(ns todomvc.views
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [subscribe dispatch reg-event-db reg-sub]]
            [clojure.string :as str]))

(defn todo-input [{:keys [title on-save on-stop]}]
  (let [val  (reagent/atom title)
        stop #(do (reset! val "")
                  (when on-stop (on-stop)))
        save #(let [v (-> @val str str/trim)]
                (on-save v)
                (stop))]
    (fn [props]
      [:input (merge (dissoc props :on-save :on-stop :title)
                     {:type        "text"
                      :value       @val
                      :auto-focus  true
                      :on-blur     save
                      :on-change   #(reset! val (-> % .-target .-value))
                      :on-key-down #(case (.-which %)
                                      13 (save)
                                      27 (stop)
                                      nil)})])))

(defn todo-item
  []
  (let [editing (reagent/atom false)]
    (fn [{:keys [id done title]}]
      [:li {:class (str (when done "completed ")
                        (when @editing "editing"))}
       [:div.view
        [:input.toggle
         {:type      "checkbox"
          :checked   done
          :on-change #(dispatch [:toggle-done id])}]
        [:label
         {:on-double-click #(reset! editing true)}
         title]
        [:button.destroy
         {:on-click #(dispatch [:delete-todo id])}]]
       (when @editing
         [todo-input
          {:class   "edit"
           :title   title
           :on-save #(if (seq %)
                       (dispatch [:save id %])
                       (dispatch [:delete-todo id]))
           :on-stop #(reset! editing false)}])])))

(defn task-list
  []
  (let [visible-todos @(subscribe [:visible-todos])
        all-complete? @(subscribe [:all-complete?])]
    [:section#main
     [:input#toggle-all
      {:type      "checkbox"
       :checked   all-complete?
       :on-change #(dispatch [:complete-all-toggle])}]
     [:label
      {:for "toggle-all"}
      "Mark all as complete"]
     [:ul#todo-list
      (for [todo visible-todos]
        ^{:key (:id todo)} [todo-item todo])]]))

(defn footer-controls
  []
  (let [[active done] @(subscribe [:footer-counts])
        showing @(subscribe [:showing])
        a-fn    (fn [filter-kw txt]
                  [:a {:class (when (= filter-kw showing) "selected")
                       :href  (str "#/" (name filter-kw))} txt])]
    [:footer#footer
     [:span#todo-count
      [:strong active] " " (case active 1 "item" "items") " left"]
     [:ul#filters
      [:li (a-fn :all "All")]
      [:li (a-fn :active "Active")]
      [:li (a-fn :done "Completed")]]
     (when (pos? done)
       [:button#clear-completed {:on-click #(dispatch [:clear-completed])}
        "Clear completed"])]))

(defn task-entry
  []
  [:header#header
   [:h1 "todos"]
   [todo-input
    {:id          "new-todo"
     :placeholder "What needs to be done?"
     :on-save     #(when (seq %)
                     (dispatch [:add-todo %]))}]])

(reg-event-db :do-nothing (fn [db _] db))
(def should-i-subscribe? (reagent/atom false))
(def not-buggy-run-ct (atom 0))
(reg-sub :NOT-BUGGY #(swap! not-buggy-run-ct inc))

(defn not-buggy-component []
  [:div
   [:button {:on-click #(do (dispatch [:do-nothing]) ;; for tracing purposes
                            (swap! should-i-subscribe? not))}
    "Click Me, I'm normal!"]
   [:div
    (when @should-i-subscribe?
      @(subscribe [:NOT-BUGGY]))]])

(reg-event-db :swap-sub #(update %1 :should-i-subscribe? not))
(reg-sub :should-i-subscribe? :-> :should-i-subscribe?)
(def buggy-run-ct (atom 0))
(reg-sub :BUGGY #(swap! buggy-run-ct inc))

(defn buggy-component []
  [:div
   [:button {:on-click #(dispatch [:swap-sub])}
    "Click Me, I'm buggy!"]
   [:div
   (when @(subscribe [:should-i-subscribe?])
     @(subscribe [:BUGGY]))]])

(defn todo-app
  []
  [:div {:style {:display "flex"}}
   [not-buggy-component]
   [:div {:style {:width 50}}]
   [buggy-component]])
