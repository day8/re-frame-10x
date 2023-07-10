(ns day8.re-frame-10x.panels.event.subs
  (:require
   [zprint.core                                                  :as zp]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
   [day8.re-frame-10x.panels.settings.subs                              :as settings.subs]
   [day8.re-frame-10x.panels.traces.subs                                :as traces.subs]))

(rf/reg-sub
 ::root
 (fn [{:keys [code]} _]
   code))

(rf/reg-sub
 ::code-for-epoch
 :<- [::traces.subs/filtered-by-epoch]
 (fn [traces _]
   (->> traces
        (keep-indexed
         (fn [i trace]
           (when-some [code (get-in trace [:tags :code])]
             {:id       i
              :trace-id (:id trace)
              :title    (pr-str (:op-type trace))
              :code     (->> code (map-indexed (fn [i code] (assoc code :id i))) vec) ;; Add index
              :form     (get-in trace [:tags :form])})))
        (first)))) ;; Ignore multiple code executions for now

(rf/reg-sub
 ::code-for-epoch-exists?
 :<- [::code-for-epoch]
 (fn [code _]
   (boolean code)))

(rf/reg-sub
 ::fragments-for-epoch
 :<- [::code-for-epoch]
 :<- [::execution-order?]
 (fn [[{:keys [code]} execution-order?] _]
   (let [unordered-fragments (remove (fn [line] (fn? (:result line))) code)]
     (if execution-order?
       unordered-fragments
       (sort-by :syntax-order unordered-fragments)))))

(rf/reg-sub
 ::trace-id-for-epoch
 :<- [::code-for-epoch]
 (fn [{:keys [trace-id]} _]
   trace-id))

(rf/reg-sub
 ::form-for-epoch
 :<- [::code-for-epoch]
 (fn [{:keys [form]} _]
   form))

(rf/reg-sub
 ::zprint-form-for-epoch
 :<- [::form-for-epoch]
 (fn [form _]
   (zp/zprint-str form)))

(rf/reg-sub
 ::execution-order?
 :<- [::root]
 (fn [code _]
   (get code :execution-order? true)))

(rf/reg-sub
 ::code-open?
 :<- [::root]
 (fn [{:keys [code-open?]} _]
   code-open?))

(rf/reg-sub
 ::highlighted-form
 :<- [::root]
 (fn [{:keys [highlighted-form]} _]
   highlighted-form))

(rf/reg-sub
 ::show-all-code?
 :<- [::root]
 (fn [{:keys [show-all-code?]} _]
   show-all-code?))

(rf/reg-sub
 ::repl-msg-state
 :<- [::root]
 (fn [{:keys [repl-msg-state]} _]
   repl-msg-state))

;; [IJ] TODO: This should not be a subscription:
(def canvas (js/document.createElement "canvas"))

(rf/reg-sub
 ::single-character-width
 (fn [_ _]
   (let [context (.getContext canvas "2d")]
     (set! (.-font context) "monospace 1em")
     (.-width (.measureText context "T")))))

(rf/reg-sub
 ::max-column-width
 :<- [::settings.subs/window-width-rounded 100]
 :<- [::single-character-width]
  ;; It seems like it would be possible to do something smarter responding to panel sizing,
  ;; but that introduces a lot of jank, so we just set to maximum possible window width.
 (fn [[window-width char-width] _]
   (Math/ceil (/ window-width
                 char-width))))