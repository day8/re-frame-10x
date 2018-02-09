(ns day8.re-frame.trace.events
  (:require [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [mranderson047.reagent.v0v7v0.reagent.core :as r]
            [cljs.tools.reader.edn]
            [day8.re-frame.trace.utils.utils :as utils :refer [spy]]
            [day8.re-frame.trace.utils.localstorage :as localstorage]
            [clojure.string :as str]
            [goog.object]
            [re-frame.db]
            [re-frame.interop]
            [day8.re-frame.trace.view.container :as container]
            [day8.re-frame.trace.styles :as styles]
            [clojure.set :as set]
            [day8.re-frame.trace.metamorphic :as metam]
            [re-frame.trace]))

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
               (f db event)    ;; call f for side effects
               context))))     ;; context is unchanged

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
  :settings/pause
  (fn [db _]
    (assoc-in db [:settings :paused?] true)))

(rf/reg-event-db
  :settings/play
  (fn [db _]
    (-> db
        (assoc-in [:settings :paused?] false)
        (assoc-in [:epochs :current-epoch-index] nil)
        (assoc-in [:epochs :current-epoch-id] nil))))

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
                            [container/devtools-inner {:panel-type :popup}])})]
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
  :snapshot/load-snapshot
  (fn [db [_ new-db]]
    (reset! re-frame.db/app-db new-db)
    db))

;;;

(defn first-match-id
  [m]
  (-> m :match-info first :id))

(rf/reg-event-db
  :epochs/receive-new-traces
  (fn [db [_ new-traces]]
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
                                               ;; TODO: this isn't quite correct, shouldn't be using filtered-traces here
                                               (let [epoch-traces   (into []
                                                                          (comp
                                                                            (utils/id-between-xf (:id (first match)) (:id (last match))))
                                                                          filtered-traces)
                                                     start-of-epoch (nth epoch-traces 0)
                                                     finish-run     (or (first (filter metam/finish-run? epoch-traces))
                                                                        (utils/last-in-vec epoch-traces))]
                                                 {:re-frame/event-time (metam/elapsed-time start-of-epoch finish-run)}))
                                             new-matches)

            new-matches                (map (fn [match sub-match t] {:match-info match
                                                                     :sub-state  sub-match
                                                                     :timing     t})
                                            new-matches subscription-matches timing)
            all-matches                (reduce conj previous-matches new-matches)
            retained-matches           (into [] (take-last number-of-epochs-to-retain all-matches))
            first-id-to-retain         (first-match-id (first retained-matches))
            retained-traces            (into [] (comp (drop-while #(< (:id %) first-id-to-retain))
                                                      (remove (fn [trace]
                                                                (or (when drop-reagent (metam/low-level-reagent-trace? trace))
                                                                    (when drop-re-frame (metam/low-level-re-frame-trace? trace)))))) all-traces)]
        (-> db
            (assoc-in [:traces :all-traces] retained-traces)
            (update :epochs (fn [epochs]
                              (assoc epochs
                                :matches retained-matches
                                :matches-by-id (into {} (map (juxt first-match-id identity)) retained-matches)
                                :match-ids (mapv first-match-id retained-matches)
                                :parse-state parse-state
                                :sub-state new-sub-state
                                :subscription-info subscription-info)))))
      ;; Else
      db)))

(rf/reg-event-fx
  :epochs/previous-epoch
  [(rf/path [:epochs])]
  (fn [{:keys [db]} _]
    (if-some [current-id (:current-epoch-id db)]
      (let [match-ids         (:match-ids db)
            match-array-index (utils/find-index-in-vec (fn [x] (= current-id x)) match-ids)
            new-id            (nth match-ids (dec match-array-index))]
        {:db       (assoc db :current-epoch-id new-id)
         :dispatch [:settings/pause]})
      {:db       (assoc db :current-epoch-id (nth (:match-ids db) (- (count (:match-ids db)) 2)))
       :dispatch [:settings/pause]})))

(rf/reg-event-fx
  :epochs/next-epoch
  [(rf/path [:epochs])]
  (fn [{:keys [db]} _]
    (if-some [current-id (:current-epoch-id db)]
      (let [match-ids         (:match-ids db)
            match-array-index (utils/find-index-in-vec (fn [x] (= current-id x)) match-ids)
            new-id            (nth match-ids (inc match-array-index))]
        {:db       (assoc db :current-epoch-id new-id)
         :dispatch [:settings/pause]})
      {:db       (assoc db :current-epoch-id (last (:match-ids db)))
       :dispatch [:settings/pause]})))

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
  :subs/diff-pod?
  [(rf/path [:subs :expansions])]
  (fn [expansions [_ id diff?]]
    (assoc-in expansions [id :diff?] diff?)))
