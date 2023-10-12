(ns day8.re-frame-10x.fx.log
  (:require-macros [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.trace])
  (:require
   [clojure.pprint :refer [pprint]]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.core :as rf]
   [day8.re-frame-10x.inlined-deps.re-frame.v1v3v0.re-frame.loggers :as loggers]
   [day8.re-frame-10x.fx.clipboard :as clipboard]))

(defn pretty [value] (binding [*print-length* 20]
                       (with-out-str (pprint value))))

(rf/reg-fx
 ::console
 (fn [{:keys [value]}]
   (loggers/console :log value)))

(rf/reg-fx
 ::console-raw
 (fn [{:keys [value pretty?]}]
   (loggers/console :log (if pretty?
                           (pretty value)
                           (pr-str value)))))

(rf/reg-fx
 ::clipboard
 (fn [{:keys [value pretty?]}]
   (clipboard/copy! (cond-> value pretty? pretty))))
