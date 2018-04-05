(ns day8.re-frame-10x.db
  (:require [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [day8.re-frame-10x.utils.localstorage :as localstorage]))

(defn init-db [debug?]
  (let [panel-width% (localstorage/get "panel-width-ratio" 0.35)
        show-panel? (localstorage/get "show-panel" true)
        selected-tab (localstorage/get "selected-tab" :event)
        filter-items (localstorage/get "filter-items" [])
        app-db-paths (into (sorted-map) (localstorage/get "app-db-paths" {}))
        json-ml-paths (localstorage/get "app-db-json-ml-expansions" #{})
        external-window? (localstorage/get "external-window?" false)
        external-window-dimensions (localstorage/get "external-window-dimensions" {:width 800 :height 800 :top 0 :left 0})
        show-epoch-traces? (localstorage/get "show-epoch-traces?" true)
        using-trace? (localstorage/get "using-trace?" true)
        ignored-events (localstorage/get "ignored-events" {})
        low-level-trace (localstorage/get "low-level-trace" {:reagent true :re-frame true})
        filtered-view-trace (localstorage/get "filtered-view-trace" (let [id1 (random-uuid)
                                                                          id2 (random-uuid)]
                                                                      {id1 {:id id1 :ns-str "re-com.box" :ns 're-com.box :sort 0}
                                                                       id2 {:id id2 :ns-str "re-com.input-text" :ns 're-com.input-text :sort 1}}))
        num-epochs (localstorage/get "retained-epochs" 5)
        follows-events? (localstorage/get "app-db-follows-events?" true)
        categories (localstorage/get "categories" #{:event :sub/run :sub/create :sub/dispose})]
    (when using-trace?
      (rf/dispatch [:global/enable-tracing]))
    (rf/dispatch [:settings/panel-width% panel-width%])
    (rf/dispatch [:settings/show-panel? show-panel?])
    (rf/dispatch [:settings/selected-tab selected-tab])
    (rf/dispatch [:settings/set-ignored-events ignored-events])
    (rf/dispatch [:settings/set-filtered-view-trace filtered-view-trace])
    (rf/dispatch [:settings/set-low-level-trace low-level-trace])
    (rf/dispatch [:settings/set-number-of-retained-epochs num-epochs])
    (rf/dispatch [:settings/app-db-follows-events? follows-events?])
    (rf/dispatch [:settings/debug? debug?])
    (when external-window?
      (rf/dispatch [:global/launch-external]))
    (rf/dispatch [:settings/external-window-dimensions external-window-dimensions])
    (rf/dispatch [:traces/filter-items filter-items])
    (rf/dispatch [:traces/set-categories categories])
    (rf/dispatch [:trace-panel/update-show-epoch-traces? show-epoch-traces?])
    (rf/dispatch [:app-db/paths app-db-paths])
    (rf/dispatch [:app-db/set-json-ml-paths json-ml-paths])
    (rf/dispatch [:global/add-unload-hook])
    (rf/dispatch [:app-db/reagent-id])))
