(ns day8.re-frame.trace.panels.save
  (:require [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [re-frame.db]
            [day8.re-frame.trace.utils.localstorage :as localstorage]))

(rf/reg-event-db
  :save/save-app-db
  (fn [db _]
    (localstorage/save! "saved-app-db" @re-frame.db/app-db)
    db
    ))

(rf/reg-event-db
  :save/restore-app-db
  (fn [db _]
    (if-let [app-db (localstorage/get "saved-app-db")]
      (reset! re-frame.db/app-db app-db)
      db)))

(defn save-panel []
  [:div
   [:h1 "Save State"]
   [:br]
   [:button.text-button
    {:on-click #(rf/dispatch [:save/save-app-db])}
    "Save"]
   [:br]
   [:button.text-button
    {:on-click #(rf/dispatch [:save/restore-app-db])}
    "Reload"]])
