(ns ^{:mranderson/inlined true} day8.re-frame-10x.inlined-deps.garden.v1v3v3.garden.repl
  "Method definitions for `print-method` with Garden types."
  (:require [day8.re-frame-10x.inlined-deps.garden.v1v3v3.garden.compiler :as compiler]
            [day8.re-frame-10x.inlined-deps.garden.v1v3v3.garden.util :as util]
            [day8.re-frame-10x.inlined-deps.garden.v1v3v3.garden.types]
            [day8.re-frame-10x.inlined-deps.garden.v1v3v3.garden.color]
            [day8.re-frame-10x.inlined-deps.garden.v1v3v3.garden.selectors :as selectors])
  (:import (day8.re_frame_10x.inlined_deps.garden.v1v3v3.garden.types CSSUnit
                         CSSFunction
                         CSSAtRule)
           (day8.re_frame_10x.inlined_deps.garden.v1v3v3.garden.color CSSColor)
           (day8.re_frame_10x.inlined_deps.garden.v1v3v3.garden.selectors CSSSelector)))

(defmethod print-method CSSUnit [css-unit writer]
  (.write writer (compiler/render-css css-unit)))

(defmethod print-method CSSFunction [css-function writer]
  (.write writer (compiler/render-css css-function)))

(defmethod print-method CSSColor [color writer]
  (.write writer (compiler/render-css color)))

(defmethod print-method CSSAtRule [css-at-rule writer]
  (let [f (if (or (util/at-keyframes? css-at-rule)
                  (util/at-media? css-at-rule))
            compiler/compile-css
            compiler/render-css)]
    (.write writer (f css-at-rule))))

(defmethod print-method CSSSelector [css-selector writer]
  (.write writer (selectors/css-selector css-selector)))
