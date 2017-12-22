(ns mranderson047.reagent.v0v6v0.reagent.core
  (:require [mranderson047.reagent.v0v6v0.reagent.ratom :as ra]))

(defmacro with-let [bindings & body]
  "Bind variables as with let, except that when used in a component
  the bindings are only evaluated once. Also takes an optional finally
  clause at the end, that is executed when the component is
  destroyed."
  `(ra/with-let ~bindings ~@body))
