(ns day8.re-frame-10x.inlined-deps.spade.git-sha-5197e54.runtime
  (:require [clojure.string :as str]
            [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.core :as garden]
            [day8.re-frame-10x.inlined-deps.garden.v1v3v10.garden.types :refer [->CSSFunction]]
            [day8.re-frame-10x.inlined-deps.spade.git-sha-5197e54.container :as sc]
            [day8.re-frame-10x.inlined-deps.spade.git-sha-5197e54.runtime.defaults :as defaults]))

(defonce ^:dynamic *css-compile-flags*
  {:pretty-print? #? (:cljs goog.DEBUG
                      :clj false)
   :always-compile-css? #? (:cljs goog.DEBUG
                            :clj false)})

(defonce ^:dynamic *style-container* (defaults/create-container))

(defn ->css-var [n]
  (->CSSFunction "var" n))

(defn compile-css [elements]
  (garden/css *css-compile-flags* elements))

(defn- compose-names [{style-name :name composed :composes}]
  (if-not composed
    style-name

    (->> (if (sequential? composed)
           (conj composed style-name)
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

(defn ensure-style! [mode metadata name-factory style-factory params]
  (let [style-name (name-factory params)
        always-compile? (or (:always-compile-css metadata)
                            (:always-compile-css? *css-compile-flags*))

        ; NOTE: If we've been instructed to always compile css, then always
        ; assume it's unmounted. The container can update a mounted style
        mounted-info (when-not always-compile?
                       (sc/mounted-info *style-container* style-name))

        {css :css :as info} (or
                              mounted-info

                              ; Not mounted *or* we always want to compile
                              (style-factory style-name params))]

    (when-not mounted-info
      (sc/mount-style! *style-container* style-name css info))

    (case mode
      :attrs {:class (compose-names info)}
      (:class :keyframes) (compose-names info)
      :global css)))
