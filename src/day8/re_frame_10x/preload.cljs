(ns day8.re-frame-10x.preload
  "Use this namespace with the :preloads compiler option to perform the necessary setup for enabling
   re-frame-10x; e.g.

       {:compiler {:preloads [day8.re-frame-10x.preload] ...}}"
  (:require
   ;; Keep the public namespace loaded from every preload entry point so
   ;; standard preload-only consumers can probe
   ;; goog.global.day8.re_frame_10x.public.*. See preload_loads_public_test.clj.
   [day8.re-frame-10x.public]
   [day8.re-frame-10x.preload.react-17]))
