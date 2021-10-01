(ns day8.reagent.impl.component
  (:require
    [goog.object            :as gobj]
    [clojure.string         :as string]
    [re-frame.trace         :as trace :include-macros true]
    [re-frame.interop       :as interop]
    [reagent.impl.component :as component]
    [reagent.impl.batching  :as batch]
    [reagent.impl.util      :as util]
    [reagent.ratom          :as ratom]
    [reagent.debug          :refer-macros [dev? assert-callable]]))

;; Monkey patched reagent.impl.component/wrap-funs to hook into render
(defn wrap-funs [fmap compiler]
  (when (dev?)
    (let [renders (select-keys fmap [:render :reagentRender])
          render-fun (-> renders vals first)]
      (assert (not (:componentFunction fmap)) ":component-function is no longer supported, use :reagent-render instead.")
      (assert (pos? (count renders)) "Missing reagent-render")
      (assert (== 1 (count renders)) "Too many render functions supplied")
      (assert-callable render-fun)))
  (let [render-fun (or (:reagentRender fmap)
                       (:render fmap))
        legacy-render (nil? (:reagentRender fmap))
        name (or (:displayName fmap)
                 (util/fun-name render-fun)
                 (str (gensym "reagent")))
        fmap (reduce-kv (fn [m k v]
                          (assoc m k (component/get-wrapper k v)))
                        {} fmap)]
    (assoc fmap
      :displayName name
      :cljsLegacyRender legacy-render
      :reagentRender render-fun
      :render (fn render []
                (this-as c
                  (let [component-name (component/component-name c)]
                    (trace/with-trace
                      {:op-type   :render
                       :tags      (if component-name
                                    {:component-name component-name}
                                    {})
                       :operation component-name}))
                  (if util/*non-reactive*
                    (component/do-render c compiler)
                    (let [^clj rat (gobj/get c "cljsRatom")
                          _        (batch/mark-rendered c)
                          res      (if (nil? rat)
                                     (ratom/run-in-reaction #(component/do-render c compiler) c "cljsRatom"
                                                            batch/queue-render component/rat-opts)
                                     (._run rat false))
                          cljs-ratom (gobj/get c "cljsRatom")]
                      (trace/merge-trace!
                        {:tags {:reaction (interop/reagent-id cljs-ratom)
                                :input-signals (when cljs-ratom
                                                 (map interop/reagent-id (gobj/get cljs-ratom "watching" :none)))}})
                      res)))))))

(defn patch-wrap-funs
  []
  (set! reagent.impl.component/wrap-funs wrap-funs))

(defonce original-custom-wrapper reagent.impl.component/custom-wrapper)

(defn custom-wrapper
  [key f]
  (case key
    :componentWillUnmount
    (fn componentWillUnmount []
      (this-as c
        (trace/with-trace
          {:op-type   key
           :operation (last (string/split (component/component-name c) #" > "))
           :tags      {:component-name (component/component-name c)
                       :reaction (interop/reagent-id (gobj/get c "cljsRatom"))}})
        (.call (original-custom-wrapper key f) c c)))
    (original-custom-wrapper key f)))

(defn patch-custom-wrapper
  []
  (set! reagent.impl.component/custom-wrapper custom-wrapper))
