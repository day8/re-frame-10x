(ns day8.re-frame.trace.view.event
  (:require [day8.re-frame.trace.utils.re-com :as rc]
            [day8.re-frame.trace.metamorphic :as metam]))

(defn render []
  [rc/v-box
   :padding "12px 0px"
   :children [[rc/label :label "Event"]
              [rc/label :label "Dispatch Point"]
              [rc/label :label "Coeffects"]
              [rc/label :label "Effects"]
              [rc/label :label "Interceptors"]

              [rc/h-box
               :children [[:p "Subs Run"] [:p "Created"] [:p "Destroyed"]]]
              [:p "Views Rendered"]
              [rc/h-box
               :children [[:p "Timing"] [:p "Animation Frames"]]]
              ]])
