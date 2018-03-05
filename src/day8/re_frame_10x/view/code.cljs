(ns day8.re-frame-10x.view.code
  (:require [day8.re-frame-10x.utils.re-com :as rc]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [day8.re-frame-10x.common-styles :as common]
            [day8.re-frame-10x.view.components :as components]
            [zprint.core :as zp]))

(defn render []
  (let [code-traces @(rf/subscribe [:code/current-code])]
    [rc/v-box
     :style {:padding-bottom common/gs-31s}
     :children
     [[:h1 "Code"]
      (for [code-execution code-traces]
        ^{:key (:id code-execution)}
  [rc/v-box
   :children
         [[:h2 (:title code-execution)]
          [:pre (zp/zprint-str (:form code-execution))]

          [:br]

          ;[:pre "(->> db\n   (update))"]
          (map-indexed
            (fn [i line]
              ;(js/console.log (:form line) (str (:form line)))

              ^{:key i}
              [rc/v-box
               :children [[:pre (zp/zprint-str (:form line))]
                          ;; TODO: disable history expansion, or at least storing it in ls.
                          [components/simple-render (:result line) [(:id code-execution) i]]
                          [:br]]])
            (:code code-execution))]])]]))
