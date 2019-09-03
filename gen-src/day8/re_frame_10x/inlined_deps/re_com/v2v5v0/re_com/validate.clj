(ns day8.re-frame-10x.inlined-deps.re-com.v2v5v0.re-com.validate)

(defmacro validate-args-macro
  "if goog.DEBUG is true then validate the args, otherwise replace the validation code with true
  for production builds which the {:pre ...} will be happy with"
  [args-desc args component-name]
  `(if-not ~(vary-meta 'js/goog.DEBUG assoc :tag 'boolean)
     true
     (day8.re-frame-10x.inlined-deps.re-com.v2v5v0.re-com.validate/validate-args (day8.re-frame-10x.inlined-deps.re-com.v2v5v0.re-com.validate/extract-arg-data ~args-desc) ~args ~component-name)))
