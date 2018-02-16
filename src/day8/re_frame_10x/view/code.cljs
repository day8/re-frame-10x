(ns day8.re-frame-10x.view.code
  (:require [day8.re-frame-10x.utils.re-com :as rc]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [day8.re-frame-10x.common-styles :as common]
            [clojure.string :as str]
            [day8.re-frame-10x.view.components :as components]))

(defn render []
  (let [code-traces @(rf/subscribe [:code/current-code])]
    [rc/v-box
     :style {:padding-bottom common/gs-31s}
     :children
     [[:h1 "Code"]
      (for [section code-traces]
        ^{:key (:id section)}
  [rc/v-box
   :children
         [[:h2 (:title section)]

          (map-indexed
            (fn [i line]
              ^{:key i}
              [rc/v-box
               :children [[:pre (str (:form line))]
                          ;; TODO: disable history expansion, or at least storing it in ls.
                          [components/simple-render (:result line) [(:id section) i]]
                          [:br]]])
            (:code section))]])]]))
