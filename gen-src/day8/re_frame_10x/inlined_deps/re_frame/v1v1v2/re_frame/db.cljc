(ns ^{:mranderson/inlined true} day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.db
  (:require [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.interop :refer [ratom]]))


;; -- Application State  --------------------------------------------------------------------------
;;
;; Should not be accessed directly by application code.
;; Read access goes through subscriptions.
;; Updates via event handlers.
(def app-db (ratom {}))

