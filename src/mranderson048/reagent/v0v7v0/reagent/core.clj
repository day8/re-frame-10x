(ns mranderson048.reagent.v0v7v0.reagent.core
  (:require [mranderson048.reagent.v0v7v0.reagent.ratom :as ra]))

(defmacro with-let [bindings & body]
  "Bind variables as with let, except that when used in a component
  the bindings are only evaluated once. Also takes an optional finally
  clause at the end, that is executed when the component is
  destroyed."
  `(ra/with-let ~bindings ~@body))
