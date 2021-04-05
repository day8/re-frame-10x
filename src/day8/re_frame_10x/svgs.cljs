(ns day8.re-frame-10x.svgs)

(defn triangle-down
  [& {:keys [style fill]
      :or   {style {}}}]
  [:svg {:height "7px"
         :viewBox "0 0 12 7"
         :style   style}
   [:g {:id "Canvas"
        :transform "translate(-2344 -40)"}
    [:g {:id "Polygon"}
     [:use {:href "#path0_fill"
            :transform "matrix(-1 9.54098e-18 -9.54098e-18 -1 2356 47)"
            :fill fill}]]]
   [:defs
    [:path {:id "path0_fill"
            :d "M 6.05481 0L 12 7L 0 7L 6.05481 0Z"}]]])

(defn round-arrow
  []
  [:svg {:width "34"
         :height "32"
         :viewBox "0 0 34 32"}
   [:g {:id "Canvas"
        :transform "translate(-918 -1231)"}
    [:g {:id "Vector"}
     [:use {:href "#path0_stroke"
            :transform "translate(921.009 1231)"
            :fill "#767A7C"}]]]
   [:defs
    [:path {:id "path0_stroke"
            :d "M 0 0L -2.8822 5.00262L 2.8913 4.99737L 0 0ZM -0.495905 4.5006C -0.476808 11.1305 -0.366878 16.1392 0.247919 19.9008C 0.867102 23.6815 2.01224 26.3073 4.17733 28.0747C 6.32558 29.8284 9.39641 30.6673 13.6945 31.0836C 17.9937 31.4994 23.6227 31.5 30.9712 31.5L 30.9712 30.5C 23.6227 30.5 18.0419 30.5006 13.791 30.0883C 9.53899 29.6764 6.71945 28.859 4.8097 27.3001C 2.91677 25.7548 1.83627 23.4118 1.23477 19.7391C 0.628892 16.0473 0.523116 11.1182 0.504091 4.49773L -0.495905 4.5006Z"}]]])