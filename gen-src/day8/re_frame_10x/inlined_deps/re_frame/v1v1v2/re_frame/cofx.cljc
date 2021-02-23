(ns ^{:mranderson/inlined true} day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.cofx
  (:require
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.db           :refer [app-db]]
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.interceptor  :refer [->interceptor]]
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.registrar    :refer [get-handler register-handler]]
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.loggers      :refer [console]]))


;; -- Registration ------------------------------------------------------------

(def kind :cofx)
(assert (day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.registrar/kinds kind))

(defn reg-cofx
  [id handler]
  (register-handler kind id handler))


;; -- Interceptor -------------------------------------------------------------

(defn inject-cofx
  ([id]
   (->interceptor
     :id      :coeffects
     :before  (fn coeffects-before
                [context]
                (if-let [handler (get-handler kind id)]
                  (update context :coeffects handler)
                  (console :error "No cofx handler registered for" id)))))
  ([id value]
   (->interceptor
     :id     :coeffects
     :before  (fn coeffects-before
                [context]
                (if-let [handler (get-handler kind id)]
                  (update context :coeffects handler value)
                  (console :error "No cofx handler registered for" id))))))


;; -- Builtin CoEffects Handlers  ---------------------------------------------

;; :db
;;
;; Adds to coeffects the value in `app-db`, under the key `:db`
(reg-cofx
  :db
  (fn db-coeffects-handler
    [coeffects]
    (assoc coeffects :db @app-db)))


;; Because this interceptor is used so much, we reify it
(def inject-db (inject-cofx :db))


