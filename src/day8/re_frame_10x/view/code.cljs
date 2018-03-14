(ns day8.re-frame-10x.view.code
  (:require [day8.re-frame-10x.utils.re-com :as rc]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [day8.re-frame-10x.common-styles :as common]
            [day8.re-frame-10x.view.components :as components]
            [zprint.core :as zp]
            [clojure.string :as str]))

(def code-styles
  [:#--re-frame-10x--
   [:.code-panel
    {:padding-bottom common/gs-31}]
   [:.code-listing
    {:background-color common/white-background-color
     :padding          "5px"
     :margin-right     "19px"}
    [:.code-listing--highlighted
     {:font-weight      "bold"
      :background-color "rgba(100, 100, 255, 0.08)"}]]
   [:.code-fragment {:background-color common/white-background-color
                     :padding          "5px"
                     :margin-right     "19px"
                     :cursor           "pointer"}
    [:&:hover {:background-color "rgba(100, 100, 255, 0.08)"}]]])

;; Terminology:
;; Form: a single Clojure form (may have nested children)
;; Result: the result of execution of a single form
;; Fragment: the combination of a form and result
;; Listing: a block of traced Clojure code, e.g. an event handler function

(defn render []
  (let [code-traces      @(rf/subscribe [:code/current-code])
        highlighted-form @(rf/subscribe [:code/highlighted-form])]
    [rc/v-box
     :size "1 1 auto"
     :class "code-panel"
     :children
     [[:h1 "Code"]
      [:pre "Hover " (pr-str highlighted-form) "\n"]
      (doall
        (for [code-execution code-traces]
          ^{:key (:id code-execution)}
          [rc/v-box
           :size "1 1 auto"
           :gap "5px"
           :children
           (let [form       (:form code-execution)
                 form-str   (zp/zprint-str form)
                 search-str highlighted-form
                 start      (str/index-of form-str search-str)
                 length     (if (some? search-str)
                              (count (pr-str search-str))
                              0)
                 before     (subs form-str 0 start)
                 end-index  (+ start length)
                 highlight  (subs form-str start end-index)
                 after      (subs form-str end-index)
                 ]
             [[:h2 (:title code-execution)]
              [:pre.code-listing
               (list ^{:key "before"} before
                     ^{:key "hl"} [:span.code-listing--highlighted highlight]
                     ^{:key "after"} after)]
              [:br]
              [rc/v-box
               :size "1 1 auto"
               :style {:overflow-y "scroll"}
               :children
               (doall
                 (->> (:code code-execution)
                      ;; Remove traced function values, these are usually not very interesting in and of themselves.
                      (remove (fn [line] (fn? (:result line))))
                      (map-indexed
                        (fn [i line]
                          (list
                            ;; See https://github.com/reagent-project/reagent/issues/350 for why we use random-uuid here
                            ^{:key (random-uuid)}
                            [rc/v-box
                             :class "code-fragment"
                             :style {:margin-left (str (* 50 (dec (:indent-level line))) "px")}
                             :attr {:on-mouse-enter #(do (println "Enter" (:form line))
                                                         (rf/dispatch [:code/hover-form (:form line)])
                                                         true)
                                    :on-mouse-leave #(do (println "Leave" (:form line))
                                                         (rf/dispatch [:code/exit-hover-form (:form line)])
                                                         true)}
                             :children [[:pre (zp/zprint-str (:form line))]
                                        ;; TODO: disable history expansion, or at least storing of it in ls.
                                        [components/simple-render (:result line) [(:id code-execution) i]]]]
                            ^{:key (random-uuid)}
                            [rc/gap-f :size "5px"]))
                        )))]])]))]]))
