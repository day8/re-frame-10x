(ns day8.re-frame-10x.inlined-deps.spade.git-sha-5197e54.container.alternate
  "The AlternateStyleContainer may be used when a preferred container
   is not always available."
  (:require [day8.re-frame-10x.inlined-deps.spade.git-sha-5197e54.container :as sc :refer [IStyleContainer]]))

(deftype AlternateStyleContainer [get-preferred fallback]
  IStyleContainer
  (mounted-info
    [_ style-name]
    (or (when-let [preferred (get-preferred)]
          (sc/mounted-info preferred style-name))
        (sc/mounted-info fallback style-name)))
  (mount-style!
    [_ style-name css info]
    (or (when-let [preferred (get-preferred)]
          (sc/mount-style! preferred style-name css info))
        (sc/mount-style! fallback style-name css info))))
