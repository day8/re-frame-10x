(ns day8.re-frame-10x.utils.animated
  "Provides animation support for Regent components

   Depends on react-flip-move via cljsjs/react-flip-move
   https://github.com/cljsjs/packages/tree/master/react-flip-move

   see: https://github.com/joshwcomeau/react-flip-move#table-of-contents
   see: http://www.upgradingdave.com/blog/posts/2016-12-17-permutation.html
   See: https://medium.com/developers-writing/animating-the-unanimatable-1346a5aab3cd#.20km1k5jr

   Usage in apps:
    [animated/component
      (animated/v-box-options {:style {:margin-left \"-34px\"}}
      (map
        (fn [item] ^{:key (:id item)} [list-item item])
        items-list)

   see additional properties that can be specified in options
   https://github.com/joshwcomeau/react-flip-move#options"
  (:require
    [mranderson047.reagent.v0v7v0.reagent.core :as reagent]
    [cljsjs.react-flip-move]))

(def component
  (reagent/adapt-react-class js/FlipMove))

(def ^{:constant true
       :doc "mimic v-box style properties for standard :div
             note1: typicaly you should also set :class \"rc-v-box display-flex\"
             note2: re-com.box/justify-style & align-style for others"}
  rc-v-box-style
  {:align-items     "stretch"
   :flex-flow       "column nowrap"
   :flex            "0 0 auto"
   :justify-content "flex-start"})


(defn v-box-options
  "return merged component attributes suitable for animated v-box.
   See https://github.com/joshwcomeau/react-flip-move#options
   for supported props other then :style"
  [options]
  (-> options
      (update :style #(merge rc-v-box-style %))
      (assoc  :class "rc-v-box display-flex")))

(def ^{:constant true
       :doc      "mimic h-box style properties for standard :div
                  note1: typicaly you should also set :class \"rc-v-box display-flex\"
                  note2: re-com.box/justify-style & align-style for others"}
  rc-h-box-style
  {:align-items     "stretch"
   :flex-flow       "row nowrap"
   :flex            "0 0 auto"
   :justify-content "flex-start"})

(defn h-box-options
  "return merged component attributes suitable for animated h-box.
   See https://github.com/joshwcomeau/react-flip-move#options
   for supported props other then :style"
  [options]
  (-> options
      (update :style #(merge rc-h-box-style %))
      (assoc  :class "rc-h-box display-flex")))
