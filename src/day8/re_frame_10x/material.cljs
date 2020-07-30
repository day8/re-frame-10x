(ns day8.re-frame-10x.material)

;; Icons from https://material.io/resources/icons/ 'Sharp' theme.
;; Names have been kept the same for ease of reference.

(defn add
  [& {:keys [fill]}]
  [:svg {:fill    fill
         :height  "24"
         :viewBox "0 0 24 24"
         :width   "24"}
   [:path {:d "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"}]])

(defn arrow-drop-down
  [& {:keys [fill]}]
  [:svg {:fill    fill
         :height  "24"
         :viewBox "0 0 24 24"
         :width   "24"}
   [:path {:d "M7 10l5 5 5-5H7z"}]])

(defn arrow-drop-up
  [& {:keys [fill]}]
  [:svg {:fill    fill
         :height  "24"
         :viewBox "0 0 24 24"
         :width   "24"}
   [:path {:d "M7 14l5-5 5 5H7z"}]])

(defn arrow-left
  [& {:keys [fill]}]
  [:svg {:fill    fill
         :height  "24"
         :viewBox "0 0 24 24"
         :width   "24"}
   [:path {:d "M14 7l-5 5 5 5V7z"}]])

(defn arrow-right
  [& {:keys [fill]}]
  [:svg {:fill    fill
         :height  "24"
         :viewBox "0 0 24 24"
         :width   "24"}
   [:path {:d "M10 17l5-5-5-5v10z"}]])

(defn help
  [& {:keys [fill]}]
  [:svg {:fill    fill
         :height  "24"
         :viewBox "0 0 24 24"
         :width   "24"}
   [:path {:d "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 17h-2v-2h2v2zm2.07-7.75l-.9.92C13.45 12.9 13 13.5 13 15h-2v-.5c0-1.1.45-2.1 1.17-2.83l1.24-1.26c.37-.36.59-.86.59-1.41 0-1.1-.9-2-2-2s-2 .9-2 2H8c0-2.21 1.79-4 4-4s4 1.79 4 4c0 .88-.36 1.68-.93 2.25z"}]])

(defn open-in-new
  [& {:keys [fill]}]
  [:svg {:fill    fill
         :height  "24"
         :viewBox "0 0 24 24"
         :width   "24"}
   [:path {:d "M19 19H5V5h7V3H3v18h18v-9h-2v7zM14 3v2h3.59l-9.83 9.83 1.41 1.41L19 6.41V10h2V3h-7z"}]])

(defn refresh
  [& {:keys [fill]}]
  [:svg {:fill    fill
         :height  "24"
         :viewBox "0 0 24 24"
         :width   "24"}
   [:path {:d "M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"}]])

(defn settings
  [& {:keys [fill]}]
  [:svg {:fill    fill
         :height  "24"
         :viewBox "0 0 24 24"
         :width   "24"}
   [:path {:d "M19.44 12.99l-.01.02c.04-.33.08-.67.08-1.01 0-.34-.03-.66-.07-.99l.01.02 2.44-1.92-2.43-4.22-2.87 1.16.01.01c-.52-.4-1.09-.74-1.71-1h.01L14.44 2H9.57l-.44 3.07h.01c-.62.26-1.19.6-1.71 1l.01-.01-2.88-1.17-2.44 4.22 2.44 1.92.01-.02c-.04.33-.07.65-.07.99 0 .34.03.68.08 1.01l-.01-.02-2.1 1.65-.33.26 2.43 4.2 2.88-1.15-.02-.04c.53.41 1.1.75 1.73 1.01h-.03L9.58 22h4.85s.03-.18.06-.42l.38-2.65h-.01c.62-.26 1.2-.6 1.73-1.01l-.02.04 2.88 1.15 2.43-4.2s-.14-.12-.33-.26l-2.11-1.66zM12 15.5c-1.93 0-3.5-1.57-3.5-3.5s1.57-3.5 3.5-3.5 3.5 1.57 3.5 3.5-1.57 3.5-3.5 3.5z"}]])

(defn skip-next
  [& {:keys [fill]}]
  [:svg {:fill    fill
         :height  "24"
         :viewBox "0 0 24 24"
         :width   "24"}
   [:path {:d "M6 18l8.5-6L6 6v12zM16 6v12h2V6h-2z"}]])

(defn unfold-less
  [& {:keys [fill]}]
  [:svg {:fill    fill
         :height  "24"
         :viewBox "0 0 24 24"
         :width   "24"}
   [:path {:d "M7.41 18.59L8.83 20 12 16.83 15.17 20l1.41-1.41L12 14l-4.59 4.59zm9.18-13.18L15.17 4 12 7.17 8.83 4 7.41 5.41 12 10l4.59-4.59z"}]])

(defn unfold-more
  [& {:keys [fill]}]
  [:svg {:fill    fill
         :height  "24"
         :viewBox "0 0 24 24"
         :width   "24"}
   [:path {:d "M12 5.83L15.17 9l1.41-1.41L12 3 7.41 7.59 8.83 9 12 5.83zm0 12.34L8.83 15l-1.41 1.41L12 21l4.59-4.59L15.17 15 12 18.17z"}]])
