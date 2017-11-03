(ns day8.re-frame.trace.styles
  (:require-macros [day8.re-frame.trace.utils.macros :as macros]))

(def panel-styles (macros/slurp-macro "day8/re_frame/trace/main.css"))
