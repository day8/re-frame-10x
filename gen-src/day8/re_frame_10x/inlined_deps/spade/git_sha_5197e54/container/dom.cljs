(ns day8.re-frame-10x.inlined-deps.spade.git-sha-5197e54.container.dom
  "The DomStyleContainer renders styles into DOM elements. References to those
   elements are stored in a `styles` atom, or `*injected-styles*` if that is
   not provided. Similarly, if no `target-dom` is provided, the `document.head`
   element is used."
  (:require [day8.re-frame-10x.inlined-deps.spade.git-sha-5197e54.container :refer [IStyleContainer]]))

(defonce ^:dynamic *injected-styles* (atom nil))

(defn- perform-update! [obj css]
  (set! (.-innerHTML (:element obj)) css))

(defn update! [styles-container id css info]
  (swap! styles-container update id
         (fn update-injected-style [obj]
           (when-not (= (:source obj) css)
             (perform-update! obj css))
           (assoc obj :source css :info info))))

(defn inject! [target-dom styles-container id css info]
  (let [element (doto (js/document.createElement "style")
                  (.setAttribute "spade-id" (str id)))
        obj {:element element
             :source css
             :info info
             :id id}]
    (assert (some? target-dom)
            "An <head> element or target DOM is required to inject the style.")

    (.appendChild target-dom element)

    (swap! styles-container assoc id obj)
    (perform-update! obj css)))

(deftype DomStyleContainer [target-dom styles]
  IStyleContainer
  (mounted-info [_ style-name]
    (let [resolved-container (or styles
                                 *injected-styles*)]
      (:info (get @resolved-container style-name))))

  (mount-style! [_ style-name css info]
    (let [resolved-container (or styles
                                 *injected-styles*)]
      (if (contains? @resolved-container style-name)
        (update! resolved-container style-name css info)

        (let [resolved-dom (or (when (ifn? target-dom)
                                 (target-dom))
                               target-dom
                               (.-head js/document))]
          (inject! resolved-dom resolved-container style-name css info))))))

(defn create-container
  "Create a DomStyleContainer. With no args, the default is created, which
   renders into the `document.head` element. For rendering into a custom
   target, such as when using Shadow DOM, you may provide a custom
   `target-dom`: this may either be the element itself, or a function which
   returns that element.

   If you also wish to provide your own storage for the style references, you
   may use the 3-arity version and provide an atom."
  ([] (create-container nil))
  ([target-dom] (create-container target-dom (when target-dom
                                               (atom nil))))
  ([target-dom styles-container]
   (->DomStyleContainer target-dom styles-container)))
