(ns todomvc.views
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [subscribe dispatch]]
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

(defn todo-app
  []
  [:div
   [:section#10x-dev
    [:a
     {:on-click #(dispatch [:flood])
      :style {:cursor "pointer"}}
     "Flood Events & app-db"]
    " | "
    [:a
     {:on-click #(dispatch [:bad-event])
      :style {:cursor "pointer"}}
     "Dispatch Test Event"]
    [:br]
    [:a
     {:on-click #(re-frame.core/clear-global-interceptor :day8.re-frame-10x/internal)
      :style {:cursor "pointer"}}
     "Break Event"]
    " | "
    [:a
     {:on-click #(do (day8.re-frame-10x/fix-db-noop))
      :style {:cursor "pointer"}}
     "Fix Event"]]
   [:section#todoapp
    [task-entry]
    (when (seq @(subscribe [:todos]))
      [task-list])
    [footer-controls]]
   [:footer#info
    [:p "Double-click to edit a todo"]]])
