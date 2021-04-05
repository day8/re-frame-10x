(ns day8.re-frame-10x.fx.window
  (:require
    [goog.object                                                  :as gobj]
    [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]))

(defn open-debugger-window
  "Originally copied from re-frisk.devtool/open-debugger-window"
  [{:keys [width height top left on-load on-success on-failure] :as dimensions}]
  (let [doc-title        js/document.title
        new-window-title (goog.string/escapeString (str "re-frame-10x | " doc-title))
        new-window-html  (str "<head><title>"
                              new-window-title
                              "</title></head><body style=\"margin: 0px;\"><div id=\"--re-frame-10x--\" class=\"external-window\"></div></body>")]
    ;; We would like to set the windows left and top positions to match the monitor that it was on previously, but Chrome doesn't give us
    ;; control over this, it will only position it within the same display that it was popped out on.
    (if-let [w (js/window.open "about:blank" "re-frame-10x-popout"
                               (str "width=" width ",height=" height ",left=" left ",top=" top
                                    ",resizable=yes,scrollbars=yes,status=no,directories=no,toolbar=no,menubar=no"))]
      (let [d (.-document w)]
        ;; We had to comment out the following unmountComponentAtNode as it causes a React exception we assume
        ;; because React says el is not a root container that it knows about.
        ;; In theory by not freeing up the resources associated with this container (e.g. event handlers) we may be
        ;; creating memory leaks. However with observation of the heap in developer tools we cannot see any significant
        ;; unbounded growth in memory usage.
        ;(when-let [el (.getElementById d "--re-frame-10x--")]
        ;  (r/unmount-component-at-node el)))
        (.open d)
        (.write d new-window-html)
        (gobj/set w "onload" (partial on-load w d))
        (.close d)
        (rf/dispatch on-success))
      (rf/dispatch on-failure))))

(rf/reg-fx
  ::open-debugger-window
  open-debugger-window)