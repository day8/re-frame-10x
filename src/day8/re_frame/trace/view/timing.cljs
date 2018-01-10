(ns day8.re-frame.trace.view.timing
  (:require [clojure.string :as str]
            [devtools.prefs]
            [devtools.formatters.core]
            [day8.re-frame.trace.view.components :as components]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [mranderson047.reagent.v0v6v0.reagent.core :as r]
            [day8.re-frame.trace.utils.re-com :as rc]
            [day8.re-frame.trace.common-styles :as common])
  (:require-macros [day8.re-frame.trace.utils.macros :as macros]))

(defn render []
  [rc/v-box
   :padding "12px 0px"
   :children [[rc/label :label "Timing panel"]]])
