(ns day8.re-frame.trace.graph
  (:require [clojure.set :as set]))

(defn select-type [type traces]
  (filter #(= type (:op-type %)) traces))

(defn get-reaction [trace]
  (get-in trace [:tags :reaction]))

(defn distinct-k
  "Returns a lazy sequence of the elements of coll with duplicates removed."
  ([key-fn coll]
   (let [step (fn step [xs seen]
                (lazy-seq
                  ((fn [[f :as xs] seen]
                     (when-let [s (seq xs)]
                       (let [f-fn (key-fn f)]
                         (if (contains? seen f-fn)
                           (recur (rest s) seen)
                           (cons f (step (rest s) (conj seen f-fn)))))))
                    xs seen)))]
     (step coll #{}))))

(defn select-links [traces type disposed-ids link-val]
  (->> traces
       (select-type type)
       (remove #(contains? disposed-ids (get-reaction %)))
       (mapcat (fn [trace]
                 (for [input-signal (get-in trace [:tags :input-signals])
                       :let [reaction (get-reaction trace)]
                       :when (every? some? [input-signal reaction])]
                   {:source input-signal :target reaction :value link-val
                    :id     (str input-signal "|" reaction)})))
       (distinct-k :id)))

(defn select-sub-nodes [traces type disposed-ids r]
  (->> traces
       (select-type type)
       (remove #(contains? disposed-ids (get-reaction %)))
       (remove #(get-in % [:tags :cached?]))
       (map (fn [trace]
              {:id    (get-reaction trace)
               :title (str (:operation trace))
               :group 2
               :r     r
               :data  trace}))
       (distinct-k :id)))

(defn select-view-nodes [traces type unmounted-components r]
  (->> traces
       (select-type type)
       (remove #(contains? unmounted-components (get-reaction %)))
       (map (fn [trace]
              {:id    (get-reaction trace)
               :title (str (:operation trace))
               :group 3
               :r     r
               :data  trace
               :fx    350}))
       (remove #(nil? (:id %)))                            ;; remove reactions that are null (mostly from input fields???)
       (distinct-k :id)))

;; Use http://bl.ocks.org/GerHobbelt/3683278 to constrain nodes

(defn trace->sub-graph [traces extra-nodes]
  (let [disposed-ids         (->> (select-type :sub/dispose traces)
                                  (map get-reaction)
                                  set)
        unmounted-components (->> (select-type :componentWillUnmount traces)
                                  (map get-reaction)
                                  set)

        sub-nodes            (select-sub-nodes traces :sub/create disposed-ids 10)
        view-nodes          nil #_ (select-view-nodes traces :render unmounted-components 5)
        sub-links            (select-links traces :sub/run disposed-ids 1)
        view-links        nil #_   (select-links traces :render unmounted-components 0.5)
        all-nodes            (concat extra-nodes sub-nodes view-nodes)
        node-ids             (set (map :id all-nodes))
        nodes-links          (->> (mapcat (fn [{:keys [source target]}] [source target]) view-links) set)
        missing-nodes        (set/difference nodes-links node-ids) ;; These are local ratoms
        view-links           (->> view-links
                                  (remove #(get missing-nodes (:source %))))
        all-links            (concat sub-links view-links)]
    {:nodes all-nodes
     :links all-links}))
