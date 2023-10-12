(ns day8.re-frame-10x.panels.app-db.subs
  (:require
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core :as rf]
   [day8.re-frame-10x.navigation.epochs.subs                     :as epochs]))

(rf/reg-sub
 ::root
 (fn [{:keys [app-db]} _]
   app-db))

(rf/reg-sub
 ::current-epoch-app-db-after
 :<- [::epochs/selected-event-trace]
 (fn [trace _]
   (get-in trace [:tags :app-db-after])))

(rf/reg-sub
 ::current-epoch-app-db-before
 :<- [::epochs/selected-event-trace]
 (fn [trace _]
   (get-in trace [:tags :app-db-before])))

(rf/reg-sub
 ::paths
 :<- [::root]
 (fn [{:keys [paths]} _]
   (reverse
    (map #(assoc (val %) :id (key %))
         paths))))

;; [IJ] TODO: This doesn't appear to be used anywhere:
(rf/reg-sub
 ::search-string
 :<- [::root]
 (fn [{:keys [search-string]} _]
   search-string))

(rf/reg-sub
 ::expansions
 :<- [::root]
 (fn [{:keys [json-ml-expansions]} _]
   json-ml-expansions))

(rf/reg-sub
 ::node-expanded?
 :<- [::expansions]
 (fn [expansions [_ path]]
   (contains? expansions path)))

;; [IJ] TODO: This doesn't appear to be used anywhere:
(rf/reg-sub
 ::reagent-id
 :<- [::root]
 (fn [{:keys [reagent-id]} _]
   reagent-id))

(rf/reg-sub
 ::data-path-annotations?
 :<- [::root]
 (fn [{:keys [data-path-annotations?]} _]
   data-path-annotations?))

(rf/reg-sub
 ::expand-all?
 :<- [::root]
 (fn [{:keys [expand-all?]} [_ path-id]]
   (get expand-all? path-id)))
