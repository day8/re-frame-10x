(ns day8.re-frame-10x.navigation.events
  (:require
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.panels.settings.events                     :as settings.events]
    [day8.re-frame-10x.fx.window                                  :as window]
    [day8.re-frame-10x.fx.local-storage                           :as local-storage]
    [day8.re-frame-10x.tools.coll                                 :as tools.coll]))

(rf/reg-event-fx
  ::launch-external
  [rf/trim-v]
  (fn [{:keys [db]} [view-fn]]
    {:fx [[::window/open-debugger-window
           (merge (get-in db [:settings :external-window-dimensions])
                  {:on-load    #(view-fn %1 %2)
                   :on-success [::launch-external-success]
                   :on-failure [::launch-external-failure]})]]}))

(rf/reg-event-fx
  ::launch-external-success
  [(local-storage/save "external-window?" [:settings :external-window?])]
  (fn [{:keys [db]} _]
    {:db (-> db
             (assoc-in [:settings :external-window?] true)
             (tools.coll/dissoc-in [:errors :popup-failed?]))
     :fx [[:dispatch-later {:ms 200 :dispatch [::settings.events/show-panel? false]}]]}))

(rf/reg-event-fx
  ::launch-external-failure
  (fn [{:keys [db]} _]
    {:db (assoc-in db [:errors :popup-failed?] true)
     :fx [[:dispatch [::external-closed]]]}))

(rf/reg-event-fx
  ::external-closed
  [(rf/path [:settings :external-window?]) (local-storage/save "external-window?")]
  (fn [_ _]
    {:db false
     :fx [[:dispatch-later {:ms 400 :dispatch [::settings.events/show-panel? true]}]]}))

(rf/reg-event-db
  ::dismiss-popup-failed
  [(rf/path [:errors])]
  (fn [errors _]
    (dissoc errors :popup-failed?)))