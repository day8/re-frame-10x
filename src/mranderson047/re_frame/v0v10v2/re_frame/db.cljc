(ns mranderson047.re-frame.v0v10v2.re-frame.db
  (:require [mranderson047.re-frame.v0v10v2.re-frame.interop :refer [ratom]]))


;; -- Application State  --------------------------------------------------------------------------
;;
;; Should not be accessed directly by application code.
;; Read access goes through subscriptions.
;; Updates via event handlers.
(def app-db (ratom {}))

