(ns day8.re-frame-10x.preload
  "Use this namespace with the :preloads compiler option to perform the necessary setup for enabling
   re-frame-10x; e.g.

       {:compiler {:preloads [day8.re-frame-10x.preload] ...}}"
  (:require
   [day8.re-frame-10x :as re-frame-10x]))

(re-frame-10x/init!)
