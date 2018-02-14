(ns day8.re-frame.trace.view.code
  (:require [day8.re-frame.trace.utils.re-com :as rc]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]))

(defn render []
  (let [code-traces (rf/subscribe [:epochs/current-code-traces])])
  [rc/v-box
   :children
   [[:h1 "Code"]]])
