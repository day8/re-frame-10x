(ns todomvc.core
  (:require-macros [secretary.core :refer [defroute]])
  (:require [goog.events :as events]
            [reagent.core :as reagent]
            [reagent.dom.client :as rdc]
            [re-frame.core :refer [dispatch dispatch-sync]]
            [secretary.core :as secretary]
    ;; These two are only required to make the compiler
    ;; load them (see docs/Basic-App-Structure.md)
            [todomvc.events]
            [todomvc.subs]
            [todomvc.views]
            [devtools.core :as devtools])
  (:import [goog History]
           [goog.history EventType]))

;; -- Debugging aids ----------------------------------------------------------
;; we love https://github.com/binaryage/cljs-devtools
(devtools/install!)

;; so that println writes to `console.log`
(enable-console-print!)

;; Put an initial value into app-db.
;; The event handler for `:initialise-db` can be found in `events.cljs`
;; Using the sync version of dispatch means that value is in
;; place before we go onto the next step.
(dispatch-sync [:initialise-db])

;; -- Routes and History ------------------------------------------------------
;; Although we use the secretary library below, that's mostly a historical
;; accident. You might also consider using:
;;   - https://github.com/DomKM/silk
;;   - https://github.com/juxt/bidi
;; We don't have a strong opinion.
;;
(defroute "/" [] (dispatch [:set-showing :all]))

(defroute #"/([a-z]+)" [filter] (dispatch [:set-showing (keyword filter)]))

(def history
  (doto (History.)
    (events/listen EventType.NAVIGATE
                   (fn [^js event] (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -- Entry Point -------------------------------------------------------------
;; Within ../../resources/public/index.html you'll see this code
;;    window.onload = function () {
;;      todomvc.core.main();
;;    }
;; So this is the entry function that kicks off the app once the HTML is loaded.
;;

(defonce react-root
  (rdc/create-root (.getElementById js/document "app")))

(defn main
  []
      ;; Render the UI into the HTML's <div id="app" /> element
      ;; The view function `todomvc.views/todo-app` is the
      ;; root view for the entire UI.
  (rdc/render react-root [todomvc.views/todo-app]))
