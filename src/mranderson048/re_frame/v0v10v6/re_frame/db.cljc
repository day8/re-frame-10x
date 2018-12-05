(ns mranderson048.re-frame.v0v10v6.re-frame.db
  (:require [mranderson048.re-frame.v0v10v6.re-frame.interop :refer [ratom]]))


;; -- Application State  --------------------------------------------------------------------------
;;
;; Should not be accessed directly by application code.
;; Read access goes through subscriptions.
;; Updates via event handlers.
(def app-db (ratom {}))

