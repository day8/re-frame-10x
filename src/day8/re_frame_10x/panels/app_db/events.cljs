(ns day8.re-frame-10x.panels.app-db.events
  (:require
   [re-frame.db]
   [re-frame.interop]
   [re-frame.core]
   [clojure.string                                               :as string]
   [zprint.core                                                  :as zp]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core :as rf]
   [day8.re-frame-10x.fx.window                                  :refer [popout-window]]
   [day8.re-frame-10x.fx.local-storage                           :as local-storage]
   [day8.re-frame-10x.tools.reader.edn                           :as reader.edn]))

(def paths-interceptors
  [(rf/path [:app-db :paths])
   rf/trim-v
   (local-storage/save "app-db-paths")])

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
 ::create-path
 paths-interceptors
 (fn [paths [open-new-inspectors?]]
   (assoc paths
          (js/Date.now)
          {:diff?       false
           :open?       open-new-inspectors?
           :path        nil
           :path-str    ""
           :valid-path? true})))

(rf/reg-event-fx
 ::create-path-and-skip-to
 paths-interceptors
 (fn [{:keys [db]} [skip-to-path open-new-inspectors?]]
   (let [path-id (js/Date.now)]
     {:db       (assoc db
                       path-id
                       {:diff?       false
                        :open?       open-new-inspectors?
                        :path        nil
                        :path-str    ""
                        :valid-path? true})
      :dispatch [::update-path {:id path-id :path-str skip-to-path}]})))

(rf/reg-event-db
 ::update-path
 paths-interceptors
 (fn [paths [{:keys [id path-str]}]]
   (let [path  (reader.edn/read-string-maybe path-str)
         valid? (or (and (some? path) (sequential? path))
                    (string/blank? path-str))]
     (-> paths
         (assoc-in [id :path-str] path-str)
         (assoc-in [id :valid-path?] valid?)
         (cond-> valid? (assoc-in [id :path] path))))))

(rf/reg-event-db
 ::update-path-blur
 paths-interceptors
 (fn [paths [path-id]]
   (let [{:keys [valid-path? path]} (get paths path-id)]
     (if valid-path?
       paths
       (-> (assoc-in paths [path-id :path-str] (pr-str path))
           (assoc-in [path-id :valid-path?] true))))))

(rf/reg-event-db
 ::set-path-visibility
 paths-interceptors
 (fn [paths [path-id open?]]
   (assoc-in paths [path-id :open?] open?)))

(rf/reg-event-db
 ::set-diff-visibility
 paths-interceptors
 (fn [paths [path-id diff?]]
   (let [open? (if diff?
                 true
                 (get-in paths [path-id :open?]))]
     (-> paths
         (assoc-in [path-id :diff?] diff?)
          ;; If we turn on diffing then we want to also expand the path
         (assoc-in [path-id :open?] open?)))))

(rf/reg-event-db
 ::remove-path
 paths-interceptors
 (fn [paths [path-id]]
   (dissoc paths path-id)))

(rf/reg-event-db
 ::paths
 paths-interceptors
 (fn [_ [paths]]
   paths))

;; [IJ] TODO: This doesn't appear to be used anywhere:
(rf/reg-event-db
 ::set-search-string
 [(rf/path [:app-db :search-string]) rf/trim-v]
 (fn [_ [search-string]]
   search-string))

(rf/reg-event-db
 ::set-json-ml-paths
 [(rf/path [:app-db :json-ml-expansions]) rf/trim-v (local-storage/save "app-db-json-ml-expansions")]
 (fn [_ [paths]]
   paths))

(rf/reg-event-db
 ::toggle-expansion
 [(rf/path [:app-db :json-ml-expansions]) rf/trim-v (local-storage/save "app-db-json-ml-expansions")]
 (fn [paths [path]]
   (if (contains? paths path)
     (disj paths path)
     (conj paths path))))

(rf/reg-event-db
 ::reagent-id
 [(rf/path [:app-db :reagent-id])]
 (fn [_ _]
   (re-frame.interop/reagent-id re-frame.db/app-db)))

(rf/reg-event-db
 ::set-sort-form?
 paths-interceptors
 (fn [paths [path-id sort]]
   (-> paths
       (assoc-in [path-id :sort?] sort))))

(rf/reg-event-db
 ::set-data-path-annotations?
 [(rf/path [:app-db :data-path-annotations?]) rf/trim-v (local-storage/save "data-path-annotations?")]
 (fn [_ [data-path-annotations?]]
   data-path-annotations?))

(rf/reg-event-db
 ::expand
 paths-interceptors
 (fn [paths [id value]]
   (let [{:keys [expand?]} (get paths id)]
     (assoc-in paths [id :expand?] (if-not (nil? value)
                                     value
                                     (not expand?))))))

(rf/reg-event-db
 ::start-edit
 paths-interceptors
 (fn [paths [id]]
   (assoc-in paths [id :editing?] true)))

(rf/reg-event-db
 ::finish-edit
 paths-interceptors
 (fn [paths [id]]
   (assoc-in paths [id :editing?] false)))

(rf/reg-event-db
 ::set-edit-str
 paths-interceptors
 (fn [paths [{:keys [id value refresh?]}]]
   (cond-> paths
     :do (update id assoc :edit-str (zp/zprint-str value {:parse-string? true}))
     refresh? (update-in [id :editor-key] inc))))

(rf/reg-sub
 ::editor-key
 (fn [db [_ id]]
   (get-in db [:app-db :paths id :editor-key])))

(re-frame.core/reg-event-db
 ::edit
 (fn [db [_ path s]]
   (let [new-data (reader.edn/read-string-maybe s)]
     (if-not (seq path)
       new-data
       (assoc-in db path new-data)))))

(defn read-file [file callback]
  (let [file-reader (js/FileReader.)]
    (set! (.-onload ^js file-reader)
          #(callback (.-result (.-target %))))
    (.readAsText ^js file-reader file)))

(rf/reg-fx
 :read-file
 (fn [{:keys [file] [id m] :on-read}]
   (read-file file #(rf/dispatch [id (assoc m :value %)]))))

(rf/reg-event-fx
 ::open-file
 (fn [_ [_ file on-read]]
   {:read-file {:file file
                :on-read on-read}}))

(rf/reg-fx
 ::save-to-file
 (fn [filename contents]
   (let [blob (js/Blob. #js [contents] #js {:type "text/plain"})
         url (str (.createObjectURL js/URL blob))
         link (js/document.createElement "a")]
     (set! (.-href link) url)
     (set! (.-download link) filename)
     (.appendChild js/document.body link)
     (.click link)
     (.remove link))))

(rf/reg-event-fx
 ::save-to-file
 (fn [_ [_ s]]
   {::save-to-file s}))

(rf/reg-cofx
 ::window-bounds
 (fn [cofx]
   (let [rect (-> (or (some-> @popout-window .-document) js/document)
                  (.getElementById "--re-frame-10x--")
                  .-shadowRoot
                  (.getElementById "re-frame-10x__ui-container")
                  .getBoundingClientRect)]
     (into cofx {::window-bounds [(.-x rect) (.-y rect)]}))))

(rf/reg-event-fx
 ::open-popup-menu
 [(rf/inject-cofx ::window-bounds)]
 (fn [{:keys [db] [wx wy] ::window-bounds}
      [_ {:keys [path data data-path] [mx my] :mouse-position}]]
   {:db (assoc db :popup-menu {:showing? true
                               :position [(- mx wx) (- my wy)]
                               :path     path
                               :data     data
                               :data-path data-path})}))

(rf/reg-event-db
 ::close-popup-menu
 (fn [db _] (assoc db :popup-menu {:showing? false})))
