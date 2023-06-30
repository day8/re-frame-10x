(ns day8.re-frame-10x.panels.settings.events
  (:require
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
    [day8.re-frame-10x.fx.local-storage                           :as local-storage]
    [day8.re-frame-10x.fx.trace                                   :as trace]
    [day8.re-frame-10x.tools.reader.edn                           :as reader.edn]))

(rf/reg-event-db
  ::panel-width%
  [(rf/path [:settings :panel-width%]) rf/trim-v (local-storage/save "panel-width-ratio")]
  (fn [_ [width%]]
    (max width% 0.05)))

(rf/reg-event-db
  ::window-width
  [(rf/path [:settings :window-width]) rf/trim-v]
  (fn [_ [width]]
    width))

(rf/reg-event-db
  ::selected-tab
  [(rf/path [:settings :selected-tab]) rf/trim-v (local-storage/save "selected-tab")]
  (fn [_ [selected-tab]]
    selected-tab))

(rf/reg-event-db
  ::toggle
  [(rf/path [:settings :showing-settings?])]
  (fn [showing? _]
    (not showing?)))

(rf/reg-event-db
  ::show-panel?
  [(rf/path [:settings :show-panel?]) rf/trim-v (local-storage/save "show-panel")]
  (fn [_ [show-panel?]]
    show-panel?))

(rf/reg-event-db
  ::factory-reset
  (fn [db _]
    ;; [IJ] TODO: these should be fx
    (local-storage/delete-all-keys!)
    (js/location.reload)
    db))

(rf/reg-event-db
  ::set-ambiance
  [(rf/path [:settings :ambiance]) rf/trim-v (local-storage/save "ambiance")]
  (fn [_ [ambiance]]
    ambiance))

(rf/reg-event-db
  ::set-syntax-color-scheme
  [(rf/path [:settings :syntax-color-scheme]) rf/trim-v (local-storage/save "syntax-color-scheme")]
  (fn [_ [syntax-color-scheme]]
    syntax-color-scheme))

(rf/reg-event-db
  ::set-number-of-retained-epochs
  [(rf/path [:settings :number-of-epochs]) rf/trim-v (local-storage/save "retained-epochs")]
  (fn [_ [num-str]]
    ;; TODO: this is not perfect, there is an issue in re-com
    ;; where it won't update its model if it never receives another
    ;; changes after it's on-change is fired.
    ;; TODO: you could reset the stored epochs on change here
    ;; once the way they are processed is refactored.
    (let [num (js/parseInt num-str)
          num (if (and (not (js/isNaN num)) (pos-int? num))
                num
                5)]
      num)))

(def ignored-event-interceptors
  [(rf/path [:settings :ignored-events])
   rf/trim-v
   (local-storage/save "ignored-events")])

(rf/reg-event-db
  ::add-ignored-event
  ignored-event-interceptors
  (fn [ignored-events _]
    (let [id (random-uuid)]
      (assoc ignored-events id {:id id :event-str "" :event-id nil :sort (js/Date.now)}))))

(rf/reg-event-db
  ::remove-ignored-event
  ignored-event-interceptors
  (fn [ignored-events [id]]
    (dissoc ignored-events id)))

(rf/reg-event-db
  ::update-ignored-event
  ignored-event-interceptors
  (fn [ignored-events [id event-str]]
    ;; TODO: this won't inform users if they type bad strings in.
    (let [event (reader.edn/read-string-maybe event-str)]
      (-> ignored-events
          (assoc-in [id :event-str] event-str)
          (update-in [id :event-id] (fn [old-event] (if event event old-event)))))))

(rf/reg-event-db
  ::set-ignored-events
  ignored-event-interceptors
  (fn [_ [ignored-events]]
    ignored-events))

(def filtered-view-trace-interceptors
  [(rf/path [:settings :filtered-view-trace])
   rf/trim-v
   (local-storage/save "filtered-view-trace")])

(rf/reg-event-db
  ::add-filtered-view-trace
  filtered-view-trace-interceptors
  (fn [filtered-view-trace _]
    (let [id (random-uuid)]
      (assoc filtered-view-trace id {:id id :ns-str "" :ns nil :sort (js/Date.now)}))))

(rf/reg-event-db
  ::remove-filtered-view-trace
  filtered-view-trace-interceptors
  (fn [filtered-view-trace [id]]
    (dissoc filtered-view-trace id)))

(rf/reg-event-db
  ::update-filtered-view-trace
  filtered-view-trace-interceptors
  (fn [filtered-view-trace [id ns-str]]
    ;; TODO: this won't inform users if they type bad strings in.
    (let [event (reader.edn/read-string-maybe ns-str)]
      (-> filtered-view-trace
          (assoc-in [id :ns-str] ns-str)
          (update-in [id :ns] (fn [old-event] (if event event old-event)))))))

(rf/reg-event-db
  ::set-filtered-view-trace
  filtered-view-trace-interceptors
  (fn [_ [ignored-events]]
    ignored-events))

(def low-level-trace-interceptors
  [(rf/path [:settings :low-level-trace])
   rf/trim-v
   (local-storage/save "low-level-trace")])

(rf/reg-event-db
  ::set-low-level-trace
  low-level-trace-interceptors
  (fn [_ [low-level]]
    low-level))

(rf/reg-event-db
  ::low-level-trace
  low-level-trace-interceptors
  (fn [low-level [trace-type capture?]]
    (assoc low-level trace-type capture?)))

(rf/reg-event-db
  ::debug?
  [(rf/path [:settings :debug?]) rf/trim-v]
  (fn [_ [debug?]]
    debug?))

(rf/reg-event-db
  ::app-db-follows-events?
  [(rf/path [:settings :app-db-follows-events?]) rf/trim-v (local-storage/save "app-db-follows-events?")]
  (fn [_ [follows-events?]]
    follows-events?))

(rf/reg-event-db
  ::external-window-dimensions
  [(rf/path [:settings :external-window-dimensions]) rf/trim-v (local-storage/save "external-window-dimensions")]
  (fn [_ [external-window-dimensions]]
    external-window-dimensions))

(rf/reg-event-db
  ::external-window-resize
  [(rf/path [:settings :external-window-dimensions]) rf/unwrap (local-storage/save "external-window-dimensions")]
  (fn [external-window-dimensions {:keys [width height]}]
    (assoc external-window-dimensions :width width :height height)))

(rf/reg-event-db
  ::external-window-position
  [(rf/path [:settings :external-window-dimensions]) rf/unwrap (local-storage/save "external-window-dimensions")]
  (fn [external-window-dimensions {:keys [left top]}]
    (assoc external-window-dimensions :left left :top top)))

(rf/reg-event-fx
  ::user-toggle-panel
  [(rf/path [:settings])
   (local-storage/save "using-trace?" :using-trace?)
   (local-storage/save "show-panel" :show-panel?)]
  (fn [{settings :db} _]
    (let [now-showing?    (not (get settings :show-panel?))
          external-panel? (get settings :external-window?)
          using-trace?    (or external-panel? now-showing?)]
      (merge
        {:db (-> settings
                 (assoc :using-trace? using-trace?)
                 (assoc :show-panel? now-showing?))}
        (if now-showing?
          {::trace/enable {:key ::cb}}
          (when-not external-panel?
            {::trace/disable {:key ::cb}}))))))

(rf/reg-event-fx
  ::enable-tracing
  (fn [_ _]
    {::trace/enable {:key ::cb}}))

(rf/reg-event-fx
  ::disable-tracing
  (fn [_ _]
    {::trace/disable {:key ::cb}}))

(rf/reg-event-db
  ::show-event-history?
  [(rf/path [:settings :show-event-history?]) rf/trim-v (local-storage/save "show-event-history")]
  (fn [_ [show-event-history?]]
    show-event-history?))

(rf/reg-event-db
  ::open-new-inspectors?
  [(rf/path [:settings :open-new-inspectors?]) rf/trim-v (local-storage/save "open-new-inspectors?")]
  (fn [_ [open-new-inspectors?]]
    open-new-inspectors?))

(rf/reg-event-db
  ::handle-keys?
  [(rf/path [:settings :handle-keys?]) rf/trim-v (local-storage/save "handle-keys?")]
  (fn [_ [handle-keys?]]
    handle-keys?))

(rf/reg-event-db
 ::ready-to-bind-key
 [(rf/path [:settings :ready-to-bind-key]) rf/trim-v]
 (fn [_ [key-intent]] key-intent))

(rf/reg-event-db
 ::key-bindings
 [(rf/path [:settings :key-bindings]) rf/trim-v (local-storage/save "key-bindings")]
 (fn [_ [key-bindings]] key-bindings))

(rf/reg-event-db
 ::bind-key
 [(rf/path [:settings :key-bindings]) rf/trim-v (local-storage/save "key-bindings")]
 (fn [key-bindings [key-intent value]]
   (assoc key-bindings key-intent value)))

(rf/reg-event-db
 ::log-outputs
 [(rf/path [:settings :log-outputs]) rf/trim-v (local-storage/save "log-outputs")]
 (fn [log-outputs [value & [enabled?]]]
   (if (vector? value)
     value
     (-> (set log-outputs)
         ((if-not (false? enabled?) conj disj) value)
         sort
         vec))))

(rf/reg-event-db
 ::log-pretty?
 [(rf/path [:settings :log-pretty?]) rf/trim-v (local-storage/save "log-pretty?")]
 (fn [_ [pretty?]]
   pretty?))
