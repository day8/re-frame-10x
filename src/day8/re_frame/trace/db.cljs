(ns day8.re-frame.trace.db
  (:require [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [day8.re-frame.trace.utils.localstorage :as localstorage]))

(defn init-db []
  (let [panel-width% (localstorage/get "panel-width-ratio" 0.35)
        show-panel? (localstorage/get "show-panel" false)
        selected-tab (localstorage/get "selected-tab" :traces)
        filter-items (localstorage/get "filter-items" [])
        app-db-paths (localstorage/get "app-db-paths" '())
        json-ml-paths (localstorage/get "app-db-json-ml-expansions" #{})
        external-window? (localstorage/get "external-window?" false)
        using-trace? (localstorage/get "using-trace?" true)]
    (when using-trace?
      (rf/dispatch [:global/enable-tracing]))
    (rf/dispatch [:settings/panel-width% panel-width%])
    (rf/dispatch [:settings/show-panel? show-panel?])
    (rf/dispatch [:settings/selected-tab selected-tab])
    (when external-window?
      (rf/dispatch [:global/launch-external]))
    (rf/dispatch [:traces/filter-items filter-items])
    (rf/dispatch [:app-db/paths app-db-paths])
    (rf/dispatch [:app-db/set-json-ml-paths json-ml-paths])
    (rf/dispatch [:global/add-unload-hook])))
