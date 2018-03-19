(ns day8.re-frame-10x.view.event
  (:require [day8.re-frame-10x.utils.re-com :as rc]
            [day8.re-frame-10x.view.components :as components]
            [day8.re-frame-10x.common-styles :as common]
            [mranderson047.garden.v1v3v3.garden.units :as units]
            [mranderson047.reagent.v0v7v0.reagent.core :as reagent]
            [mranderson047.re-frame.v0v10v2.re-frame.core :as rf]
            [zprint.core :as zp]
            [clojure.string :as str])
  (:require-macros [day8.re-frame-10x.utils.macros :refer [with-cljs-devtools-prefs]]
                   [day8.re-frame-10x.utils.re-com :refer [handler-fn]]))

(def code-border (str "1px solid " common/white-background-border-color))


(def current-code
  (reagent/atom
    {1 {:title ":event/handler",
        :id    1,
        :code  {1 {:id           1
                   :open?        false
                   :form         'dissoc
                   :result       dissoc
                   :indent-level 1}
                2 {:id           2
                   :open?        false
                   :form         'todos
                   :result       {3 {:id 3, :title "abc", :done false},
                                  4 {:id 4, :title "abc", :done true},
                                  5 {:id 5, :title "def", :done true},
                                  6 {:id 6, :title "abc", :done false},
                                  7 {:id 7, :title "add", :done false}}
                   :indent-level 1}
                3 {:id           3
                   :open?        false
                   :form         'todos
                   :result       {3 {:id 3, :title "abc", :done false},
                                  4 {:id 4, :title "abc", :done true},
                                  5 {:id 5, :title "def", :done true},
                                  6 {:id 6, :title "abc", :done false},
                                  7 {:id 7, :title "add", :done false}}
                   :indent-level 4}
                4 {:id           4
                   :open?        false
                   :form         '(vals todos)
                   :result       '({:id 3, :title "abc", :done false},
                                    {:id 4, :title "abc", :done true},
                                    {:id 5, :title "def", :done true},
                                    {:id 6, :title "abc", :done false},
                                    {:id 7, :title "add", :done false})
                   :indent-level 3}
                5 {:id           5
                   :open?        false
                   :form         '(filter :done)
                   :result       '({:id 4 :title "abc" :done true}
                                    {:id 5 :title "def" :done true})
                   :indent-level 2}
                6 {:id           6
                   :open?        false
                   :form         '(map :id)
                   :result       '(4 5)
                   :indent-level 1}
                7 {:id     7
                   :open?  false
                   :form   '(reduce dissoc todos)
                   :result {3 {:id 3, :title "abc", :done false},
                            6 {:id 6, :title "abc", :done false},
                            7 {:id 7, :title "add", :done false}}}
                8 {:id     8
                   :open?  false
                   :form   '(->> (vals todos)
                                 (filter :done)
                                 (map :id)
                                 (reduce dissoc todos))
                   :result {3 {:id 3, :title "abc", :done false},
                            6 {:id 6, :title "abc", :done false},
                            7 {:id 7, :title "add", :done false}}}}
        :form  '(->> (vals todos)
                     (filter :done)
                     (map :id)
                     (reduce dissoc todos))}}))


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


(defn code-header
  [code-execution line]
  (let [open? (:open? line)]
    [rc/h-box
     :style {:border  code-border
             :padding "1px 6px"}
     :children [[rc/box
                 :width  "17px"
                 :height "17px"
                 :class  "noselect"
                 :style  {:cursor "pointer"
                          :color  "#b0b2b4"}
                 :attr   {:on-click (handler-fn
                                      #_#(rf/dispatch [:subs/open-pod? id (not open?)])
                                      (swap! current-code #(update-in % [(:id code-execution) :code (:id line) :open?] not)))}
                 :child  [rc/box
                          :margin "auto"
                          :child  [:span.arrow (if open? "▼" "▶")]]]
                [:pre
                 {:style {:margin-left "2px"
                          :margin-top  "2px"}}
                 (zp/zprint-str (:form line))]]]))


(defn code-block
  [code-execution line i]
  [rc/box
   :style {:background-color "rgba(100, 255, 100, 0.08)"
           :border           code-border
           :margin-top       "-1px"
           :overflow-x       "auto"
           :overflow-y       "hidden"
           :padding          "0px 3px"}
   :child [components/simple-render (:result line) [(:id code-execution) i]]])


;; Terminology:
;; Form: a single Clojure form (may have nested children)
;; Result: the result of execution of a single form
;; Fragment: the combination of a form and result
;; Listing: a block of traced Clojure code, e.g. an event handler function

(defn event-code []
  (let [code-traces      @current-code ;; @(rf/subscribe [:code/current-code])
        highlighted-form @(rf/subscribe [:code/highlighted-form])
        ;highlighted-form '(filter :done)
        debug?           @(rf/subscribe [:settings/debug?])]
    [rc/v-box
     :size "1 1 auto"
     :class "code-panel"
     :children
     [(when debug? [:pre "Hover " (pr-str highlighted-form) "\n"])
      (doall
        (for [code-execution (vals code-traces)]
          ^{:key (:id code-execution)}
          [rc/v-box
           :size "1 1 auto"
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
             [
              ;; We get lots of React errors if we don't force a creation of a new element
              ;; when the highlight changes. Not really sure why...
              ^{:key (pr-str highlighted-form)}
              [:div
               (if (some? highlighted-form)
                 [components/highlight {:language "clojure"}
                  (list ^{:key "before"} before
                        ^{:key "hl"} [:span.code-listing--highlighted highlight]
                        ^{:key "after"} after)]
                 [components/highlight {:language "clojure"}
                  form-str])]

              [:br]
              [rc/v-box
               :size     "1 1 auto"
               :style    {:margin-bottom common/gs-31s}
               :children (doall
                           (->> (:code code-execution)
                                vals ;; TODO: Now that this is a map, the order is not guaranteed
                                ;; Remove traced function values, these are usually not very interesting in and of themselves.
                                (remove (fn [line] (fn? (:result line))))
                                (map-indexed
                                  (fn [i line]
                                    (list
                                      ;; See https://github.com/reagent-project/reagent/issues/350 for why we use random-uuid here
                                      ^{:key (random-uuid)}
                                      [rc/v-box
                                       :class "code-fragment"
                                       :style {:margin-left (str (* 9 (:indent-level line)) "px")
                                               :margin-top  (when (pos? i) "-1px")}
                                       ;; on-mouse enter/leave fires fewer events (only on enter/leave of outer form)
                                       ;; but the events don't seem to be reliably sent in order.
                                       ;; Instead we use pointer-events: none on the children of the code fragments
                                       ;; to prevent lots of redundant events.
                                       :attr {:on-mouse-enter (handler-fn (rf/dispatch [:code/hover-form (:form line)]))
                                              :on-mouse-leave  (handler-fn (rf/dispatch [:code/exit-hover-form (:form line)]))}
                                       :children [[code-header code-execution line]
                                                  ;; TODO: disable history expansion, or at least storing of it in ls.
                                                  (when (:open? line)
                                                    [code-block code-execution line i])]])))))]])]))]]))


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
