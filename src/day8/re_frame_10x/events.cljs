(ns day8.re-frame-10x.events
  (:require [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [mranderson047.reagent.v0v7v0.reagent.core :as r]
            [cljs.tools.reader.edn]
            [day8.re-frame-10x.utils.utils :as utils :refer [spy]]
            [day8.re-frame-10x.utils.localstorage :as localstorage]
            [reagent.impl.batching :as batching]
            [clojure.string :as str]
            [goog.object]
            [goog.string]
            [re-frame.db]
            [re-frame.interop]
            [re-frame.core]
            [re-frame.trace]
            [day8.re-frame-10x.view.container :as container]
            [day8.re-frame-10x.styles :as styles]
            [clojure.set :as set]
            [day8.re-frame-10x.metamorphic :as metam]))

(defn fixed-after
  ;; Waiting on https://github.com/Day8/re-frame/issues/447
  [f]
  (rf/->interceptor
    :id :after
    :after (fn after-after
             [context]
             (let [db    (if (contains? (:effects context) :db)
                           (get-in context [:effects :db])
                           (get-in context [:coeffects :db]))
                   event (get-in context [:coeffects :event])]
               (f db event)                                 ;; call f for side effects
               context))))                                  ;; context is unchanged

(defn log-trace? [trace]
  (let [render-operation? (or (= (:op-type trace) :render)
                              (= (:op-type trace) :componentWillUnmount))
        component-path    (get-in trace [:tags :component-path] "")]
    (if-not render-operation?
      true
      (not (str/includes? component-path "devtools outer")))))

(defn disable-tracing! []
  (re-frame.trace/remove-trace-cb ::cb))

(defn enable-tracing! []
  (re-frame.trace/register-trace-cb ::cb #(rf/dispatch [:epochs/receive-new-traces %])))

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

(defn read-string-maybe [s]
  (try (cljs.tools.reader.edn/read-string s)
       (catch :default e
         nil)))

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
  :settings/toggle-settings
  (fn [db _]
    (update-in db [:settings :showing-settings?] not)))

(rf/reg-event-db
  :settings/show-panel?
  (fn [db [_ show-panel?]]
    (localstorage/save! "show-panel" show-panel?)
    (assoc-in db [:settings :show-panel?] show-panel?)))

(rf/reg-event-db
  :settings/factory-reset
  (fn [db _]
    (localstorage/delete-all-keys!)
    (js/location.reload)
    db))

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

(rf/reg-event-db
  :settings/set-number-of-retained-epochs
  (fn [db [_ num-str]]
    ;; TODO: this is not perfect, there is an issue in re-com
    ;; where it won't update its model if it never receives another
    ;; changes after it's on-change is fired.
    ;; TODO: you could reset the stored epochs on change here
    ;; once the way they are processed is refactored.
    (let [num (js/parseInt num-str)
          num (if (and (not (js/isNaN num)) (pos-int? num))
                num
                5)]
      (localstorage/save! "retained-epochs" num)
      (assoc-in db [:settings :number-of-epochs] num))))

(def ignored-event-mw
  [(rf/path [:settings :ignored-events]) (fixed-after #(localstorage/save! "ignored-events" %))])

(rf/reg-event-db
  :settings/add-ignored-event
  ignored-event-mw
  (fn [ignored-events _]
    (let [id (random-uuid)]
      (assoc ignored-events id {:id id :event-str "" :event-id nil :sort (js/Date.now)}))))

(rf/reg-event-db
  :settings/remove-ignored-event
  ignored-event-mw
  (fn [ignored-events [_ id]]
    (dissoc ignored-events id)))

(rf/reg-event-db
  :settings/update-ignored-event
  ignored-event-mw
  (fn [ignored-events [_ id event-str]]
    ;; TODO: this won't inform users if they type bad strings in.
    (let [event (read-string-maybe event-str)]
      (-> ignored-events
          (assoc-in [id :event-str] event-str)
          (update-in [id :event-id] (fn [old-event] (if event event old-event)))))))

(rf/reg-event-db
  :settings/set-ignored-events
  ignored-event-mw
  (fn [_ [_ ignored-events]]
    ignored-events))

(def filtered-view-trace-mw
  [(rf/path [:settings :filtered-view-trace]) (fixed-after #(localstorage/save! "filtered-view-trace" %))])

(rf/reg-event-db
  :settings/add-filtered-view-trace
  filtered-view-trace-mw
  (fn [filtered-view-trace _]
    (let [id (random-uuid)]
      (assoc filtered-view-trace id {:id id :ns-str "" :ns nil :sort (js/Date.now)}))))

(rf/reg-event-db
  :settings/remove-filtered-view-trace
  filtered-view-trace-mw
  (fn [filtered-view-trace [_ id]]
    (dissoc filtered-view-trace id)))

(rf/reg-event-db
  :settings/update-filtered-view-trace
  filtered-view-trace-mw
  (fn [filtered-view-trace [_ id ns-str]]
    ;; TODO: this won't inform users if they type bad strings in.
    (let [event (read-string-maybe ns-str)]
      (-> filtered-view-trace
          (assoc-in [id :ns-str] ns-str)
          (update-in [id :ns] (fn [old-event] (if event event old-event)))))))

(rf/reg-event-db
  :settings/set-filtered-view-trace
  filtered-view-trace-mw
  (fn [_ [_ ignored-events]]
    ignored-events))

(def low-level-trace-mw [(rf/path [:settings :low-level-trace]) (fixed-after #(localstorage/save! "low-level-trace" %))])

(rf/reg-event-db
  :settings/set-low-level-trace
  low-level-trace-mw
  (fn [_ [_ low-level]]
    low-level))

(rf/reg-event-db
  :settings/low-level-trace
  low-level-trace-mw
  (fn [low-level [_ trace-type capture?]]
    (assoc low-level trace-type capture?)))

(rf/reg-event-db
  :settings/debug?
  (fn [db [_ debug?]]
    (assoc-in db [:settings :debug?] debug?)))

(rf/reg-event-db
  :settings/app-db-follows-events?
  [(rf/path [:settings :app-db-follows-events?]) (fixed-after #(localstorage/save! "app-db-follows-events?" %))]
  (fn [db [_ follows-events?]]
    follows-events?))

;; Global

(defn mount [popup-window popup-document]
  ;; When programming here, we need to be careful about which document and window
  ;; we are operating on, and keep in mind that the window can close without going
  ;; through standard react lifecycle, so we hook the beforeunload event.
  (let [app                      (.getElementById popup-document "--re-frame-10x--")
        resize-update-scheduled? (atom false)
        handle-window-resize     (fn [e]
                                   (when-not @resize-update-scheduled?
                                     (batching/next-tick
                                       (fn []
                                         (let [width  (.-innerWidth popup-window)
                                               height (.-innerHeight popup-window)]
                                           (rf/dispatch [:settings/external-window-resize {:width width :height height}]))
                                         (reset! resize-update-scheduled? false)))
                                     (reset! resize-update-scheduled? true)))
        handle-window-position   (let [pos (atom {})]
                                   (fn []
                                     ;; Only update re-frame if the windows position has changed.
                                     (let [{:keys [left top]} @pos
                                           screen-left (.-screenX popup-window)
                                           screen-top  (.-screenY popup-window)]
                                       (when (or (not= left screen-left)
                                                 (not= top screen-top))
                                         (rf/dispatch [:settings/external-window-position {:left screen-left :top screen-top}])
                                         (reset! pos {:left screen-left :top screen-top})))))
        window-position-interval (atom nil)
        unmount                  (fn [_]
                                   (.removeEventListener popup-window "resize" handle-window-resize)
                                   (some-> @window-position-interval js/clearInterval)
                                   nil)]


    (styles/inject-trace-styles popup-document)
    (goog.object/set popup-window "onunload" #(rf/dispatch [:global/external-closed]))
    (r/render
      [(r/create-class
         {:display-name           "devtools outer external"
          :component-did-mount    (fn []
                                    (.addEventListener popup-window "resize" handle-window-resize)
                                    (.addEventListener popup-window "beforeunload" unmount)
                                    ;; Check the window position every 10 seconds
                                    (reset! window-position-interval
                                            (js/setInterval
                                              handle-window-position
                                              2000)))
          :component-will-unmount unmount
          :reagent-render         (fn [] [container/devtools-inner {:panel-type :popup}])})]
      app)))

(defn open-debugger-window
  "Originally copied from re-frisk.devtool/open-debugger-window"
  [{:keys [width height top left] :as dimensions}]
  (let [doc-title        js/document.title
        new-window-title (goog.string/escapeString (str "re-frame-10x | " doc-title))
        new-window-html  (str "<head><title>"
                              new-window-title
                              "</title></head><body style=\"margin: 0px;\"><div id=\"--re-frame-10x--\" class=\"external-window\"></div></body>")
        ;; We would like to set the windows left and top positions to match the monitor that it was on previously, but Chrome doesn't give us
        ;; control over this, it will only position it within the same display that it was popped out on.
        w                (js/window.open "" "re-frame-10x-popout"
                                         (str "width=" width ",height=" height ",left=" left ",top=" top
                                              ",resizable=yes,scrollbars=yes,status=no,directories=no,toolbar=no,menubar=no"))

        d                (.-document w)]
    (when-let [el (.getElementById d "--re-frame-10x--")]
      (r/unmount-component-at-node el))
    (.open d)
    (.write d new-window-html)
    (goog.object/set w "onload" #(mount w d))
    (.close d)))

(rf/reg-event-fx
  :global/launch-external
  (fn [ctx _]
    (open-debugger-window (get-in ctx [:db :settings :external-window-dimensions]))
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

(rf/reg-event-db
  :settings/external-window-dimensions
  [(rf/path [:settings :external-window-dimensions]) (rf/after #(localstorage/save! "external-window-dimensions" %))]
  (fn [dim [_ new-dim]]
    new-dim))

(rf/reg-event-db
  :settings/external-window-resize
  [(rf/path [:settings :external-window-dimensions]) (rf/after #(localstorage/save! "external-window-dimensions" %))]
  (fn [dim [_ {width :width height :height}]]
    (assoc dim :width width :height height)))

(rf/reg-event-db
  :settings/external-window-position
  [(rf/path [:settings :external-window-dimensions]) (rf/after #(localstorage/save! "external-window-dimensions" %))]
  (fn [dim [_ {left :left top :top}]]
    (assoc dim :left left :top top)))

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
                   (let [filter-items (if (and (= :slower-than filter-type)
                                               (some #(= filter-type (:filter-type %)) filter-items))
                                        (remove #(= :slower-than (:filter-type %)) filter-items)
                                        filter-items)]
                     ;; add new filter
                     (conj filter-items {:id          (random-uuid)
                                         :query       (if (= filter-type :contains)
                                                        (str/lower-case filter-input)
                                                        (js/parseFloat filter-input))
                                         :filter-type filter-type})))]
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

(rf/reg-event-db
  :traces/toggle-all-expansions
  [(rf/path [:traces :expansions])]
  (fn [trace-detail-expansions _]
    (-> trace-detail-expansions
        (assoc :overrides {})
        (update :show-all? not))))

(rf/reg-event-db
  :traces/toggle-trace
  [(rf/path [:traces :expansions])]
  (fn [expansions [_ id]]
    (let [showing? (get-in expansions [:overrides id] (:show-all? expansions))]
      (update-in expansions [:overrides id] #(if showing? false (not %))))))

(rf/reg-event-db
  :traces/toggle-categories
  [(rf/path [:traces :categories])]
  (fn [categories [_ new-categories]]
    (let [new-categories (if (set/superset? categories new-categories)
                           (set/difference categories new-categories)
                           (set/union categories new-categories))]
      (localstorage/save! "categories" new-categories)
      new-categories)))

(rf/reg-event-db
  :traces/set-categories
  [(rf/path [:traces :categories])]
  (fn [categories [_ new-categories]]
    new-categories))


(rf/reg-event-db
  :trace-panel/update-show-epoch-traces?
  [(rf/path [:trace-panel :show-epoch-traces?]) (fixed-after #(localstorage/save! "show-epoch-traces?" %))]
  (fn [_ [k show-epoch-traces?]]
    show-epoch-traces?))

;; App DB

(def app-db-path-mw
  [(rf/path [:app-db :paths]) (fixed-after #(localstorage/save! "app-db-paths" %))])

(rf/reg-event-db
  :app-db/create-path
  app-db-path-mw
  (fn [paths _]

    (assoc paths (js/Date.now) {:diff? false :open? true :path nil :path-str "" :valid-path? true})))



;; The core idea with :app-db/update-path and :app-db/update-path-blur
;; is that we need to separate the users text input (`path-str`) with the
;; parsing of that string (`path`). We let the user type any string that
;; they like, and check it for validity on each change. If it is valid
;; then we update `path` and mark the pod as valid. If it isn't valid then
;; we don't update `path` and mark the pod as invalid.
;;
;; On blur of the input, we reset path-str to the last valid path, if
;; the pod isn't currently valid.

(rf/reg-event-db
  :app-db/update-path
  app-db-path-mw
  (fn [paths [_ path-id path-str]]
    (let [path  (read-string-maybe path-str)
          paths (assoc-in paths [path-id :path-str] path-str)]
      (if (or (and (some? path)
                   (sequential? path))
              (str/blank? path-str))
        (-> paths
            (assoc-in [path-id :path] path)
            (assoc-in [path-id :valid-path?] true))
        (assoc-in paths [path-id :valid-path?] false)))))

(rf/reg-event-db
  :app-db/update-path-blur
  app-db-path-mw
  (fn [paths [_ path-id]]
    (let [{:keys [valid-path? path]} (get paths path-id)]
      (if valid-path?
        paths
        (-> (assoc-in paths [path-id :path-str] (pr-str path))
            (assoc-in [path-id :valid-path?] true))))))

(rf/reg-event-db
  :app-db/set-path-visibility
  app-db-path-mw
  (fn [paths [_ path-id open?]]
    (assoc-in paths [path-id :open?] open?)))

(rf/reg-event-db
  :app-db/set-diff-visibility
  app-db-path-mw
  (fn [paths [_ path-id diff?]]
    (let [open? (if diff?
                  true
                  (get-in paths [path-id :open?]))]
      (-> paths
          (assoc-in [path-id :diff?] diff?)
          ;; If we turn on diffing then we want to also expand the path
          (assoc-in [path-id :open?] open?)))))

(rf/reg-event-db
  :app-db/remove-path
  app-db-path-mw
  (fn [paths [_ path-id]]
    (dissoc paths path-id)))

(rf/reg-event-db
  :app-db/paths
  app-db-path-mw
  (fn [db [_ paths]]
    paths))

#_(rf/reg-event-db
    :app-db/remove-path
    (fn [db [_ path]]
      (let [new-db (update-in db [:app-db :paths] #(remove (fn [p] (= p path)) %))]
        (localstorage/save! "app-db-paths" (get-in new-db [:app-db :paths]))
        ;; TODO: remove from json-ml expansions too.
        new-db)))

#_(rf/reg-event-db
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
  :app-db/reagent-id
  [(rf/path [:app-db :reagent-id])]
  (fn [paths _]
    (re-frame.interop/reagent-id re-frame.db/app-db)))

(rf/reg-event-db
  :snapshot/reset-current-epoch-app-db
  (fn [db [_ new-id]]
    (when (get-in db [:settings :app-db-follows-events?])
      (let [epochs   (:epochs db)
            match-id (or new-id
                         ;; new-id may be nil when we call this event from :settings/play
                         (utils/last-in-vec (get epochs :match-ids)))
            match    (get-in epochs [:matches-by-id match-id])
            event    (metam/matched-event (:match-info match))]
        ;; Don't mess up the users app if there is a problem getting app-db-after.
        (when-some [new-db (metam/app-db-after event)]
          (reset! re-frame.db/app-db new-db))))
    db))

;;;

(defn first-match-id
  [m]
  (-> m :match-info first :id))

(rf/reg-event-fx
  :epochs/receive-new-traces
  (fn [{:keys [db]} [_ new-traces]]
    (if-let [filtered-traces (->> (filter log-trace? new-traces)
                                  (sort-by :id))]
      (let [number-of-epochs-to-retain (get-in db [:settings :number-of-epochs])
            events-to-ignore           (->> (get-in db [:settings :ignored-events]) vals (map :event-id) set)
            previous-traces            (get-in db [:traces :all-traces] [])
            parse-state                (get-in db [:epochs :parse-state] metam/initial-parse-state)
            {drop-re-frame :re-frame drop-reagent :reagent} (get-in db [:settings :low-level-trace])
            all-traces                 (reduce conj previous-traces filtered-traces)
            parse-state                (metam/parse-traces parse-state filtered-traces)
            ;; TODO:!!!!!!!!!!!!! We should be parsing everything else with the traces that span the newly matched
            ;; epochs, not the filtered-traces, as these are only partial.
            new-matches                (:partitions parse-state)
            previous-matches           (get-in db [:epochs :matches] [])
            parse-state                (assoc parse-state :partitions []) ;; Remove matches we know about
            new-matches                (remove (fn [match]
                                                 (let [event (get-in (metam/matched-event match) [:tags :event])]
                                                   (contains? events-to-ignore (first event)))) new-matches)
            ;; subscription-info is calculated separately from subscription-match-state because they serve different purposes:
            ;; - subscription-info collects all the data that we know about the subscription itself, like its layer, inputs and other
            ;;   things that are defined as part of the reg-sub.
            ;; - subscription-match-state collects all the data that we know about the state of specific instances of subscriptions
            ;;   like its reagent id, when it was created, run, disposed, what values it returned, e.t.c.
            subscription-info          (metam/subscription-info (get-in db [:epochs :subscription-info] {}) filtered-traces (get-in db [:app-db :reagent-id]))
            sub-state                  (get-in db [:epochs :sub-state] metam/initial-sub-state)
            subscription-match-state   (metam/subscription-match-state sub-state all-traces new-matches)
            subscription-matches       (rest subscription-match-state)

            new-sub-state              (last subscription-match-state)
            timing                     (mapv (fn [match]
                                               (let [epoch-traces        (into []
                                                                               (comp
                                                                                 (utils/id-between-xf (:id (first match)) (:id (last match))))
                                                                               all-traces)
                                                     ;; TODO: handle case when there are no epoch-traces
                                                     start-of-epoch      (nth epoch-traces 0)
                                                     ;; TODO: optimise trace searching
                                                     event-handler-trace (first (filter metam/event-handler? epoch-traces))
                                                     dofx-trace          (first (filter metam/event-dofx? epoch-traces))
                                                     event-trace         (first (filter metam/event-run? epoch-traces))
                                                     finish-run          (or (first (filter metam/finish-run? epoch-traces))
                                                                             (utils/last-in-vec epoch-traces))]
                                                 {:re-frame/event-run-time     (metam/elapsed-time start-of-epoch finish-run)
                                                  :re-frame/event-time         (:duration event-trace)
                                                  :re-frame/event-handler-time (:duration event-handler-trace)
                                                  :re-frame/event-dofx-time    (:duration dofx-trace)}))
                                             new-matches)

            new-matches                (map (fn [match sub-match t] {:match-info match
                                                                     :sub-state  sub-match
                                                                     :timing     t})
                                            new-matches subscription-matches timing)
            ;; If there are new matches found, then by definition, a quiescent trace must have been received
            ;; However in cases where we reset the db in a replay, we won't get an event match.
            ;; We short circuit here to avoid iterating over the traces when it's unnecessary.
            quiescent?                 (or (seq new-matches)
                                           (filter metam/quiescent? filtered-traces))
            all-matches                (reduce conj previous-matches new-matches)
            retained-matches           (into [] (take-last number-of-epochs-to-retain all-matches))
            first-id-to-retain         (first-match-id (first retained-matches))
            retained-traces            (into [] (comp (drop-while #(< (:id %) first-id-to-retain))
                                                      (remove (fn [trace]
                                                                (or (when drop-reagent (metam/low-level-reagent-trace? trace))
                                                                    (when drop-re-frame (metam/low-level-re-frame-trace? trace)))))) all-traces)]
        {:db       (-> db
                       (assoc-in [:traces :all-traces] retained-traces)
                       (update :epochs (fn [epochs]
                                         (let [current-index (:current-epoch-index epochs)
                                               current-id    (:current-epoch-id epochs)]
                                           (assoc epochs
                                             :matches retained-matches
                                             :matches-by-id (into {} (map (juxt first-match-id identity)) retained-matches)
                                             :match-ids (mapv first-match-id retained-matches)
                                             :parse-state parse-state
                                             :sub-state new-sub-state
                                             :subscription-info subscription-info
                                             ;; Reset current epoch to the head of the list if we got a new event in.
                                             :current-epoch-id (if (seq new-matches) nil current-id)
                                             :current-epoch-index (if (seq new-matches) nil current-index))))))
         :dispatch (when quiescent? [:epochs/quiescent])})
      ;; Else
      {:db db})))

;; TODO: this code is a bit messy, needs refactoring and cleaning up.
(rf/reg-event-fx
  :epochs/previous-epoch
  [(rf/path [:epochs])]
  (fn [{:keys [db]} _]
    (if-some [current-id (:current-epoch-id db)]
      (let [match-ids         (:match-ids db)
            match-array-index (utils/find-index-in-vec (fn [x] (= current-id x)) match-ids)
            new-id            (nth match-ids (dec match-array-index))]
        {:db       (assoc db :current-epoch-id new-id)
         :dispatch [:snapshot/reset-current-epoch-app-db new-id]})
      (let [new-id (nth (:match-ids db)
                        (- (count (:match-ids db)) 2))]
        {:db       (assoc db :current-epoch-id new-id)
         :dispatch [:snapshot/reset-current-epoch-app-db new-id]}))))

(rf/reg-event-fx
  :epochs/next-epoch
  [(rf/path [:epochs])]
  (fn [{:keys [db]} _]
    (if-some [current-id (:current-epoch-id db)]
      (let [match-ids         (:match-ids db)
            match-array-index (utils/find-index-in-vec (fn [x] (= current-id x)) match-ids)
            new-id            (nth match-ids (inc match-array-index))]
        {:db         (assoc db :current-epoch-id new-id)
         :dispatch   [:snapshot/reset-current-epoch-app-db new-id]})
      (let [new-id (utils/last-in-vec (:match-ids db))]
        {:db         (assoc db :current-epoch-id new-id)
         :dispatch   [:snapshot/reset-current-epoch-app-db new-id]}))))

(rf/reg-event-fx
  :epochs/most-recent-epoch
  [(rf/path [:epochs])]
  (fn [{:keys [db]} _]
    {:db (assoc db :current-epoch-index nil
                   :current-epoch-id nil)
     :dispatch [:snapshot/reset-current-epoch-app-db (utils/last-in-vec (:match-ids db))]}))

(rf/reg-event-db
  :epochs/replay
  [(rf/path [:epochs])]
  (fn [epochs _]
    (let [current-epoch-id (or (get epochs :current-epoch-id)
                               (utils/last-in-vec (get epochs :match-ids)))
          event-trace      (-> (get-in epochs [:matches-by-id current-epoch-id :match-info])
                               (metam/matched-event))
          app-db-before    (metam/app-db-before event-trace)
          event            (get-in event-trace [:tags :event])]
      (reset! re-frame.db/app-db app-db-before)
      ;; Wait for quiescence
      (assoc epochs :replay event))))

(rf/reg-event-db
  :epochs/quiescent
  [(rf/path [:epochs])]
  (fn [db _]
    (if-some [event-to-replay (:replay db)]
      (do (re-frame.core/dispatch event-to-replay)
          (dissoc db :replay))
      db)))

(rf/reg-event-db
  :epochs/reset
  (fn [db]
    (re-frame.trace/reset-tracing!)
    (dissoc db :epochs :traces)))

;;

(rf/reg-event-db
  :subs/ignore-unchanged-l2-subs?
  [(rf/path [:subs :ignore-unchanged-subs?])]
  (fn [_ [_ ignore?]]
    ignore?))

(rf/reg-event-db
  :subs/open-pod?
  [(rf/path [:subs :expansions])]
  (fn [expansions [_ id open?]]
    (assoc-in expansions [id :open?] open?)))

(rf/reg-event-db
  :subs/set-diff-visibility
  [(rf/path [:subs :expansions])]
  (fn [expansions [_ id diff?]]
    (let [open? (if diff?
                  true
                  (get-in expansions [id :open?]))]
      (-> expansions
          (assoc-in [id :diff?] diff?)
          ;; If we turn on diffing then we want to also expand the path
          (assoc-in [id :open?] open?)))))

;;

(rf/reg-event-db
  :code/set-code-visibility
  [(rf/path [:code :code-open?])]
  (fn [code-open? [_ open?-path open?]]
    (assoc-in code-open? open?-path open?)))

(rf/reg-event-db
  :code/hover-form
  [(rf/path [:code :highlighted-form])]
  (fn [form [_ new-form]]
    new-form))

(rf/reg-event-db
  :code/exit-hover-form
  [(rf/path [:code :highlighted-form])]
  (fn [form [_ new-form]]
    (if (= form new-form)
      nil
      new-form)))

(rf/reg-event-db
  :code/set-show-all-code?
  [(rf/path [:code :show-all-code?])]
  (fn [_show-all-code? [_ new-show-all-code?]]
    new-show-all-code?))

(rf/reg-event-db
  :code/repl-msg-state
  [(rf/path [:code :repl-msg-state])]
  (fn [current-state [_ new-state]]
    (if (and (= current-state :running) (= new-state :start)) ;; Toggles between :running and :re-running to guarantee rerenderig when you continuously call this event
      :re-running
      (if (= new-state :start) :running :end))))

;;

(rf/reg-event-db
  :component/set-direction
  [(rf/path [:component])]
  (fn [component [_ new-direction]]
    (assoc component :direction new-direction)))
