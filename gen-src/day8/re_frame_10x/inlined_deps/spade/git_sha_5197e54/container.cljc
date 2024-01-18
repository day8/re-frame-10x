(ns day8.re-frame-10x.inlined-deps.spade.git-sha-5197e54.container)

(defprotocol IStyleContainer
  "The IStyleContainer represents anything that can be used by Spade to
   'mount' styles for access by Spade style components."
  (mounted-info
    [this style-name]
    "Given a style-name, return the info object that was passed when style-name
     was mounted, or nil if that style is not currently mounted.")
  (mount-style!
    [this style-name css info]
    "Ensure the style with the given name and CSS is available. [info]
     should be stored somewhere in-memory to be quickly retrieved
     by a call to [mounted-info]."))
