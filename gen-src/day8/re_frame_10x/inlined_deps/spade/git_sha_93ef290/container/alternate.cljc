(ns day8.re-frame-10x.inlined-deps.spade.git-sha-93ef290.container.alternate
  "The AlternateStyleContainer may be used when a preferred container
   is not always available."
  (:require [day8.re-frame-10x.inlined-deps.spade.git-sha-93ef290.container :as sc :refer [IStyleContainer]]))

(deftype AlternateStyleContainer [get-preferred fallback]
  IStyleContainer
  (mount-style!
    [_ style-name css]
    (or (when-let [preferred (get-preferred)]
          (sc/mount-style! preferred style-name css))
        (sc/mount-style! fallback style-name css))))
