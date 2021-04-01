(ns ^{:mranderson/inlined true} day8.re-frame-10x.inlined-deps.spade.v1v1v0.spade.runtime
  (:require [clojure.string :as str]
            [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.core :as garden]
            [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.types :refer [->CSSFunction]]))

(defonce
  ^{:private true
    :dynamic true}
  *injected* (atom {}))

(defonce ^:dynamic *css-compile-flags*
  {:pretty-print? goog.DEBUG})

(defn ->css-var [n]
  (->CSSFunction "var" n))

(defn compile-css [elements]
  (garden/css *css-compile-flags* elements))

(defn- perform-update! [obj css]
  (set! (.-innerHTML (:element obj)) css))

(defn update! [id css]
  (swap! *injected* update id
         (fn update-injected-style [obj]
           (when-not (= (:source obj) css)
             (perform-update! obj css))
           (assoc obj :source css))))

(defn inject! [id css]
  (let [head (.-head js/document)
        element (doto (js/document.createElement "style")
                  (.setAttribute "spade-id" (str id)))
        obj {:element element
             :source css
             :id id}]
    (assert (some? head)
            "An head element is required in the dom to inject the style.")

    (.appendChild head element)

    (swap! *injected* assoc id obj)
    (perform-update! obj css)))

(defn- compose-names [{style-name :name composed :composes}]
  (if-not composed
    style-name
    (str/join " "
              (->>
                (if (seq? composed)
                  (into composed style-name)
                  [composed style-name])
                (map (fn [item]
                       (cond
                         (string? item) item

                         ; unpack a defattrs
                         (and (map? item)
                              (string? (:class item)))
                         (:class item)

                         :else
                         (throw (js/Error.
                                  (str "Invalid argument to :composes key:"
                                       item))))))))))

(defn ensure-style! [mode base-style-name factory params]
  (let [{css :css style-name :name :as info} (apply factory base-style-name params params)
        existing (get @*injected* style-name)]

    (if existing
      ; update existing style element
      (update! style-name css)

      ; create a new element
      (inject! style-name css))

    (case mode
      :attrs {:class (compose-names info)}
      (:class :keyframes) (compose-names info)
      :global css)))