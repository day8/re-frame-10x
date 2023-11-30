(ns day8.re-frame-10x.preload.react-18
  "Use this namespace with the :preloads compiler option to perform the necessary setup for enabling
   re-frame-10x; e.g.

       {:compiler {:preloads [day8.re-frame-10x.preload] ...}}

   This namespace is intended for use with React 18.
   It launches re-frame-10x with react's new dom client API.
   See https://react.dev/blog/2022/03/29/react-v18#react-dom-client."
  (:require
   [day8.re-frame-10x                                                :as re-frame-10x]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core     :as rf]
   [day8.re-frame-10x.inlined-deps.reagent.v1v2v0.reagent.dom.client :as rdc]
   [day8.re-frame-10x.events                                         :as events]))

(re-frame-10x/patch!)

(rf/dispatch-sync [::events/init re-frame-10x/project-config])

(when re-frame-10x/init-event?
  (re-frame.core/reg-event-db :day8.re-frame-10x/init (fn [db _] db))
  (re-frame.fx/dispatch-later {:ms 500 :dispatch [:day8.re-frame-10x/init]}))

(rf/clear-subscription-cache!)

(def shadow-root (re-frame-10x/create-shadow-root))

(rdc/render (rdc/create-root shadow-root)
            (re-frame-10x/create-style-container shadow-root))
