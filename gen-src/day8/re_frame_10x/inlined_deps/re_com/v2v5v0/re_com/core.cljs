(ns day8.re-frame-10x.inlined-deps.re-com.v2v5v0.re-com.core
  (:require [day8.re-frame-10x.inlined-deps.re-com.v2v5v0.re-com.alert          :as alert]
            [day8.re-frame-10x.inlined-deps.re-com.v2v5v0.re-com.box            :as box]
            [day8.re-frame-10x.inlined-deps.re-com.v2v5v0.re-com.buttons        :as buttons]
            [day8.re-frame-10x.inlined-deps.re-com.v2v5v0.re-com.close-button   :as close-button]
            [day8.re-frame-10x.inlined-deps.re-com.v2v5v0.re-com.datepicker     :as datepicker]
            [day8.re-frame-10x.inlined-deps.re-com.v2v5v0.re-com.dropdown       :as dropdown]
            [day8.re-frame-10x.inlined-deps.re-com.v2v5v0.re-com.typeahead      :as typeahead]
            [day8.re-frame-10x.inlined-deps.re-com.v2v5v0.re-com.input-time     :as input-time]
            [day8.re-frame-10x.inlined-deps.re-com.v2v5v0.re-com.splits         :as splits]
            [day8.re-frame-10x.inlined-deps.re-com.v2v5v0.re-com.misc           :as misc]
            [day8.re-frame-10x.inlined-deps.re-com.v2v5v0.re-com.modal-panel    :as modal-panel]
            [day8.re-frame-10x.inlined-deps.re-com.v2v5v0.re-com.popover        :as popover]
            [day8.re-frame-10x.inlined-deps.re-com.v2v5v0.re-com.selection-list :as selection-list]
            [day8.re-frame-10x.inlined-deps.re-com.v2v5v0.re-com.tabs           :as tabs]
            [day8.re-frame-10x.inlined-deps.re-com.v2v5v0.re-com.text           :as text]
            [day8.re-frame-10x.inlined-deps.re-com.v2v5v0.re-com.tour           :as tour]))

;; -----------------------------------------------------------------------------
;; re-com public API (see also day8.re-frame-10x.inlined-deps.re-com.v2v5v0.re-com.util)
;; -----------------------------------------------------------------------------

(def alert-box                  alert/alert-box)
(def alert-list                 alert/alert-list)

(def flex-child-style           box/flex-child-style)
(def flex-flow-style            box/flex-flow-style)
(def justify-style              box/justify-style)
(def align-style                box/align-style)
(def scroll-style               box/scroll-style)

(def h-box                      box/h-box)
(def v-box                      box/v-box)
(def box                        box/box)
(def line                       box/line)
(def gap                        box/gap)
(def scroller                   box/scroller)
(def border                     box/border)

(def button                     buttons/button)
(def md-circle-icon-button      buttons/md-circle-icon-button)
(def md-icon-button             buttons/md-icon-button)
(def info-button                buttons/info-button)
(def row-button                 buttons/row-button)
(def hyperlink                  buttons/hyperlink)
(def hyperlink-href             buttons/hyperlink-href)

(def close-button               close-button/close-button)

(def datepicker                 datepicker/datepicker)
(def datepicker-dropdown        datepicker/datepicker-dropdown)

(def single-dropdown            dropdown/single-dropdown)

(def typeahead                  typeahead/typeahead)

(def input-time                 input-time/input-time)

(def h-split                    splits/h-split)
(def v-split                    splits/v-split)

(def input-text                 misc/input-text)
(def input-password             misc/input-password)
(def input-textarea             misc/input-textarea)
(def checkbox                   misc/checkbox)
(def radio-button               misc/radio-button)
(def slider                     misc/slider)
(def progress-bar               misc/progress-bar)
(def throbber                   misc/throbber)

(def modal-panel                modal-panel/modal-panel)

(def popover-content-wrapper    popover/popover-content-wrapper)
(def popover-anchor-wrapper     popover/popover-anchor-wrapper)
(def popover-border             popover/popover-border)
(def popover-tooltip            popover/popover-tooltip)

(def selection-list             selection-list/selection-list)

(def horizontal-tabs            tabs/horizontal-tabs)
(def horizontal-bar-tabs        tabs/horizontal-bar-tabs)
(def vertical-bar-tabs          tabs/vertical-bar-tabs)
(def horizontal-pill-tabs       tabs/horizontal-pill-tabs)
(def vertical-pill-tabs         tabs/vertical-pill-tabs)

(def label                      text/label)
(def p                          text/p)
(def p-span                     text/p-span)
(def title                      text/title)

(def make-tour                  tour/make-tour)
(def start-tour                 tour/start-tour)
(def make-tour-nav              tour/make-tour-nav)
