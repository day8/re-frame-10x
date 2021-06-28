(ns day8.re-frame-10x.inlined-deps.spade.git-sha-93ef290.container.atom
  "The AtomStyleContainer renders styles into an atom it is provided with."
  (:require [day8.re-frame-10x.inlined-deps.spade.git-sha-93ef290.container :refer [IStyleContainer]]))

(deftype AtomStyleContainer [styles-atom]
  IStyleContainer
  (mount-style! [_ style-name css]
    (swap! styles-atom assoc style-name css)))

