(ns day8.re-frame-10x.view.code
  (:require [day8.re-frame-10x.utils.re-com :as rc]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [day8.re-frame-10x.common-styles :as common]
            [day8.re-frame-10x.view.components :as components]
            [zprint.core :as zp]
            [clojure.string :as str]))

(def code-styles
  [:#--re-frame-10x--
   [:.code-panel
    {:padding-bottom common/gs-31}]
   ;; We rely on highlight JS for our background now.
   #_[:.code-listing
    {:background-color common/white-background-color
     :padding          "5px"
     :margin-right     "19px"}]
   [:.code-listing--highlighted
    {:font-weight      "bold"
     :background-color "rgba(100, 100, 255, 0.08)"}]
   [:.code-fragment {:background-color common/white-background-color}
    [:&:hover {:background-color "rgba(100, 100, 255, 0.08)"}]]])

;; Terminology:
;; Form: a single Clojure form (may have nested children)
;; Result: the result of execution of a single form
;; Fragment: the combination of a form and result
;; Listing: a block of traced Clojure code, e.g. an event handler function

(defn render []
  [rc/v-box
   :size "1 1 auto"
   :class "code-panel"
   :children
   [[:h1 "Code moved to Events"]]])
