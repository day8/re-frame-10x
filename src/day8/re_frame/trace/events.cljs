(ns day8.re-frame.trace.events
  (:require [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [day8.re-frame.trace.utils.utils :as utils]
            [day8.re-frame.trace.utils.localstorage :as localstorage]
            [clojure.string :as str]
            [reagent.core :as r]
            [goog.object]
            [re-frame.db]
            [day8.re-frame.trace.view.container :as container]
            [day8.re-frame.trace.styles :as styles]))

(defonce traces (r/atom []))
(defonce total-traces (r/atom 0))

(defn log-trace? [trace]
  (let [render-operation? (= (:op-type trace) :render)
        component-path    (get-in trace [:tags :component-path] "")]
    (if-not render-operation?
      true
      (not (str/includes? component-path "devtools outer")))))

(defn disable-tracing! []
  (re-frame.trace/remove-trace-cb ::cb))

(defn enable-tracing! []
  (re-frame.trace/register-trace-cb ::cb (fn [new-traces]
                                           (when-let [new-traces (filter log-trace? new-traces)]
                                             (swap! total-traces + (count new-traces))
                                             (swap! traces
                                                    (fn [existing]
                                                      (let [new  (reduce conj existing new-traces)
                                                            size (count new)]
                                                        (if (< 4000 size)
                                                          (let [new2 (subvec new (- size 2000))]
                                                            (if (< @total-traces 20000) ;; Create a new vector to avoid structurally sharing all traces forever
                                                              (do (reset! total-traces 0)
                                                                  (into [] new2))))
                                                          new))))))))

(defn dissoc-in
  "Dissociates an entry from a nested associative structure returning a new
  nested structure. keys is a sequence of keys. Any empty maps that result
  will not be present in the new structure."
  [m [k & ks :as keys]]
  (if ks
    (if-let [nextmap (clojure.core/get m k)]
      (let [newmap (dissoc-in nextmap ks)]
        (if (seq newmap)
          (assoc m k newmap)
          (dissoc m k)))
      m)
    (dissoc m k)))


(rf/reg-event-db
  :settings/panel-width%
  (fn [db [_ width%]]
    (localstorage/save! "panel-width-ratio" (max width% 0.05))
    (assoc-in db [:settings :panel-width%] (max width% 0.05))))

(rf/reg-event-db
  :settings/selected-tab
  (fn [db [_ selected-tab]]
    (localstorage/save! "selected-tab" selected-tab)
    (assoc-in db [:settings :selected-tab] selected-tab)))

(rf/reg-event-db
  :settings/show-panel?
  (fn [db [_ show-panel?]]
    (localstorage/save! "show-panel" show-panel?)
    (assoc-in db [:settings :show-panel?] show-panel?)))

(rf/reg-event-db
  :settings/user-toggle-panel
  (fn [db _]
    (let [now-showing?    (not (get-in db [:settings :show-panel?]))
          external-panel? (get-in db [:settings :external-window?])
          using-trace?    (or external-panel? now-showing?)]
      (if now-showing?
        (enable-tracing!)
        (when-not external-panel?
          (disable-tracing!)))
      (localstorage/save! "using-trace?" using-trace?)
      (localstorage/save! "show-panel" now-showing?)
      (-> db
          (assoc-in [:settings :using-trace?] using-trace?)
          (assoc-in [:settings :show-panel?] now-showing?)))))

;; Global

(defn mount [popup-window popup-document]
  (let [app (.getElementById popup-document "--re-frame-trace--")
        doc js/document]
    (styles/inject-trace-styles popup-document)
    (goog.object/set popup-window "onunload" #(rf/dispatch [:global/external-closed]))
    (r/render
      [(r/create-class
         {:display-name   "devtools outer external"
          :reagent-render (fn []
                            [container/devtools-inner traces {:panel-type :popup}
                             ])})]
      app)))

(defn open-debugger-window
  "Copied from re-frisk.devtool/open-debugger-window"
  []
  (let [{:keys [ext_height ext_width]} (:prefs {})
        w (js/window.open "" "Debugger" (str "width=" (or ext_width 800) ",height=" (or ext_height 800)
                                             ",resizable=yes,scrollbars=yes,status=no,directories=no,toolbar=no,menubar=no"))
        d (.-document w)]
    (.open d)
    (.write d "<head></head><body style=\"margin: 0px;\"><div id=\"--re-frame-trace--\" class=\"external-window\"></div></body>")
    (goog.object/set w "onload" #(mount w d))
    (.close d)))

(rf/reg-event-fx
  :global/launch-external
  (fn [ctx _]
    (open-debugger-window)
    (localstorage/save! "external-window?" true)
    {:db             (assoc-in (:db ctx) [:settings :external-window?] true)
     ;; TODO: capture the intent that the user is still interacting with devtools, to persist between reloads.
     :dispatch-later [{:ms 200 :dispatch [:settings/show-panel? false]}]}))

(rf/reg-event-fx
  :global/external-closed
  (fn [ctx _]
    (localstorage/save! "external-window?" false)
    {:db             (assoc-in (:db ctx) [:settings :external-window?] false)
     :dispatch-later [{:ms 400 :dispatch [:settings/show-panel? true]}]}))

(rf/reg-event-fx
  :global/enable-tracing
  (fn [ctx _]
    (enable-tracing!)
    nil))

(rf/reg-event-fx
  :global/disable-tracing
  (fn [ctx _]
    (disable-tracing!)
    nil))

(rf/reg-event-fx
  :global/add-unload-hook
  (fn [_ _]
    (js/window.addEventListener "beforeunload" #(rf/dispatch-sync [:global/unloading? true]))
    nil))

(rf/reg-event-db
  :global/unloading?
  (fn [db [_ unloading?]]
    (assoc-in db [:global :unloading?] unloading?)))

;; Traces

(defn save-filter-items [filter-items]
  (localstorage/save! "filter-items" filter-items))

(rf/reg-event-db
  :traces/filter-items
  (fn [db [_ filter-items]]
    (save-filter-items filter-items)
    (assoc-in db [:traces :filter-items] filter-items)))

(rf/reg-event-db
  :traces/add-filter
  [(rf/path [:traces :filter-items])]
  (fn [filter-items [_ filter-input filter-type]]
    (let [new-db (when-not (some #(= filter-input (:query %)) filter-items) ;; prevent duplicate filter strings
                   ;; if existing, remove prior filter for :slower-than
                   ;; TODO: rework how time filters are used.
                   (when (and (= :slower-than filter-type)
                              (some #(= filter-type (:filter-type %)) filter-items))
                     (remove #(= :slower-than (:filter-type %)) filter-items))
                   ;; add new filter
                   (conj filter-items {:id          (random-uuid)
                                       :query       (if (= filter-type :contains)
                                                      (str/lower-case filter-input)
                                                      (js/parseFloat filter-input))
                                       :filter-type filter-type}))]
      (save-filter-items new-db)
      new-db)))

(rf/reg-event-db
  :traces/remove-filter
  [(rf/path [:traces :filter-items])]
  (fn [filter-items [_ filter-id]]
    (let [new-db (remove #(= (:id %) filter-id) filter-items)]
      (save-filter-items new-db)
      new-db)))

(rf/reg-event-db
  :traces/reset-filter-items
  (fn [db _]
    (let [new-db (dissoc-in db [:traces :filter-items])]
      (save-filter-items (get-in new-db [:traces :filter-items]))
      new-db)))

;; App DB

(rf/reg-event-db
  :app-db/paths
  (fn [db [_ paths]]
    (let [new-paths (into [] paths)]                        ;; Don't use sets, use vectors
      (localstorage/save! "app-db-paths" paths)
      (assoc-in db [:app-db :paths] paths))))

(rf/reg-event-db
  :app-db/remove-path
  (fn [db [_ path]]
    (let [new-db (update-in db [:app-db :paths] #(remove (fn [p] (= p path)) %))]
      (localstorage/save! "app-db-paths" (get-in new-db [:app-db :paths]))
      ;; TODO: remove from json-ml expansions too.
      new-db)))

(rf/reg-event-db
  :app-db/add-path
  (fn [db _]
    (let [search-string (get-in db [:app-db :search-string])
          path          (try
                          (when-not (str/blank? search-string)
                            (cljs.reader/read-string (str "[" search-string "]")))
                          (catch :default e
                            nil))]
      (if (some? path)
        (do (localstorage/save! "app-db-paths" (cons path (get-in db [:app-db :paths])))
            (rf/dispatch [:app-db/toggle-expansion [path]])
            (-> db
                (update-in [:app-db :paths] #(cons path %))
                (assoc-in [:app-db :search-string] "")))
        db))))

(rf/reg-event-db
  :app-db/search-string
  (fn [db [_ search-string]]
    (assoc-in db [:app-db :search-string] search-string)))

(rf/reg-event-db
  :app-db/set-json-ml-paths
  [(rf/path [:app-db :json-ml-expansions])]
  (fn [db [_ paths]]
    (localstorage/save! "app-db-json-ml-expansions" paths)
    paths))

(rf/reg-event-db
  :app-db/toggle-expansion
  [(rf/path [:app-db :json-ml-expansions])]
  (fn [paths [_ path]]
    (let [new-paths (if (contains? paths path)
                      (disj paths path)
                      (conj paths path))]
      (localstorage/save! "app-db-json-ml-expansions" new-paths)
      new-paths)))

(rf/reg-event-db
  :snapshot/save-snapshot
  [(rf/path [:snapshot])]
  (fn [snapshot _]
    (assoc snapshot :current-snapshot @re-frame.db/app-db)))

(rf/reg-event-db
  :snapshot/load-snapshot
  [(rf/path [:snapshot])]
  (fn [snapshot _]
    (reset! re-frame.db/app-db (:current-snapshot snapshot))
    snapshot))
