(ns day8.re-frame-10x.inlined-deps.spade.git-sha-93ef290.container)

(defprotocol IStyleContainer
  "The IStyleContainer represents anything that can be used by Spade to
   'mount' styles for access by Spade style components."
  (mount-style!
    [this style-name css]
    "Ensure the style with the given name and CSS is available"))
