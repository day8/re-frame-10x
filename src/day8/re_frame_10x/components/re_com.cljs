(ns day8.re-frame-10x.components.re-com
  "Shameless pilfered from re-com."
  (:require-macros
    [day8.re-frame-10x.components.re-com :refer [handler-fn]])
  (:require
    [goog.object                                                 :as    gobj]
    [clojure.string                                              :as string]
    [day8.re-frame-10x.inlined-deps.reagent.v1v0v0.reagent.ratom :as reagent :refer [RAtom Reaction RCursor Track Wrapper]]
    [day8.re-frame-10x.inlined-deps.spade.git-sha-93ef290.core   :refer [defclass]]
    [reagent.impl.component                                      :as    component]
    [reagent.core                                                :as    r]))

(defn px
  "takes a number (and optional :negative keyword to indicate a negative value) and returns that number as a string with 'px' at the end"
  [val & negative]
  (str (if negative (- val) val) "px"))

(defn deref-or-value-peek
  "Takes a value or an atom
  If it's a value, returns it
  If it's a Reagent object that supports IDeref, returns the value inside it, but WITHOUT derefing
  The arg validation code uses this, since calling deref-or-value adds this arg to the watched ratom list for the component
  in question, which in turn can cause different rendering behaviour between dev (where we validate) and prod (where we don't).
  This was experienced in popover-content-wrapper with the position-injected atom which was not derefed there, however
  the dev-only validation caused it to be derefed, modifying its render behaviour and causing mayhem and madness for the developer.
  See below that different Reagent types have different ways of retrieving the value without causing capture, although in the case of
  Track, we just deref it as there is no peek or state, so hopefully this won't cause issues (surely this is used very rarely).
  "
  [val-or-atom]
  (if (satisfies? IDeref val-or-atom)
    (cond
      (instance? RAtom val-or-atom) val-or-atom.state
      (instance? Reaction val-or-atom) (._peek-at val-or-atom)
      (instance? RCursor val-or-atom) (._peek val-or-atom)
      (instance? Track val-or-atom) @val-or-atom
      (instance? Wrapper val-or-atom) val-or-atom.state
      :else (throw (js/Error. "Unknown reactive data type")))
    val-or-atom))

(defn deref-or-value
  "Takes a value or an atom
  If it's a value, returns it
  If it's a Reagent object that supports IDeref, returns the value inside it by derefing
  "
  [val-or-atom]
  (if (satisfies? IDeref val-or-atom)
    @val-or-atom
    val-or-atom))

(defn deep-merge
  "Recursively merges maps. If vals are not maps, the last value wins."
  [& vals]
  (if (every? map? vals)
    (apply merge-with deep-merge vals)
    (last vals)))

(defclass flex-style
  []
  {:display :flex})

(defclass inline-flex-style
  []
  {:display :flex})

(defn flex-flow-style
  "A cross-browser helper function to output flex-flow with all it's potential browser prefixes"
  [flex-flow]
  {:-webkit-flex-flow flex-flow
   :flex-flow         flex-flow})

(defn flex-child-style
  "Determines the value for the 'flex' attribute (which has grow, shrink and basis), based on the :size parameter.
   IMPORTANT: The term 'size' means width of the item in the case of flex-direction 'row' OR height of the item in the case of flex-direction 'column'.
   Flex property explanation:
    - grow    Integer ratio (used with other siblings) to determined how a flex item grows it's size if there is extra space to distribute. 0 for no growing.
    - shrink  Integer ratio (used with other siblings) to determined how a flex item shrinks it's size if space needs to be removed. 0 for no shrinking.
    - basis   Initial size (width, actually) of item before any growing or shrinking. Can be any size value, e.g. 60%, 100px, auto
              Note: auto will cause the initial size to be calculated to take up as much space as possible, in conjunction with it's siblings :flex settings.
   Supported values:
    - initial            '0 1 auto'  - Use item's width/height for dimensions (or content dimensions if w/h not specifed). Never grow. Shrink (to min-size) if necessary.
                                       Good for creating boxes with fixed maximum size, but that can shrink to a fixed smaller size (min-width/height) if space becomes tight.
                                       NOTE: When using initial, you should also set a width/height value (depending on flex-direction) to specify it's default size
                                             and an optional min-width/height value to specify the size it can shrink to.
    - auto               '1 1 auto'  - Use item's width/height for dimensions. Grow if necessary. Shrink (to min-size) if necessary.
                                       Good for creating really flexible boxes that will gobble as much available space as they are allowed or shrink as much as they are forced to.
    - none               '0 0 auto'  - Use item's width/height for dimensions (or content dimensions if not specifed). Never grow. Never shrink.
                                       Good for creating rigid boxes that stick to their width/height if specified, otherwise their content size.
    - 100px              '0 0 100px' - Non flexible 100px size (in the flex direction) box.
                                       Good for fixed headers/footers and side bars of an exact size.
    - 60%                '60 1 0px'  - Set the item's size (it's width/height depending on flex-direction) to be 60% of the parent container's width/height.
                                       NOTE: If you use this, then all siblings with percentage values must add up to 100%.
    - 60                 '60 1 0px'  - Same as percentage above.
    - grow shrink basis  'grow shrink basis' - If none of the above common valaues above meet your needs, this gives you precise control.
   If number of words is not 1 or 3, an exception is thrown.
   Reference: http://www.w3.org/TR/css3-flexbox/#flexibility
   Diagram:   http://www.w3.org/TR/css3-flexbox/#flex-container
   Regex101 testing: ^(initial|auto|none)|(\\d+)(px|%|em)|(\\d+)\\w(\\d+)\\w(.*) - remove double backslashes"
  [size]
  ;; TODO: Could make initial/auto/none into keywords???
  (let [split-size      (string/split (string/trim size) #"\s+") ;; Split into words separated by whitespace
        split-count     (count split-size)
        _               (assert (contains? #{1 3} split-count) "Must pass either 1 or 3 words to flex-child-style")
        size-only       (when (= split-count 1) (first split-size)) ;; Contains value when only one word passed (e.g. auto, 60px)
        split-size-only (when size-only (string/split size-only #"(\d+)(.*)")) ;; Split into number + string
        [_ num units] (when size-only split-size-only)      ;; grab number and units
        pass-through?   (nil? num)                          ;; If we can't split, then we'll pass this straign through
        grow-ratio?     (or (= units "%") (= units "") (nil? units)) ;; Determine case for using grow ratio
        grow            (if grow-ratio? num "0")            ;; Set grow based on percent or integer, otherwise no grow
        shrink          (if grow-ratio? "1" "0")            ;; If grow set, then set shrink to even shrinkage as well
        basis           (if grow-ratio? "0px" size)         ;; If grow set, then even growing, otherwise set basis size to the passed in size (e.g. 100px, 5em)
        flex            (if (and size-only (not pass-through?))
                          (str grow " " shrink " " basis)
                          size)]
    {:-webkit-flex flex
     :flex         flex}))

(defn display-flex-style
  []
  {:display :flex})

(defn justify-style
  "Determines the value for the flex 'justify-content' attribute.
   This parameter determines how children are aligned along the main axis.
   The justify parameter is a keyword.
   Reference: http://www.w3.org/TR/css3-flexbox/#justify-content-property"
  [justify]
  (let [js (case justify
             :start "flex-start"
             :end "flex-end"
             :center "center"
             :between "space-between"
             :around "space-around")]
    {:-webkit-justify-content js
     :justify-content         js}))


(defn align-style
  "Determines the value for the flex align type attributes.
   This parameter determines how children are aligned on the cross axis.
   The justify parameter is a keyword.
   Reference: http://www.w3.org/TR/css3-flexbox/#align-items-property"
  [attribute align]
  (let [attribute-wk (->> attribute name (str "-webkit-") keyword)
        as           (case align
                       :start "flex-start"
                       :end "flex-end"
                       :center "center"
                       :baseline "baseline"
                       :stretch "stretch")]
    {attribute-wk as
     attribute    as}))

(defn gap-f
  "Returns a component which produces a gap between children in a v-box/h-box along the main axis"
  [& {:keys [size width height class style attr]}]
  (let [s (merge
            (when size (flex-child-style size))
            (when width {:width width})
            (when height {:height height})
            style)]
    [:div
     (merge
       {:class (str "rc-gap " class) :style s}
       attr)]))

(defn h-box
  "Returns hiccup which produces a horizontal box.
   It's primary role is to act as a container for components and lays it's children from left to right.
   By default, it also acts as a child under it's parent"
  [& {:keys [size width height min-width min-height max-width max-height justify align align-self margin padding gap children class style attr]
      :or   {size "none" justify :start align :stretch}}]
  (let [s        (merge
                   (flex-flow-style "row nowrap")
                   (flex-child-style size)
                   (when width {:width width})
                   (when height {:height height})
                   (when min-width {:min-width min-width})
                   (when min-height {:min-height min-height})
                   (when max-width {:max-width max-width})
                   (when max-height {:max-height max-height})
                   (justify-style justify)
                   (align-style :align-items align)
                   (when align-self (align-style :align-self align-self))
                   (when margin {:margin margin})       ;; margin and padding: "all" OR "top&bottom right&left" OR "top right bottom left"
                   (when padding {:padding padding})
                   style)
        gap-form (when gap [gap-f
                            :size gap
                            :width gap]) ;; TODO: required to get around a Chrome bug: https://code.google.com/p/chromium/issues/detail?id=423112. Remove once fixed.
        children (if gap
                   (interpose gap-form (filter identity children)) ;; filter is to remove possible nils so we don't add unwanted gaps
                   children)]
    (into [:div
           (merge
             {:class (str "rc-h-box " (flex-style) " " class) :style s}
             attr)]
          children)))

(defn v-box
  "Returns hiccup which produces a vertical box.
   It's primary role is to act as a container for components and lays it's children from top to bottom.
   By default, it also acts as a child under it's parent"
  [& {:keys [size width height min-width min-height max-width max-height justify align align-self margin padding gap children class style attr]
      :or   {size "none" justify :start align :stretch}}]
  (let [s        (merge
                   (flex-flow-style "column nowrap")
                   (flex-child-style size)
                   (when width {:width width})
                   (when height {:height height})
                   (when min-width {:min-width min-width})
                   (when min-height {:min-height min-height})
                   (when max-width {:max-width max-width})
                   (when max-height {:max-height max-height})
                   (justify-style justify)
                   (align-style :align-items align)
                   (when align-self (align-style :align-self align-self))
                   (when margin {:margin margin})       ;; margin and padding: "all" OR "top&bottom right&left" OR "top right bottom left"
                   (when padding {:padding padding})
                   style)
        gap-form (when gap [gap-f
                            :size gap
                            :height gap]) ;; TODO: required to get around a Chrome bug: https://code.google.com/p/chromium/issues/detail?id=423112. Remove once fixed.
        children (if gap
                   (interpose gap-form (filter identity children)) ;; filter is to remove possible nils so we don't add unwanted gaps
                   children)]
    (into [:div
           (merge
             {:class (str "rc-v-box " (flex-style) " " class) :style s}
             attr)]
          children)))

(defn scroll-style
  "Determines the value for the 'overflow' attribute.
   The scroll parameter is a keyword.
   Because we're translating scroll into overflow, the keyword doesn't appear to match the attribute value"
  [attribute scroll]
  {attribute (case scroll
               :auto "auto"
               :off "hidden"
               :on "scroll"
               :spill "visible")})


(defn- box-base
  "This should generally NOT be used as it is the basis for the box, scroller and border components"
  [& {:keys [size scroll h-scroll v-scroll width height min-width min-height max-width max-height justify align align-self
             margin padding border l-border r-border t-border b-border radius bk-color child class-name class style attr]}]
  (let [s (merge
            (flex-flow-style "inherit")
            (flex-child-style size)
            (when scroll (scroll-style :overflow scroll))
            (when h-scroll (scroll-style :overflow-x h-scroll))
            (when v-scroll (scroll-style :overflow-y v-scroll))
            (when width {:width width})
            (when height {:height height})
            (when min-width {:min-width min-width})
            (when min-height {:min-height min-height})
            (when max-width {:max-width max-width})
            (when max-height {:max-height max-height})
            (when justify (justify-style justify))
            (when align (align-style :align-items align))
            (when align-self (align-style :align-self align-self))
            (when margin {:margin margin})       ;; margin and padding: "all" OR "top&bottom right&left" OR "top right bottom left"
            (when padding {:padding padding})
            (when border {:border border})
            (when l-border {:border-left l-border})
            (when r-border {:border-right r-border})
            (when t-border {:border-top t-border})
            (when b-border {:border-bottom b-border})
            (when radius {:border-radius radius})
            (when bk-color
              {:background-color bk-color})
            style)]
    [:div
     (merge
       {:class (str class-name " " (flex-style) " " class) :style s}
       attr)
     child]))

(defn box
  "Returns hiccup which produces a box, which is generally used as a child of a v-box or an h-box.
   By default, it also acts as a container for further child compenents, or another h-box or v-box"
  [& {:keys [size scroll h-scroll v-scroll width height min-width min-height max-width max-height justify align align-self margin padding child class style attr]
      :or   {size "none"}}]
  (box-base :size size
            :scroll scroll
            :h-scroll h-scroll
            :v-scroll v-scroll
            :width width
            :height height
            :min-width min-width
            :min-height min-height
            :max-width max-width
            :max-height max-height
            :justify justify
            :align align
            :align-self align-self
            :margin margin
            :padding padding
            :child child
            :class-name "rc-box "
            :class class
            :style style
            :attr attr))

(defn line
  "Returns a component which produces a line between children in a v-box/h-box along the main axis.
   Specify size in pixels and a stancard CSS color. Defaults to a 1px lightgray line"
  [& {:keys [size color class style attr]
      :or   {size "1px" color "lightgray"}}]
  (let [s (merge
            (flex-child-style (str "0 0 " size))
            {:background-color color}
            style)]
    [:div
     (merge
       {:class (str "rc-line " class) :style s}
       attr)]))

(defn- input-text-base
  "Returns markup for a basic text input label"
  [& {:keys [model input-type]}]
  (let [external-model (reagent/atom (deref-or-value model)) ;; Holds the last known external value of model, to detect external model changes
        internal-model (reagent/atom (if (nil? @external-model) "" @external-model))] ;; Create a new atom from the model to be used internally (avoid nil)
    (fn
      [& {:keys [model on-change on-submit status status-icon? placeholder width height rows change-on-blur? validation-regex disabled? class style attr]
          :or   {change-on-blur? true}}]
      (let [latest-ext-model (deref-or-value model)
            disabled?        (deref-or-value disabled?)
            change-on-blur?  (deref-or-value change-on-blur?)]
        (when (not= @external-model latest-ext-model) ;; Has model changed externally?
          (reset! external-model latest-ext-model)
          (reset! internal-model latest-ext-model))
        [h-box
         :class "rc-input-text "
         :align :start
         :width (if width width "250px")
         :children [[:div
                     {:class (str "rc-input-text-inner "          ;; form-group
                                  (case status
                                    :success "has-success "
                                    :warning "has-warning "
                                    :error "has-error "
                                    "")
                                  (when (and status status-icon?) "has-feedback"))
                      :style (flex-child-style "auto")}
                     [(if (= input-type :password) :input input-type)
                      (merge
                        {:class       (str "form-control " class)
                         :type        (case input-type
                                        :input "text"
                                        :password "password"
                                        nil)
                         :rows        (when (= input-type :textarea) (or rows 3))
                         :style       (merge
                                        (flex-child-style "none")
                                        {:height        height
                                         :box-sizing    "border-box" ;; TODO: Added to override the incorrect default of "content-box" somehow set up by :all unset
                                         :padding-right "12px"} ;; override for when icon exists
                                        style)
                         :placeholder placeholder
                         :value       @internal-model
                         :disabled    disabled?
                         :on-change   (fn [event]
                                        (let [new-val (-> event .-target .-value)]
                                          (when (and
                                                  on-change
                                                  (not disabled?)
                                                  (if validation-regex (re-find validation-regex new-val) true))
                                            (reset! internal-model new-val)
                                            (when-not change-on-blur?
                                              (on-change @internal-model)))))
                         :on-blur     (fn [_]
                                        (when (and
                                                on-change
                                                change-on-blur?
                                                (not= @internal-model @external-model))
                                          (on-change @internal-model)))
                         :on-key-down (fn [event]
                                        (case (.-which event)
                                          13 (when on-submit
                                               (on-submit @internal-model))
                                          true))
                         :on-key-up   (fn [event]
                                        (if disabled?
                                          (.preventDefault event)
                                          (case (.-which event)
                                            #_#_13 (when on-change (on-change @internal-model))
                                            27 (reset! internal-model @external-model)
                                            true)))}
                        attr)]]]]))))


(defn input-text
  [& args]
  (apply input-text-base :input-type :input args))

(defn label
  "Returns markup for a basic label"
  [& {:keys [label on-click width class style attr]}]
  [box
   :class (str "rc-label-wrapper " (inline-flex-style))
   :width width
   :align :start
   :child [:span
           (merge
             {:class (str "rc-label " class)
              :style (merge (flex-child-style "none")
                            style)}
             (when on-click
               {:on-click (handler-fn (on-click))})
             attr)
           label]])

(defn p
  "acts like [:p ]
   Creates a paragraph of body text, expected to have a font-szie of 14px or 15px,
   which should have limited width.
   Why limited text width?  See http://baymard.com/blog/line-length-readability
   The actual font-size is inherited.
   At 14px, 450px will yield between 69 and 73 chars.
   At 15px, 450px will yield about 66 to 70 chars.
   So we're at the upper end of the prefered 50 to 75 char range.
   If the first child is a map, it is interpreted as a map of styles / attributes."
  [& children]
  (let [child1 (first children)    ;; it might be a map of attributes, including styles
        [m children] (if (map? child1)
                       [child1 (rest children)]
                       [{} children])
        m      (deep-merge {:style {:flex          "none"
                                    :width         "450px"
                                    :min-width     "450px"
                                    :margin-bottom "10px"}}
                           m)]
    [:span
     m
     (into [:p] children)]))    ;; the wrapping span allows children to contain [:ul] etc

(defn button
  "Returns the markup for a basic button"
  []
  (let [showing? (reagent/atom false)]
    (fn
      [& {:keys [label on-click disabled? class style attr]
          :or   {class "btn-default"}}]
      (let [disabled?  (deref-or-value disabled?)
            the-button [:button
                        (merge
                          {:class    (str "rc-button btn noselect " class)
                           :style    (merge
                                       (flex-child-style "none")
                                       style)
                           :disabled disabled?
                           :on-click (fn [event]
                                       (when (and on-click (not disabled?))
                                         (on-click event)))}
                          attr)
                        label]]
        (when disabled?
          (reset! showing? false))
        [box ;; Wrapper box is unnecessary but keeps the same structure as the re-com button
         :class (str "rc-button-wrapper " (inline-flex-style))
         :align :start
         :child the-button]))))

(defn hyperlink
  "Renders an underlined text hyperlink component.
   This is very similar to the button component above but styled to looks like a hyperlink.
   Useful for providing button functionality for less important functions, e.g. Cancel"
  [& {:keys [label on-click disabled? class style attr]}]
  (let [label      (deref-or-value label)
        disabled?  (deref-or-value disabled?)
        the-button [box
                    :align :start
                    :child [:a
                            (merge
                              {:class    (str "rc-hyperlink noselect " class)
                               :style    (merge
                                           (flex-child-style "none")
                                           {:cursor (if disabled? "not-allowed" "pointer")
                                            :color  (when disabled? "grey")}
                                           style)
                               :on-click (fn [event]
                                           (when (and on-click (not disabled?))
                                             (on-click event)))}
                              attr)
                            label]]]
    [box
     :class (str "rc-hyperlink-wrapper " (inline-flex-style))
     :align :start
     :child the-button]))

(defn hyperlink-href
  "Renders an underlined text hyperlink component.
   This is very similar to the button component above but styled to looks like a hyperlink.
   Useful for providing button functionality for less important functions, e.g. Cancel"
  []
  (let [showing? (reagent/atom false)]
    (fn
      [& {:keys [label href target tooltip class style attr]}]
      (when-not tooltip (reset! showing? false)) ;; To prevent tooltip from still showing after button drag/drop
      (let [label      (deref-or-value label)
            href       (deref-or-value href)
            target     (deref-or-value target)
            the-button [:a
                        (merge {:class  (str "rc-hyperlink-href noselect " class)
                                :style  (merge (flex-child-style "none")
                                               style)
                                :href   href
                                :target target}
                               (when tooltip
                                 {:on-mouse-over (handler-fn (reset! showing? true))
                                  :on-mouse-out  (handler-fn (reset! showing? false))})
                               attr)
                        label]]

        [box
         :class (str "rc-hyperlink-href-wrapper " (inline-flex-style))
         :align :start
         :child the-button]))))

(defn checkbox
  "I return the markup for a checkbox, with an optional RHS label"
  [& {:keys [model on-change label disabled? label-class label-style class style attr]}]
  (let [cursor      "default"
        model       (deref-or-value model)
        disabled?   (deref-or-value disabled?)
        callback-fn #(when (and on-change (not disabled?))
                       (on-change (not model)))]  ;; call on-change with either true or false
    [h-box
     :class "rc-checkbox-wrapper noselect"
     :align :start
     :children [[:input
                 (merge
                   {:class     (str "rc-checkbox " class)
                    :type      "checkbox"
                    :style     (merge (flex-child-style "none")
                                      {:cursor cursor}
                                      style)
                    :disabled  disabled?
                    :checked   (boolean model)
                    :on-change (handler-fn (callback-fn))}
                   attr)]
                (when label
                  [:span
                   {:class    label-class
                    :style    (merge (flex-child-style "none")
                                     {:padding-left "8px"
                                      :cursor       cursor}
                                     label-style)
                    :on-click (handler-fn (callback-fn))}
                   label])]]))

(defn radio-button
  "I return the markup for a radio button, with an optional RHS label"
  [& {:keys [model value on-change label disabled? label-class label-style class style attr]}]
  (let [cursor      "default"
        model       (deref-or-value model)
        disabled?   (deref-or-value disabled?)
        callback-fn #(when (and on-change (not disabled?))
                      (on-change value))]  ;; call on-change with the :value arg
    [h-box
     :class    (str "noselect rc-radio-button-wrapper")
     :align    :start
     :children [[:input
                 (merge
                   {:class     (str "rc-radio-button " class)
                    :style     (merge
                                 (flex-child-style "none")
                                 {:cursor cursor}
                                 style)
                    :type      "radio"
                    :disabled  disabled?
                    :checked   (= model value)
                    :on-change (handler-fn (callback-fn))}
                   attr)]
                (when label
                  [:span
                   {:class    label-class
                    :style    (merge (flex-child-style "none")
                                     {:padding-left "8px"
                                      :cursor       cursor}
                                     label-style)
                    :on-click (handler-fn (callback-fn))}
                   label])]]))

;; Taken from day8.briefly.components.close-button
(defn close-button
  []
  (let [over? (reagent/atom false)]
    (fn close-button-render
      [& {:keys [on-click div-size font-size color hover-color tooltip top-offset left-offset style attr]
          :or   {div-size 16 font-size 16 color "#ccc" hover-color "#999"}}]
      [box
       :class "rc-close-button noselect"
       :style {:display  "inline-block"
               :position "relative"
               :width    (px div-size)
               :height   (px div-size)}
       :child [box
               :style (merge
                        {:position  "absolute"
                         :cursor    "pointer"
                         :font-size (px font-size)
                         :color     (if @over? hover-color color)
                         :top       (px (- (/ (- font-size div-size) 2) top-offset) :negative)
                         :left      (px (- (/ (- font-size div-size) 2) left-offset) :negative)}
                        style)
               :attr (merge
                       {:title          tooltip
                        :on-click       (fn [event] (on-click)
                                                    (.stopPropagation event))
                        :on-mouse-enter (handler-fn (reset! over? true))
                        :on-mouse-leave (handler-fn (reset! over? false))}
                       attr)
               ;:child [:i {:class "zmdi zmdi-hc-fw-rc zmdi zmdi-close"}]
               :child [:span "×"]]])))

(defn css-join
  "Creates a single string from all passed args, separated by spaces (all args are coerced to strings)
  Very simple, but handy
  e.g. {:padding (css-join common/gs-12s (px 25))}"
  [& args]
  (clojure.string/join " " args))

(defn get-element-by-id
  [id]
  (.getElementById js/document id))

(defn drag-handle
  "Return a drag handle to go into a vertical or horizontal splitter bar:
    orientation: Can be :horizontal or :vertical
    over?:       When true, the mouse is assumed to be over the splitter so show a bolder color"
  [orientation over? parts]
  (let [vertical? (= orientation :vertical)
        length    "20px"
        width     "8px"
        pos1      "3px"
        pos2      "3px"
        color     (if over? "#999" "#ccc")
        border    (str "solid 1px " color)
        flex-flow (str (if vertical? "row" "column") " nowrap")]
    [:div
     (merge
       {:class (str "rc-" (if vertical? "v" "h") "-split-handle display-flex " (get-in parts [:handle :class]))
        :style (merge (flex-flow-style flex-flow)
                      {:width  (if vertical? width length)
                       :height (if vertical? length width)
                       :margin "auto"}
                      (get-in parts [:handle :style]))}
       (get-in parts [:handle :attr]))
     [:div
      (merge
        {:class (str "rc-" (if vertical? "v" "h") "-split-handle-bar-1 " (get-in parts [:handle-bar-1 :class]))
         :style (merge
                  (if vertical?
                    {:width pos1   :height length :border-right  border}
                    {:width length :height pos1   :border-bottom border})
                  (get-in parts [:handle-bar-1 :style]))}
        (get-in parts [:handle-bar-1 :attr]))]
     [:div
      (merge
        {:class (str "rc-" (if vertical? "v" "h") "-split-handle-bar-2 " (get-in parts [:handle-bar-2 :class]))
         :style (merge
                  (if vertical?
                    {:width pos2   :height length :border-right  border}
                    {:width length :height pos2   :border-bottom border})
                  (get-in parts [:handle-bar-2 :style]))}
        (get-in parts [:handle-bar-2 :attr]))]]))

(defn v-split
  "Returns markup for a vertical layout component"
  [& {:keys [size width height split-is-px? on-split-change initial-split splitter-size margin src debug?]
      :or   {size "auto" initial-split 50 splitter-size "8px" margin "8px"}
      :as   args}]
  (let [container-id         (gensym "v-split-")
        split-perc           (reagent/atom (js/parseInt initial-split))  ;; splitter position as a percentage of height
        dragging?            (reagent/atom false)                        ;; is the user dragging the splitter (mouse is down)?
        over?                (reagent/atom false)                        ;; is the mouse over the splitter, if so, highlight it

        stop-drag            (fn []
                               (when on-split-change (on-split-change @split-perc))
                               (reset! dragging? false))

        calc-perc            (fn [event]                                                                ;; turn a mouse y coordinate into a percentage position
                               (let [get-v-split-from-click (fn [event-target]
                                                              (loop [target        event-target
                                                                     nesting-level 0]
                                                                (let [class-name  (-> target .-className)
                                                                      id-name     (-> target .-id)
                                                                      is-v-split? (and (clojure.string/includes? class-name "rc-v-split 10x-v-split")
                                                                                       (clojure.string/includes? id-name "v-split-"))]
                                                                  (cond
                                                                    is-v-split? target
                                                                    (> target 3) nil
                                                                    :else (recur (.-parentElement target) (inc nesting-level))))))
                                     mouse-y                (.-clientY event)
                                     target                 (get-v-split-from-click (.-target event))
                                     container              (if target
                                                              target
                                                              (get-element-by-id container-id)) ;; the outside container
                                     c-height               (.-clientHeight container) ;; the container's height
                                     c-top-y                (+ (.-pageYOffset js/window)
                                                               (-> container .getBoundingClientRect .-top)) ;; the container's top Y
                                     relative-y             (- mouse-y c-top-y)] ;; the Y of the mouse, relative to container
                                 (if split-is-px?
                                   relative-y                                              ;; return the top offset in px
                                   (* 100.0 (/ relative-y c-height)))))                    ;; return the percentage panel-1 height against container width

        <html>?              #(= % (.-documentElement js/document))                        ;; test for the <html> element

        mouseout             (fn [event]
                               (if (<html>? (.-relatedTarget event))                       ;; stop drag if we leave the <html> element
                                 (stop-drag)))

        mousemove            (fn [event]
                               (reset! split-perc (calc-perc event)))

        mousedown            (fn [event]
                               (.preventDefault event)                                    ;; stop selection of text during drag
                               (reset! dragging? true))

        mouseover-split      #(reset! over? true)
        mouseout-split       #(reset! over? false)

        make-container-attrs (fn [class style attr in-drag?]
                               (merge {:class (str "rc-v-split 10x-v-split " class)
                                       :id    container-id
                                       :style (merge (display-flex-style)
                                                     (flex-child-style size)
                                                     (flex-flow-style "column nowrap")
                                                     {:margin margin
                                                      :width  width
                                                      :height height}
                                                     style)}
                                      (when in-drag?                             ;; only listen when we are dragging
                                        {:on-mouse-up   (handler-fn (stop-drag))
                                         :on-mouse-move (handler-fn (mousemove event))
                                         :on-mouse-out  (handler-fn (mouseout event))})
                                      attr))

        make-panel-attrs     (fn [class style attr in-drag? percentage]
                               (merge
                                 {:class class
                                  :style (merge (display-flex-style)
                                                (flex-child-style (if split-is-px?
                                                                    (if (pos? percentage)
                                                                      (str "0 0 " percentage "px") ;; flex for panel-1
                                                                      (str "1 1 0px"))             ;; flex for panel-2
                                                                    (str percentage " 1 0px")))
                                                (when in-drag? {:pointer-events "none"})
                                                style)}
                                 attr))

        make-splitter-attrs  (fn [class style attr]
                               (merge
                                 {:class         class
                                  :on-mouse-down (handler-fn (mousedown event))
                                  :on-mouse-over (handler-fn (mouseover-split))
                                  :on-mouse-out  (handler-fn (mouseout-split))
                                  :style         (merge (display-flex-style)
                                                        (flex-child-style (str "0 0 " splitter-size))
                                                        {:cursor  "row-resize"}
                                                        (when @over? {:background-color "#f8f8f8"})
                                                        style)}
                                 attr))]

    (fn v-split-render
      [& {:keys [panel-1 panel-2 _size _width _height _on-split-change _initial-split _splitter-size _margin class style attr parts src]}]
      [:div (make-container-attrs class style attr @dragging?)
       [:div (make-panel-attrs
               (str "rc-v-split-top " (get-in parts [:top :class]))
               (get-in parts [:top :style])
               (get-in parts [:top :attr])
               @dragging?
               @split-perc)
        panel-1]
       [:div (make-splitter-attrs
               (str "rc-v-split-splitter " (get-in parts [:splitter :class]))
               (get-in parts [:splitter :style])
               (get-in parts [:splitter :attr]))
        [drag-handle :horizontal @over? parts]]
       [:div (make-panel-attrs
               (str "rc-v-split-bottom " (get-in parts [:bottom :class]))
               (get-in parts [:bottom :style])
               (get-in parts [:bottom :attr])
               @dragging?
               (if split-is-px?
                 (- @split-perc) ;; Negative value indicates this is for panel-2
                 (- 100 @split-perc)))
        panel-2]])))