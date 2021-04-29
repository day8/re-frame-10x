(ns day8.re-frame-10x.components.cljs-devtools)

(defmacro with-cljs-devtools-prefs [prefs & body]
  `(let [previous-config# (devtools.prefs/get-prefs)
         prefs#           ~prefs]
     (try
       (devtools.prefs/set-prefs! prefs#)
       ~@body
       (finally
         (assert (= (devtools.prefs/get-prefs) prefs#) "someone modified devtools.prefs behind our back!")
         (devtools.prefs/set-prefs! previous-config#)))))