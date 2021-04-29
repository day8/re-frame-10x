(ns day8.re-frame-10x.tools.string)

(defn pluralize
  "Return a pluralized phrase, appending an s to the singular form if no plural is provided.
  For example:
     (pluralize 5 \"month\") => \"5 months\"
     (pluralize 1 \"month\") => \"1 month\"
     (pluralize 1 \"radius\" \"radii\") => \"1 radius\"
     (pluralize 9 \"radius\" \"radii\") => \"9 radii\"
     From https://github.com/flatland/useful/blob/194950/src/flatland/useful/string.clj#L25-L33"
  [num singular & [plural]]
  (str num " " (if (= 1 num) singular (or plural (str singular "s")))))

(defn pluralize-
  "Same as pluralize, but doesn't prepend the number to the pluralized string."
  [num singular & [plural]]
  (if (= 1 num)
    singular
    (or plural (str singular "s"))))
