(ns day8.re-frame-10x.view.event
  (:require [day8.re-frame-10x.utils.re-com :as rc]
            [day8.re-frame-10x.view.components :as components]
            [day8.re-frame-10x.common-styles :as common]
            [mranderson047.garden.v1v3v3.garden.units :as units]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [zprint.core :as zp]
            [clojure.string :as str])
  (:require-macros [day8.re-frame-10x.utils.macros :refer [with-cljs-devtools-prefs]]))

(def pod-border-color "#daddde")
(def pod-border-edge (str "1px solid " pod-border-color))
(def border-radius "3px")

(def event-styles
  [:#--re-frame-10x--
   [:.event-panel
    {:padding "39px 19px 0px 0px"}]
   [:.event-section]
   [:.event-section--header
    {:background-color common/navbar-tint-lighter
     :color            common/navbar-text-color
     :height           common/gs-19
     :font-size        "14px"
     :padding          [[0 common/gs-12]]
     }]
   [:.event-section--data
    {:background-color "rgba(100, 255, 100, 0.08)"
     :padding-left     (units/px- common/gs-12 common/expansion-button-horizontal-padding)
     :overflow-x       "auto"}]
   ])

(defn event-section [title data]
  [rc/v-box
   :class "event-section"
   :children
   [[rc/h-box
     :class "event-section--header app-db-path--header"
     :align :center
     :children [[:h2 title]]]

    [components/simple-render data [title] "event-section--data app-db-path--pod-border"]]])

;; Terminology:
;; Form: a single Clojure form (may have nested children)
;; Result: the result of execution of a single form
;; Fragment: the combination of a form and result
;; Listing: a block of traced Clojure code, e.g. an event handler function

(defn event-code []
  (let [code-traces      @(rf/subscribe [:code/current-code])
        code-traces      (list
                           {:title ":event/handler",
                            :id    1,
                            :code  [{:form         'dissoc
                                     :result       dissoc
                                     :indent-level 1}
                                    {:form         'todos
                                     :result       {3 {:id 3, :title "abc", :done false},
                                                    4 {:id 4, :title "abc", :done true},
                                                    5 {:id 5, :title "def", :done true},
                                                    6 {:id 6, :title "abc", :done false},
                                                    7 {:id 7, :title "add", :done false}}
                                     :indent-level 1}
                                    {:form         'todos
                                     :result       {3 {:id 3, :title "abc", :done false},
                                                    4 {:id 4, :title "abc", :done true},
                                                    5 {:id 5, :title "def", :done true},
                                                    6 {:id 6, :title "abc", :done false},
                                                    7 {:id 7, :title "add", :done false}}
                                     :indent-level 4}
                                    {:form   '(vals todos)
                                     :result '({:id 3, :title "abc", :done false},
                                                {:id 4, :title "abc", :done true},
                                                {:id 5, :title "def", :done true},
                                                {:id 6, :title "abc", :done false},
                                                {:id 7, :title "add", :done false})
                                     :indent-level 3}
                                    {:form '(filter :done)
                                     :result '({:id 4 :title "abc" :done true}
                                                {:id 5 :title "def" :done true})
                                     :indent-level 2}
                                    {:form '(map :id)
                                     :result '(4 5)
                                     :indent-level 1}
                                    {:form '(reduce dissoc todos)
                                     :result {3 {:id 3, :title "abc", :done false},
                                              6 {:id 6, :title "abc", :done false},
                                              7 {:id 7, :title "add", :done false}}}]
                            :form  '(->> (vals todos)
                                         (filter :done)
                                         (map :id)
                                         (reduce dissoc todos))})
        highlighted-form @(rf/subscribe [:code/highlighted-form])
        debug?           @(rf/subscribe [:settings/debug?])]
    [rc/v-box
     :size "1 1 auto"
     :class "code-panel"
     :children
     [(when debug? [:pre "Hover " (pr-str highlighted-form) "\n"])
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
                 after      (subs form-str end-index)]
             [[:pre.code-listing
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
                             :style {:margin-left (str (* 9 (:indent-level line)) "px")}
                             :attr {:on-mouse-enter #(do (rf/dispatch [:code/hover-form (:form line)])
                                                         true)
                                    :on-mouse-leave #(do (rf/dispatch [:code/exit-hover-form (:form line)])
                                                         true)}
                             :children [[:pre (zp/zprint-str (:form line))]
                                        ;; TODO: disable history expansion, or at least storing of it in ls.
                                        [components/simple-render (:result line) [(:id code-execution) i]]]]
                            ^{:key (random-uuid)}
                            [rc/gap-f :size "5px"])))))]])]))]]))


(defn render []
  (let [event-trace @(rf/subscribe [:epochs/current-event-trace])
        epoch-id    @(rf/subscribe [:epochs/current-match-state])]
    ;; Create a new id on each panel because Reagent can throw an exception if
    ;; the data provided in successive renders is sufficiently different.
    ^{:key epoch-id}
    [rc/v-box
     :class "event-panel"
     :gap common/gs-19s
     :children [[event-code]
                [event-section "Coeffects" (get-in event-trace [:tags :coeffects])]
                [event-section "Effects" (get-in event-trace [:tags :effects])]
                [event-section "Interceptors" (get-in event-trace [:tags :interceptors])]
                [rc/gap-f :size "0px"]]]))
