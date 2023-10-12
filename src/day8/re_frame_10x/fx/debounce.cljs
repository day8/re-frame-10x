(ns day8.re-frame-10x.fx.debounce
  (:require
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core :as rf]))

(defn now [] (.getTime (js/Date.)))

(def registered-keys (atom nil))

(defn dispatch-if-not-superceded [{:keys [key event time-received]}]
  (when (= time-received (get @registered-keys key))
    ;; no new events on this key!
    (rf/dispatch event)))

(defn dispatch-later [{:keys [delay] :as debounce}]
  (js/setTimeout
   (fn [] (dispatch-if-not-superceded debounce))
   delay))

(rf/reg-fx
 ::dispatch
 (fn dispatch-debounce [{:keys [key event delay] :as debounce}]
   (when (not (and (keyword? key) (vector? event) (integer? delay)))
     (rf/console :error "re-frame-10x ::debounce/dispatch invalid argument"))
   (let [ts (now)]
     (swap! registered-keys assoc (:key debounce) ts)
     (dispatch-later (assoc debounce :time-received ts)))))
