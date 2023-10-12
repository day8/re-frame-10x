(ns ^{:mranderson/inlined true} day8.re-frame-10x.inlined-deps.reagent.v1v2v0.reagent.impl.protocols)

(defprotocol Compiler
  (get-id [this])
  (parse-tag [this tag-name tag-value])
  (as-element [this x])
  (make-element [this argv component jsprops first-child]))

