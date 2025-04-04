(ns day8.re-frame-10x.tools.identicon
  (:require [clojure.string :as str]))

;; Ported from identicon.js by Don Park - https://github.com/donpark/identicon
;; Stripped down for SVG only and converted to ClojureScript.

(defn- build-grid [hash]
  ;; Original used 16 bytes from hash, we use the single 32-bit integer hash
  ;; Use bits from the hash to fill a 5x5 grid, mirroring the first 2 columns to the last 2
  (let [;; Use lower bits for the grid pattern
        grid-pattern (bit-and hash 0xFFFFF) ;; Use 20 bits for 5x5 grid decisions
        grid (volatile! (vec (repeat 25 false)))]
    (dotimes [i 15] ;; Fill first 3 columns (15 cells)
      (when (pos? (bit-and grid-pattern (bit-shift-left 1 i)))
        (let [row (quot i 3)
              col (rem i 3)]
          (vswap! grid assoc (+ (* row 5) col) true))))
    ;; Mirror columns 0 and 1 to 4 and 3
    (dotimes [row 5]
      (vswap! grid assoc (+ (* row 5) 4) (get @grid (+ (* row 5) 0))) ;; col 4 = col 0
      (vswap! grid assoc (+ (* row 5) 3) (get @grid (+ (* row 5) 1)))) ;; col 3 = col 1
    @grid))

(defn- foreground-color [hash]
  ;; Use higher bits for color, ensure it's not too light.
  ;; Original used first 4 bytes of hash for HSL. We adapt for our single hash int.
  ;; Simple approach: use hash modulo 360 for hue, fixed saturation/lightness.
  (let [hue (mod (bit-shift-right hash 20) 360) ;; Use some higher bits for hue
        saturation 65
        lightness 40]
    (str "hsl(" hue "," saturation "%," lightness "%)")))

(defn svg
  "Generates identicon SVG data as Hiccup.
   Takes a string value and an options map.
   Options:
     :size       - width/height of the SVG (default 100)
     :background - vector [r g b a] 0-255 (default [240 240 240 255])
     :margin     - decimal percentage margin (default 0.08)"
  [value & {:keys [size background margin]
            :or   {size 14, background "white", margin 0}}]
  (when-not (str/blank? value)
    (let [hash        (hash-string value)
          fg-color    (foreground-color hash)
          grid        (build-grid hash)
          cell-size   (/ size 5.0)
          base-margin (* size margin)
          cell-margin (/ base-margin 2.0)
          final-size  (+ size base-margin)]
      (into [:svg {:width   final-size
                   :height  final-size
                   :viewBox (str "0 0 " final-size " " final-size)
                   :xmlns   "http://www.w3.org/2000/svg"
                   :style   {:background background}}
             [:text {:x                  0
                     :y                  0
                     :text-anchor        "middle"
                     :alignment-baseline "middle"
                     :fill               "rgba(0,0,0,0)"
                     :style              {:user-select :text}}
             (subs value 5)]]
            (for [row   (range 5)
                  col   (range 5)
                  :let  [idx (+ (* row 5) col)]
                  :when (get grid idx)]
              [:rect {:x      (+ cell-margin (* col cell-size))
                      :y      (+ cell-margin (* row cell-size))
                      :width  cell-size
                      :height cell-size
                      :fill   fg-color}])))))

;; Example usage (for testing in REPL):
;; (render-svg "hello" {:size 50})
;; (render-svg "test" {:size 30 :margin 0.1 :background [255 0 0 128]})
