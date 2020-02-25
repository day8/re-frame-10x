## Event Handler Tracing

This panel allows you to look deeply into the execution trace of your event handler.

With Clojure, execution happens form-by-form, so tracing Clojure code involves: 
  - showing what forms executed 
  - the order in which they executed 
  - the argument values for each 
  - the return value for each

The code in a Clojure function, like an event handler, is a tree of forms. Execution starts at a leaf form of this tree, and works its way up to higher and higher forms and ultimately the root of the tree. In the end, it is the form which is the body of the function which returns a value.

So, a trace will start with small, nested leaf forms and values, and will progressively involve higher and higher forms in the nesting.

This panel displays all this information for your observational pleasure and allows you to interact with it.

### Show Me

Source code for the event handler is shown at the top. 

Beneath that, the form by form execution trace is shown row after row - the first form executed is shown as the first row, then the next form executed is the next row, etc. The first form - the one at the top - will likely be somewhat small and nested. A leaf of the forms tree. The last form executed will be the entire body of the `fn`.

The indent level of a trace row indicates "the depth" of the form being executed from a source code point of view, and to assist with orientation, when you mouse-hover over a row of trace, the associated source code is highlighted within the display at the top.

![Estim8 demo](/docs/images/estim8-demo.png)

### Setup

**First**, adjust your `project.clj` by following [these instructions](https://github.com/day8/re-frame-debux/blob/master/README.md#installation) to add `day8.re-frame/tracing` to the `:dev` `:dependencies`. 
 
**Then**, within the namespace of yours which contains the event handlers to be traced (perhaps called `events.cljs`):

 1. Add the following `:require` to the `ns`:  `[day8.re-frame.tracing :refer-macros [fn-traced]]`
 2. When you register your event handler, use `fn-traced` instead of `fn`, like this: 
 
 ```clj
 (reg-event-db 
    :some-id
    (fn-traced [db event]   ;; <-- notice the use of `fn-traced` instead of `fn`
       ... code in here))
 ```
