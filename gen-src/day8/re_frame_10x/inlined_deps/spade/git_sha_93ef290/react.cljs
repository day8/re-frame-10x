(ns day8.re-frame-10x.inlined-deps.spade.git-sha-93ef290.react
  (:require [react :as react]
            [day8.re-frame-10x.inlined-deps.spade.git-sha-93ef290.container :refer [IStyleContainer]]
            [day8.re-frame-10x.inlined-deps.spade.git-sha-93ef290.container.alternate :refer [->AlternateStyleContainer]]
            [day8.re-frame-10x.inlined-deps.spade.git-sha-93ef290.container.dom :as dom]
            [day8.re-frame-10x.inlined-deps.spade.git-sha-93ef290.runtime :refer [*style-container*]]))

(defonce context (react/createContext nil))
(defonce Provider (.-Provider context))

(defn- provided-container []
  ;; NOTE: This hack is inspired by ReactN:
  ;; https://charles-stover.medium.com/how-reactn-hacks-react-context-9d112397f003
  (or (.-_currentValue2 context)
      (.-_currentValue context)))

;; NOTE: As soon as this namespace is used, we "upgrade" the global style-container
;; to also check the react context
(set! *style-container*
      (->AlternateStyleContainer
        provided-container
        *style-container*))

(defn with-style-container
  "Uses the provided IStyleContainer to render the styles of the children
   elements. This is a reagent-style React component, meant to wrap whatever
   part of your app needs to have its styled rendered elsewhere (often the
   root of the app), eg:

     [with-style-container container
      [your-view]]

   Note that behavior in the presence of a reactively changing `container` is
   undefined. You should expect to declare one container per render."
  [^IStyleContainer container & children]
  (into [:> Provider {:value container}]
        children))

(defn with-dom
  "A convenience for rendering Spade styles into an alternate dom target.
   The first argument may either be an actual DOM element, or a function which
   returns one.

   See `with-style-container`, which this uses under the hood."
  [get-dom-target & _children]
  (let [container (dom/create-container get-dom-target)]
    (fn with-dom-render [_ & children]
      (into [with-style-container container]
            children))))
