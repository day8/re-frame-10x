(ns day8.re-frame-10x.events
  (:require
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.inlined-deps.reagent.v1v0v0.reagent.core :as r]
    [day8.re-frame-10x.inlined-deps.reagent.v1v0v0.reagent.dom :as rdom]
    [day8.re-frame-10x.tools.reader.edn :as reader.edn]
    [day8.re-frame-10x.fx.local-storage :as local-storage]
    [day8.re-frame-10x.panels.traces.events :as traces.events]
    [reagent.impl.batching :as batching]
    [clojure.string :as str]
    [goog.object]
    [goog.string]
    [re-frame.db]
    [re-frame.interop]
    [re-frame.core]
    [re-frame.trace]
    [day8.re-frame-10x.navigation.views :as container]
    [day8.re-frame-10x.styles :as styles]
    [day8.re-frame-10x.tools.metamorphic :as metam]
    [day8.re-frame-10x.navigation.epochs.events :as epochs.events]
    [day8.re-frame-10x.panels.settings.events :as settings.events]
    [day8.re-frame-10x.panels.app-db.events :as app-db.events]
    [day8.re-frame-10x.tools.coll :as tools.coll]))

(rf/reg-event-fx
  ::init
  [(rf/inject-cofx ::local-storage/get {:key "panel-width-ratio" :or 0.35})
   (rf/inject-cofx ::local-storage/get {:key "show-panel" :or true})
   (rf/inject-cofx ::local-storage/get {:key "selected-tab" :or :event})
   (rf/inject-cofx ::local-storage/get {:key "filter-items" :or []})
   (rf/inject-cofx ::local-storage/get {:key "app-db-json-ml-expansions" :or #{}})
   (rf/inject-cofx ::local-storage/get {:key "external-window?" :or false})
   (rf/inject-cofx ::local-storage/get {:key "external-window-dimensions" :or {:width 800 :height 800 :top 0 :left 0}})
   (rf/inject-cofx ::local-storage/get {:key "show-epoch-traces?" :or true})
   (rf/inject-cofx ::local-storage/get {:key "using-trace?" :or true})
   (rf/inject-cofx ::local-storage/get {:key "ignored-events" :or {}})
   (rf/inject-cofx ::local-storage/get {:key "low-level-trace" :or {:reagent true :re-frame true}})
   (rf/inject-cofx ::local-storage/get {:key "filtered-view-trace" :or (let [id1 (random-uuid)
                                                                             id2  (random-uuid)]
                                                                         {id1 {:id id1 :ns-str "re-com.box" :ns 're-com.box :sort 0}
                                                                          id2 {:id id2 :ns-str "re-com.input-text" :ns 're-com.input-text :sort 1}})})
   (rf/inject-cofx ::local-storage/get {:key "retained-epochs" :or 25})
   (rf/inject-cofx ::local-storage/get {:key "app-db-paths" :or {}})
   (rf/inject-cofx ::local-storage/get {:key "app-db-follows-events?" :or true})
   (rf/inject-cofx ::local-storage/get {:key "ambiance" :or :bright})
   (rf/inject-cofx ::local-storage/get {:key "syntax-color-scheme" :or :cljs-devtools})
   (rf/inject-cofx ::local-storage/get {:key "categories" :or #{:event :sub/run :sub/create :sub/dispose}})
   rf/unwrap]
  (fn [{:keys [panel-width-ratio show-panel selected-tab filter-items app-db-json-ml-expansions
               external-window? external-window-dimensions show-epoch-traces? using-trace?
               ignored-events low-level-trace filtered-view-trace retained-epochs app-db-paths
               app-db-follow-events? ambiance syntax-color-scheme categories] :as cofx}
       {:keys [debug?]}]
    {:fx [(when using-trace?
            [:dispatch [:global/enable-tracing]])
          [:dispatch [::settings.events/panel-width% panel-width-ratio]]
          [:dispatch [::settings.events/show-panel? show-panel]]
          [:dispatch [::settings.events/selected-tab selected-tab]]
          [:dispatch [::settings.events/set-ignored-events ignored-events]]
          [:dispatch [::settings.events/set-filtered-view-trace filtered-view-trace]]
          [:dispatch [::settings.events/set-low-level-trace low-level-trace]]
          [:dispatch [::settings.events/set-number-of-retained-epochs retained-epochs]]
          [:dispatch [::settings.events/app-db-follows-events? app-db-follow-events?]]
          [:dispatch [::settings.events/set-ambiance ambiance]]
          [:dispatch [::settings.events/set-syntax-color-scheme syntax-color-scheme]]
          [:dispatch [::settings.events/debug? debug?]]
          ;; Important that window dimensions are set before we open an external window.
          [:dispatch [::settings.events/external-window-dimensions external-window-dimensions]]
          (when external-window?
            [:dispatch [:global/launch-external]])
          [:dispatch [::traces.events/set-queries filter-items]]
          [:dispatch [::traces.events/set-categories categories]]
          [:dispatch [::traces.events/set-filter-by-selected-epoch? show-epoch-traces?]]
          [:dispatch [:app-db/paths (into (sorted-map) app-db-paths)]]
          [:dispatch [::app-db.events/set-json-ml-paths app-db-json-ml-expansions]]
          [:dispatch [:global/add-unload-hook]]
          [:dispatch [::app-db.events/reagent-id]]]}))



;; --- OLD ----

(defn fixed-after
  ;; Waiting on https://github.com/day8/re-frame/issues/447
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

(defn disable-tracing! []
  (re-frame.trace/remove-trace-cb ::cb))

(defn enable-tracing! []
  (re-frame.trace/register-trace-cb ::cb #(rf/dispatch [::epochs.events/receive-new-traces %])))

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
  :settings/user-toggle-panel
  (fn [db _]
    (let [now-showing?    (not (get-in db [:settings :show-panel?]))
          external-panel? (get-in db [:settings :external-window?])
          using-trace?    (or external-panel? now-showing?)]
      (if now-showing?
        (enable-tracing!)
        (when-not external-panel?
          (disable-tracing!)))
      (local-storage/save! "using-trace?" using-trace?)
      (local-storage/save! "show-panel" now-showing?)
      (-> db
          (assoc-in [:settings :using-trace?] using-trace?)
          (assoc-in [:settings :show-panel?] now-showing?)))))




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
                                           (rf/dispatch [::settings.events/external-window-resize {:width width :height height}]))
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
                                         (rf/dispatch [::settings.events/external-window-position {:left screen-left :top screen-top}])
                                         (reset! pos {:left screen-left :top screen-top})))))
        window-position-interval (atom nil)
        unmount                  (fn [_]
                                   (.removeEventListener popup-window "resize" handle-window-resize)
                                   (some-> @window-position-interval js/clearInterval)
                                   nil)]

    (styles/inject-popup-styles! popup-document)
    (goog.object/set popup-window "onunload" #(rf/dispatch [:global/external-closed]))
    (rdom/render
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
                              "</title></head><body style=\"margin: 0px;\"><div id=\"--re-frame-10x--\" class=\"external-window\"></div></body>")]
    ;; We would like to set the windows left and top positions to match the monitor that it was on previously, but Chrome doesn't give us
    ;; control over this, it will only position it within the same display that it was popped out on.
    (if-let [w (js/window.open "about:blank" "re-frame-10x-popout"
                               (str "width=" width ",height=" height ",left=" left ",top=" top
                                    ",resizable=yes,scrollbars=yes,status=no,directories=no,toolbar=no,menubar=no"))]
      (let [d (.-document w)]
        ;; We had to comment out the following unmountComponentAtNode as it causes a React exception we assume
        ;; because React says el is not a root container that it knows about.
        ;; In theory by not freeing up the resources associated with this container (e.g. event handlers) we may be
        ;; creating memory leaks. However with observation of the heap in developer tools we cannot see any significant
        ;; unbounded growth in memory usage.
        ;(when-let [el (.getElementById d "--re-frame-10x--")]
        ;  (r/unmount-component-at-node el)))
        (.open d)
        (.write d new-window-html)
        (goog.object/set w "onload" #(mount w d))
        (.close d)
        true)
      false)))

(rf/reg-event-fx
  :global/launch-external
  (fn [ctx _]
    (if (open-debugger-window (get-in ctx [:db :settings :external-window-dimensions]))
      (do
        (local-storage/save! "external-window?" true)
        {:db             (-> (:db ctx)
                             (assoc-in [:settings :external-window?] true)
                             (dissoc-in [:errors :popup-failed?]))
         :dispatch-later [{:ms 200 :dispatch [::settings.events/show-panel? false]}]})
      {:db       (assoc-in (:db ctx) [:errors :popup-failed?] true)
       :dispatch [:global/external-closed]})))

(rf/reg-event-fx
  :global/external-closed
  (fn [ctx _]
    (local-storage/save! "external-window?" false)
    {:db             (assoc-in (:db ctx) [:settings :external-window?] false)
     :dispatch-later [{:ms 400 :dispatch [::settings.events/show-panel? true]}]}))

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

;; App DB

(def app-db-path-mw
  [(rf/path [:app-db :paths]) (fixed-after #(local-storage/save! "app-db-paths" %))])





(rf/reg-event-db
  :app-db/update-path
  app-db-path-mw
  (fn [paths [_ path-id path-str]]
    (let [path  (reader.edn/read-string-maybe path-str)
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
        (local-storage/save! "app-db-paths" (get-in new-db [:app-db :paths]))
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
          (do (local-storage/save! "app-db-paths" (cons path (get-in db [:app-db :paths])))
              (rf/dispatch [::app-db.events/toggle-expansion [path]])
              (-> db
                  (update-in [:app-db :paths] #(cons path %))
                  (assoc-in [:app-db :search-string] "")))
          db))))


(rf/reg-event-db
  :snapshot/reset-current-epoch-app-db
  (fn [db [_ new-id]]
    (when (get-in db [:settings :app-db-follows-events?])
      (let [epochs   (:epochs db)
            match-id (or new-id
                         ;; new-id may be nil when we call this event from :settings/play
                         (tools.coll/last-in-vec (get epochs :match-ids)))
            match    (get-in epochs [:matches-by-id match-id])
            event    (metam/matched-event (:match-info match))]
        ;; Don't mess up the users app if there is a problem getting app-db-after.
        (when-some [new-db (metam/app-db-after event)]
          (reset! re-frame.db/app-db new-db))))
    db))

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

(rf/reg-event-db
  :subs/set-pinned
  [(rf/path [:subs :pinned])]
  (fn [pinned [_ id pinned?]]
    (assoc-in pinned [id :pin?] pinned?)))

(rf/reg-event-db
  :subs/set-filter
  [(rf/path [:subs :filter-str])]
  (fn [_ [_ filter-value]]
    filter-value))

;;

(rf/reg-event-db
  :errors/dismiss-popup-failed
  [(rf/path [:errors])]
  (fn [errors _]
    (dissoc errors :popup-failed?)))
