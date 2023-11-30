(ns day8.re-frame-10x.events
  (:require
   [re-frame.core]
   [re-frame.db]
   [re-frame.trace]
   [re-frame.interop]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core :as rf]
   [day8.re-frame-10x.fx.local-storage                           :as local-storage]
   [day8.re-frame-10x.fx.log                                     :as log]
   [day8.re-frame-10x.navigation.events                          :as navigation.events]
   [day8.re-frame-10x.navigation.views                           :as navigation.views]
   [day8.re-frame-10x.panels.app-db.events                       :as app-db.events]
   [day8.re-frame-10x.panels.settings.events                     :as settings.events]
   [day8.re-frame-10x.panels.traces.events                       :as traces.events]))

(rf/reg-event-fx
 ::init
 [(rf/inject-cofx ::local-storage/load {:key "panel-width-ratio" :or 0.35})
  (rf/inject-cofx ::local-storage/load {:key "show-panel" :or true})
  (rf/inject-cofx ::local-storage/load {:key "selected-tab" :or :event})
  (rf/inject-cofx ::local-storage/load {:key "filter-items" :or []})
  (rf/inject-cofx ::local-storage/load {:key "app-db-json-ml-expansions" :or #{}})
  (rf/inject-cofx ::local-storage/load {:key "external-window?" :or false})
  (rf/inject-cofx ::local-storage/load {:key "external-window-dimensions" :or {:width 800 :height 800 :top 0 :left 0}})
  (rf/inject-cofx ::local-storage/load {:key "show-epoch-traces?" :or true})
  (rf/inject-cofx ::local-storage/load {:key "using-trace?" :or true})
  (rf/inject-cofx ::local-storage/load {:key "ignored-events" :or {}})
  (rf/inject-cofx ::local-storage/load {:key "low-level-trace" :or {:reagent true :re-frame true}})
  (rf/inject-cofx ::local-storage/load {:key "filtered-view-trace" :or (let [id1 (random-uuid)
                                                                             id2  (random-uuid)]
                                                                         {id1 {:id id1 :ns-str "re-com.box" :ns 're-com.box :sort 0}
                                                                          id2 {:id id2 :ns-str "re-com.input-text" :ns 're-com.input-text :sort 1}})})
  (rf/inject-cofx ::local-storage/load {:key "retained-epochs" :or 25})
  (rf/inject-cofx ::local-storage/load {:key "app-db-paths" :or {}})
  (rf/inject-cofx ::local-storage/load {:key "app-db-follows-events?" :or true})
  (rf/inject-cofx ::local-storage/load {:key "ambiance" :or :bright})
  (rf/inject-cofx ::local-storage/load {:key "syntax-color-scheme" :or :cljs-devtools})
  (rf/inject-cofx ::local-storage/load {:key "categories" :or #{:event :sub/run :sub/create :sub/dispose}})
  (rf/inject-cofx ::local-storage/load {:key "data-path-annotations?" :or false})
  (rf/inject-cofx ::local-storage/load {:key "show-event-history" :or true})
  (rf/inject-cofx ::local-storage/load {:key "open-new-inspectors?" :or true})
  (rf/inject-cofx ::local-storage/load {:key "handle-keys?" :or true})
  (rf/inject-cofx ::local-storage/load {:key "key-bindings" :or {:show-panel {:key "X"
                                                                              :altKey false
                                                                              :ctrlKey true
                                                                              :metaKey false
                                                                              :shiftKey true}}})
  (rf/inject-cofx ::local-storage/load {:key "log-outputs" :or [:day8.re-frame-10x.fx.log/console]})
  (rf/inject-cofx ::local-storage/load {:key "log-pretty?" :or true})
  (rf/inject-cofx ::local-storage/load {:key "expansion-limit" :or 1000})
  (rf/inject-cofx ::local-storage/load {:key "ns-aliases" :or
                                        (let [id (random-uuid)]
                                          {id {:id id :ns-full "long-namespace" :ns-alias "ln"}})})
  (rf/inject-cofx ::local-storage/load {:key "alias-namespaces?"})
  rf/unwrap]
 (fn [{:keys [panel-width-ratio show-panel selected-tab filter-items app-db-json-ml-expansions
              external-window? external-window-dimensions show-epoch-traces? using-trace?
              ignored-events low-level-trace filtered-view-trace retained-epochs app-db-paths
              app-db-follows-events? ambiance syntax-color-scheme categories data-path-annotations?
              show-event-history open-new-inspectors? handle-keys? key-bindings log-outputs log-pretty?
              expansion-limit ns-aliases alias-namespaces?]}
      {:keys [debug?]}]
   {:fx [(when using-trace?
           [:dispatch [::settings.events/enable-tracing]])
         [:dispatch [::settings.events/panel-width% panel-width-ratio]]
         [:dispatch [::settings.events/show-panel? show-panel]]
         [:dispatch [::settings.events/selected-tab selected-tab]]
         [:dispatch [::settings.events/set-ignored-events ignored-events]]
         [:dispatch [::settings.events/set-filtered-view-trace filtered-view-trace]]
         [:dispatch [::settings.events/set-low-level-trace low-level-trace]]
         [:dispatch [::settings.events/set-number-of-retained-epochs retained-epochs]]
         [:dispatch [::settings.events/app-db-follows-events? app-db-follows-events?]]
         [:dispatch [::settings.events/set-ambiance ambiance]]
         [:dispatch [::settings.events/set-syntax-color-scheme syntax-color-scheme]]
         [:dispatch [::settings.events/debug? debug?]]
          ;; Important that window dimensions are set before we open an external window.
         [:dispatch [::settings.events/external-window-dimensions external-window-dimensions]]
         [:dispatch [::app-db.events/set-data-path-annotations? data-path-annotations?]]
         (when external-window?
           [:dispatch [::navigation.events/launch-external navigation.views/mount]])
         [:dispatch [::traces.events/set-queries filter-items]]
         [:dispatch [::traces.events/set-categories categories]]
         [:dispatch [::traces.events/set-filter-by-selected-epoch? show-epoch-traces?]]
         [:dispatch [::app-db.events/paths (into (sorted-map) app-db-paths)]]
         [:dispatch [::app-db.events/set-json-ml-paths app-db-json-ml-expansions]]
         [:dispatch [:global/add-unload-hook]]
         [:dispatch [::app-db.events/reagent-id]]
         [:dispatch [::settings.events/show-event-history? show-event-history]]
         [:dispatch [::settings.events/open-new-inspectors? open-new-inspectors?]]
         [:dispatch [::settings.events/handle-keys? handle-keys?]]
         [:dispatch [::settings.events/key-bindings key-bindings]]
         [:dispatch [::settings.events/log-outputs log-outputs]]
         [:dispatch [::settings.events/log-pretty? log-pretty?]]
         [:dispatch [::settings.events/expansion-limit expansion-limit]]
         [:dispatch [::settings.events/ns-aliases ns-aliases]]
         [:dispatch [::settings.events/alias-namespaces? alias-namespaces?]]]}))

;; Global

(rf/reg-event-fx
 :global/add-unload-hook
 (fn [_ _]
   (js/window.addEventListener "beforeunload" #(rf/dispatch-sync [:global/unloading? true]))
   nil))

(rf/reg-event-db
 :global/unloading?
 (fn [db [_ unloading?]]
   (assoc-in db [:global :unloading?] unloading?)))

(rf/reg-event-fx
 :global/log
 [(rf/path [:settings]) rf/trim-v]
 (fn [{{:keys [log-outputs log-pretty?]} :db} [value]]
   {:fx (mapv #(do [% {:value value :pretty? log-pretty?}])
              log-outputs)}))
