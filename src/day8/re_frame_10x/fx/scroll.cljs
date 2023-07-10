(ns day8.re-frame-10x.fx.scroll
  (:require
   [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]))

(rf/reg-fx
 ::into-view
 (fn [{:keys [js-dom]}]
   (when (instance? js/Element js-dom)
     (.scrollIntoView js-dom))))