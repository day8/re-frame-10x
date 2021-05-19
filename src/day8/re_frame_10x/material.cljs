(ns day8.re-frame-10x.material
  (:require
    [day8.re-frame-10x.styles :as styles])
  (:refer-clojure :exclude [print]))

;; Icons from https://material.io/resources/icons/ 'Sharp' theme.
;; Names have been kept the same for ease of reference.

(defn add
  [{:keys [size]}]
  [:svg {:height  size
         :viewBox "0 0 24 24"
         :width   size}
   [:path {:d "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"}]])

(defn arrow-drop-down
  [{:keys [size]
    :or   {size styles/gs-19s}}]
  [:svg {:height  size
         :viewBox "0 0 24 24"
         :width   size}
   [:path {:d "M7 10l5 5 5-5H7z"}]])

(defn arrow-drop-up
  [{:keys [size]
    :or   {size styles/gs-19s}}]
  [:svg {:height  size
         :viewBox "0 0 24 24"
         :width   size}
   [:path {:d "M7 14l5-5 5 5H7z"}]])

(defn arrow-left
  [{:keys [size]
    :or   {size styles/gs-19s}}]
  [:svg {:height  size
         :viewBox "0 0 24 24"
         :width   size}
   [:path {:d "M14 7l-5 5 5 5V7z"}]])

(defn arrow-right
  [{:keys [size]
    :or   {size styles/gs-19s}}]
  [:svg {:height  size
         :viewBox "0 0 24 24"
         :width   size}
   [:path {:d "M10 17l5-5-5-5v10z"}]])

(defn help-outline
  [{:keys [size]
    :or   {size styles/gs-19s}}]
  [:svg {:height  size
         :viewBox "0 0 24 24"
         :width   size}
   [:path {:d "M11 18h2v-2h-2v2zm1-16C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-14c-2.21 0-4 1.79-4 4h2c0-1.1.9-2 2-2s2 .9 2 2c0 2-3 1.75-3 5h2c0-2.25 3-2.5 3-5 0-2.21-1.79-4-4-4z"}]])

(defn open-in-new
  [{:keys [size]
    :or   {size styles/gs-19s}}]
  [:svg {:height  size
         :viewBox "0 0 24 24"
         :width   size}
   [:path {:d "M19 19H5V5h7V3H3v18h18v-9h-2v7zM14 3v2h3.59l-9.83 9.83 1.41 1.41L19 6.41V10h2V3h-7z"}]])

(defn refresh
  [{:keys [size]
    :or   {size styles/gs-19s}}]
  [:svg {:height  size
         :viewBox "0 0 24 24"
         :width   size}
   [:path {:d "M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"}]])

(defn settings
  [{:keys [size]
    :or   {size styles/gs-19s}}]
  [:svg {:height  size
         :viewBox "0 0 24 24"
         :width   size}
   [:path {:d "M19.44 12.99l-.01.02c.04-.33.08-.67.08-1.01 0-.34-.03-.66-.07-.99l.01.02 2.44-1.92-2.43-4.22-2.87 1.16.01.01c-.52-.4-1.09-.74-1.71-1h.01L14.44 2H9.57l-.44 3.07h.01c-.62.26-1.19.6-1.71 1l.01-.01-2.88-1.17-2.44 4.22 2.44 1.92.01-.02c-.04.33-.07.65-.07.99 0 .34.03.68.08 1.01l-.01-.02-2.1 1.65-.33.26 2.43 4.2 2.88-1.15-.02-.04c.53.41 1.1.75 1.73 1.01h-.03L9.58 22h4.85s.03-.18.06-.42l.38-2.65h-.01c.62-.26 1.2-.6 1.73-1.01l-.02.04 2.88 1.15 2.43-4.2s-.14-.12-.33-.26l-2.11-1.66zM12 15.5c-1.93 0-3.5-1.57-3.5-3.5s1.57-3.5 3.5-3.5 3.5 1.57 3.5 3.5-1.57 3.5-3.5 3.5z"}]])

(defn skip-next
  [{:keys [size]
    :or   {size styles/gs-19s}}]
  [:svg {:height  size
         :viewBox "0 0 24 24"
         :width   size}
   [:path {:d "M6 18l8.5-6L6 6v12zM16 6v12h2V6h-2z"}]])

(defn chevron-right
  [{:keys [size]
    :or   {size styles/gs-19s}}]
  [:svg {:height  size
         :viewBox "0 0 24 24"
         :width   size}
   [:path {:d "M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"}]])

(defn radio-button-unchecked
  [{:keys [size]
    :or   {size styles/gs-19s}}]
  [:svg {:height  size
         :viewBox "0 0 24 24"
         :width   size}
   [:path {:d "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8z"}]])

(defn radio-button-checked
  [{:keys [size]
    :or   {size styles/gs-19s}}]
  [:svg {:height  size
         :viewBox "0 0 24 24"
         :width   size}
   [:path {:d "M12 7c-2.76 0-5 2.24-5 5s2.24 5 5 5 5-2.24 5-5-2.24-5-5-5zm0-5C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8z"}]])

(defn check-box
  [{:keys [size]
    :or   {size styles/gs-19s}}]
  [:svg {:height  size
         :viewBox "0 0 24 24"
         :width   size}
   [:path {:d "M19 3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.11 0 2-.9 2-2V5c0-1.1-.89-2-2-2zm-9 14l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"}]])

(defn check-box-outline-blank
  [{:keys [size]
    :or   {size styles/gs-19s}}]
  [:svg {:height  size
         :viewBox "0 0 24 24"
         :width   size}
   [:path {:d "M19 5v14H5V5h14m0-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2z"}]])

(defn light-mode
  [{:keys [size]
    :or   {size styles/gs-19s}}]
  [:svg {:enable-background "new 0 0 24 24"
         :height            size
         :viewBox           "0 0 24 24"
         :width             size}
   [:path {:d "M12,7c-2.76,0-5,2.24-5,5s2.24,5,5,5s5-2.24,5-5S14.76,7,12,7L12,7z M2,13l2,0c0.55,0,1-0.45,1-1s-0.45-1-1-1l-2,0 c-0.55,0-1,0.45-1,1S1.45,13,2,13z M20,13l2,0c0.55,0,1-0.45,1-1s-0.45-1-1-1l-2,0c-0.55,0-1,0.45-1,1S19.45,13,20,13z M11,2v2 c0,0.55,0.45,1,1,1s1-0.45,1-1V2c0-0.55-0.45-1-1-1S11,1.45,11,2z M11,20v2c0,0.55,0.45,1,1,1s1-0.45,1-1v-2c0-0.55-0.45-1-1-1 C11.45,19,11,19.45,11,20z M5.99,4.58c-0.39-0.39-1.03-0.39-1.41,0c-0.39,0.39-0.39,1.03,0,1.41l1.06,1.06 c0.39,0.39,1.03,0.39,1.41,0s0.39-1.03,0-1.41L5.99,4.58z M18.36,16.95c-0.39-0.39-1.03-0.39-1.41,0c-0.39,0.39-0.39,1.03,0,1.41 l1.06,1.06c0.39,0.39,1.03,0.39,1.41,0c0.39-0.39,0.39-1.03,0-1.41L18.36,16.95z M19.42,5.99c0.39-0.39,0.39-1.03,0-1.41 c-0.39-0.39-1.03-0.39-1.41,0l-1.06,1.06c-0.39,0.39-0.39,1.03,0,1.41s1.03,0.39,1.41,0L19.42,5.99z M7.05,18.36 c0.39-0.39,0.39-1.03,0-1.41c-0.39-0.39-1.03-0.39-1.41,0l-1.06,1.06c-0.39,0.39-0.39,1.03,0,1.41s1.03,0.39,1.41,0L7.05,18.36z"}]])

(defn dark-mode
  [{:keys [size]
    :or   {size styles/gs-19s}}]
  [:svg {:enable-background "new 0 0 24 24"
         :height            size
         :viewBox           "0 0 24 24"
         :width             size}
   [:path {:d "M12,3c-4.97,0-9,4.03-9,9s4.03,9,9,9s9-4.03,9-9c0-0.46-0.04-0.92-0.1-1.36c-0.98,1.37-2.58,2.26-4.4,2.26 c-2.98,0-5.4-2.42-5.4-5.4c0-1.81,0.89-3.42,2.26-4.4C12.92,3.04,12.46,3,12,3L12,3z"}]])

(defn check-circle-outline
  [{:keys [size]
    :or   {size styles/gs-19s}}]
  [:svg {:height  size
         :viewBox "0 0 24 24"
         :width   size}
   [:path {:d "M16.59 7.58L10 14.17l-3.59-3.58L5 12l5 5 8-8zM12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8z"}]])

(defn close
  [{:keys [size]
    :or   {size styles/gs-19s}}]
  [:svg {:height  size
         :viewBox "0 0 24 24"
         :width   size}
   [:path {:d "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"}]])

(defn content-copy
  [{:keys [size]
    :or   {size styles/gs-19s}}]
  [:svg {:height  size
         :viewBox "0 0 24 24"
         :width   size}
   [:path {:d "M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"}]])

(defn unfold-more
  [{:keys [size]
    :or   {size styles/gs-19s}}]
  [:svg {:height  size
         :viewBox "0 0 24 24"
         :width   size}
   [:path {:d "M12 5.83L15.17 9l1.41-1.41L12 3 7.41 7.59 8.83 9 12 5.83zm0 12.34L8.83 15l-1.41 1.41L12 21l4.59-4.59L15.17 15 12 18.17z"}]])

(defn unfold-less
  [{:keys [size]
    :or   {size styles/gs-19s}}]
  [:svg {:height  size
         :viewBox "0 0 24 24"
         :width   size}
   [:path {:d "M7.41 18.59L8.83 20 12 16.83 15.17 20l1.41-1.41L12 14l-4.59 4.59zm9.18-13.18L15.17 4 12 7.17 8.83 4 7.41 5.41 12 10l4.59-4.59z"}]])

(defn search
  [{:keys [size]
    :or   {size styles/gs-19s}}]
  [:svg {:height  size
         :viewBox "0 0 24 24"
         :width   size}
   [:path {:d "M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"}]])

(defn clear
  [{:keys [size]
    :or   {size styles/gs-19s}}]
  [:svg {:height  size
         :viewBox "0 0 24 24"
         :width   size}
   [:path {:d "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"}]])

(defn print
  [{:keys [size]
    :or   {size styles/gs-19s}}]
  [:svg {:height  size
         :viewBox "0 0 24 24"
         :width   size}
   [:path {:d "M19 8H5c-1.66 0-3 1.34-3 3v6h4v4h12v-4h4v-6c0-1.66-1.34-3-3-3zm-3 11H8v-5h8v5zm3-7c-.55 0-1-.45-1-1s.45-1 1-1 1 .45 1 1-.45 1-1 1zm-1-9H6v4h12V3z"}]])