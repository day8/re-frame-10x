(ns day8.re-frame-10x.inlined-deps.spade.git-sha-93ef290.runtime
  (:require [clojure.string :as str]
            [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.core :as garden]
            [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.types :refer [->CSSFunction]]
            [day8.re-frame-10x.inlined-deps.spade.git-sha-93ef290.container :as sc]
            [day8.re-frame-10x.inlined-deps.spade.git-sha-93ef290.runtime.defaults :as defaults]))

(defonce ^:dynamic *css-compile-flags*
  {:pretty-print? #? (:cljs goog.DEBUG
                      :clj false)})

(defonce ^:dynamic *style-container* (defaults/create-container))

(defn ->css-var [n]
  (->CSSFunction "var" n))

(defn compile-css [elements]
  (garden/css *css-compile-flags* elements))

(defn- compose-names [{style-name :name composed :composes}]
  (if-not composed
    style-name

    (->> (if (seq? composed)
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
                  (throw (ex-info
                           (str "Invalid argument to :composes key:"
                                item)
                           {:style-name style-name
                            :value item})))))
         (str/join " "))))

(defn ensure-style! [mode base-style-name factory params]
  (let [{css :css style-name :name :as info} (apply factory base-style-name params params)]

    (sc/mount-style! *style-container* style-name css)

    (case mode
      :attrs {:class (compose-names info)}
      (:class :keyframes) (compose-names info)
      :global css)))
