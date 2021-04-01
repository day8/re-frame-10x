(ns ^{:mranderson/inlined true} day8.re-frame-10x.inlined-deps.spade.v1v1v0.spade.util
  (:require [clojure.string :as str]))

(defn factory->name
  "Given a style factory function, return an appropriate name for its
   style. This function assumes it will be called *once* for any given
   factory; subsequent calls for the same factory *may not* return the
   same value (especially under :simple optimizations)."
  [factory]
  (let [given-name (.-name factory)]
    (if (empty? given-name)
      ; under :simple optimizations, the way the function is declared does
      ; not leave any value for its name. so... generate one!
      (name (gensym "SPD"))

      ; normal case: base the style name on the factory function's name.
      ; this lets us have descriptive names in dev, and concise names in
      ; prod, without having to embed anything extra in the file
      (-> given-name
          (str/replace "_factory$" "")
          (str/replace #"[_$]" "-")
          (str/replace #"^-" "_")))))

(defn sanitize [s]
  (-> s
      str
      (str/replace #"[^A-Za-z0-9-_]" "-")))

(defn params->key [p]
  (try
    (hash p)
    (catch #?(:cljs :default
              :clj Throwable) _
      nil)))

(defn build-style-name [base style-key params]
  (cond
    ; easy case: a key was provided
    style-key (str base "_" (sanitize style-key))

    (seq params) (if-let [pkey (params->key params)]
                   (str base "_" pkey)

                   (let [msg (str "WARNING: no key provided for " base)]
                     #?(:cljs (js/console.warn msg)
                        :clj (throw (Exception. msg)))
                     base))

    ; easiest case: no key is necessary
    :else base))