(ns ^{:mranderson/inlined true} day8.re-frame-10x.inlined-deps.reagent.v1v0v0.reagent.impl.protocols)

(defprotocol Compiler
  (get-id [this])
  (as-element [this x])
  (make-element [this argv component jsprops first-child]))

