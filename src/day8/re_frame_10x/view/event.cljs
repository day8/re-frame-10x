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


(def event-styles
  [:#--re-frame-10x--
   [:.event-panel
    {:padding "19px 19px 0px 0px"}]
   [:.light-heading {:font-weight "300"
                     :margin-bottom common/gs-19s}]
   [:.bold {:font-weight "bold"}]
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


;; Terminology:
;; Form: a single Clojure form (may have nested children)
;; Result: the result of execution of a single form
;; Fragment: the combination of a form and result
;; Listing: a block of traced Clojure code, e.g. an event handler function


(defn no-event-instructions
  []
  [rc/v-box
   :children [#_[:span.bm-heading-text.light-heading event-str]
              [rc/p {:style {:font-style "italic"}} "Code trace is not currently available for this event"]
              [:br]
              [rc/p "This panel can show the actual code of the event along with all of it's intermediate values."]
              [rc/p "To get to this magic going, you need to make a few adjustments to your project:"]
              [:ol
               [:li [rc/p "Add " [:span.bold "[day8.re-frame/debux \"0.5.0-SNAPSHOT\"]"] " to the :dev :dependencies section in project.clj"]]
               [:li [rc/p "Add " [:span.bold "\"debux.cs.core.trace_enabled_QMARK_\" true"] " to the :closure-defines section in project.clj"]]
               [:li [rc/p "Add " [:span.bold "[debux.cs.core :refer-macros [fn-traced]]"] " to the :require section of the event code file(s)"]]
               [:li [rc/p "Replace " [:span.bold "fn"] " with " [:span.bold "fn-traced"] " in the events to be traced in this panel"]]]]])


(defn code-header
  [code-execution-id line]
  (println ">>>>>> code-header:" (:id line))
  (let [open?-path [@(rf/subscribe [:epochs/current-epoch-id]) code-execution-id (:id line)]
        open?      (get-in @(rf/subscribe [:code/code-open?]) open?-path)]
    [rc/h-box
     :style {:border   code-border
             :overflow "hidden"
             :padding  "1px 6px"}
     :children [[rc/box
                 :width  "17px"
                 :height "17px"
                 :class  "noselect"
                 :style  {:cursor "pointer"
                          :color  "#b0b2b4"}
                 :attr   {:on-click (handler-fn (rf/dispatch [:code/set-code-visibility open?-path (not open?)]))}
                 :child  [rc/box
                          :margin "auto"
                          :child  [:span.arrow (if open? "▼" "▶")]]]
                [:pre
                 {:style {:margin-left "2px"
                          :margin-top  "2px"}}
                 (str (:form line))]]]))


(defn code-block
  [code-execution-id line]
  ;(println ">>>>>> code-block:" (:id line))
  [rc/box
   :style {:background-color "rgba(100, 255, 100, 0.08)"
           :border           code-border
           :margin-top       "-1px"
           :overflow-x       "auto"
           :overflow-y       "hidden"
           :padding          "0px 3px"}
   :child [components/simple-render (:result line) [@(rf/subscribe [:epochs/current-epoch-id]) code-execution-id (:id line)]]])


(defn event-expression
  [form]
  (let [highlighted-form @(rf/subscribe [:code/highlighted-form])
        form-str         (zp/zprint-str form)
        search-str       highlighted-form
        start            (str/index-of form-str search-str)
        length           (if (some? search-str)
                           (count (pr-str search-str))
                           0)
        before           (subs form-str 0 start)
        end-index        (+ start length)
        highlight        (subs form-str start end-index)
        after            (subs form-str end-index)]
    ;(println ">> event-expression:" (pr-str (subs (pr-str highlighted-form) 0 30)))
    ; DC: We get lots of React errors if we don't force a creation of a new element when the highlight changes. Not really sure why...
    ^{:key (pr-str highlighted-form)}
    [rc/box
     :style {:max-height (str (* 10 17) "px") ;; Add scrollbar after 10 lines
             ;:overflow-x "auto"
             :overflow-y "auto" ;; TODO: Need to overwrite some CSS in the components/highlight React component to get the horizontal scrollbar working properly
             ;:border     "1px solid #e3e9ed"
             ;:background-color "white"
             }

     :child (if (some? highlighted-form)
              [components/highlight {:language "clojure"}
               (list ^{:key "before"} before
                     ^{:key "hl"} [:span.code-listing--highlighted highlight]
                     ^{:key "after"} after)]
              [components/highlight {:language "clojure"}
               form-str])
     #_#_:child [:pre form-str]
     ]))


(defn event-fragments
  [fragments code-exec-id]
  ;(println ">> event-fragments - count:" (count fragments))
  (let [code-open? @(rf/subscribe [:code/code-open?])]
    [rc/v-box
     :size     "1"
     :style    {:overflow-y "auto"}
     :children (doall
                 (for [frag fragments]
                   (let [id (:id frag)]
                     ^{:key id}
                     [rc/v-box
                      :class    "code-fragment"
                      :style    {:margin-left (str (* 9 (dec (:indent-level frag))) "px")
                                 :margin-top  (when (pos? id) "-1px")}
                      :attr     {:on-mouse-enter (handler-fn #_(println "OVER:" (:id frag)) (rf/dispatch [:code/hover-form (:form frag)]))
                                 :on-mouse-leave (handler-fn #_(println " OUT:" (:id frag)) (rf/dispatch [:code/exit-hover-form (:form frag)]))}
                      :children [[code-header code-exec-id frag]
                                 (when (get-in code-open? [@(rf/subscribe [:epochs/current-epoch-id]) code-exec-id id])
                                   [code-block code-exec-id frag id])]])))]))


(defn event-code
  []
  (let [code-traces      @(rf/subscribe [:code/current-code])
        code-execution   (first code-traces) ;; Ignore multiple code executions for now
        highlighted-form (rf/subscribe [:code/highlighted-form])
        debug?           @(rf/subscribe [:settings/debug?])]
    ;(println "EVENT-CODE")
    (if-not code-execution
      [no-event-instructions]
      [rc/v-box
       :size "1 1 auto"
       :class "code-panel"
       :children [(when debug? [:pre "Hover " (pr-str @highlighted-form) "\n"])
                  [event-expression (:form code-execution)]
                  [rc/gap-f :size common/gs-19s]
                  [event-fragments (->> (:code code-execution)
                                        (remove (fn [line] (fn? (:result line)))))
                   (:id code-execution)]]])))


(defn render []
  (let [epoch-id @(rf/subscribe [:epochs/current-match-state])]
    ;; Create a new id on each panel because Reagent can throw an exception if
    ;; the data provided in successive renders is sufficiently different.
    ^{:key epoch-id}
    [rc/v-box
     :size     "1"
     :class    "event-panel"
     ;:style    {:margin-right common/gs-19s}
     :gap      common/gs-19s
     :children [[event-code]
                [rc/gap-f :size "0px"]]]))
