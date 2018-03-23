### Event Handler Tracing

This panel shows detailed, form-by-form trace for the (ClojureScript) code in the `event handler`.  The (data) result for each `form` can be inspected.

<!-- good indicative screenshot of the panel in here -->

### Setup

**First**, adjust your `project.clj` by following [these instructions](https://github.com/Day8/re-frame-debux/tree/2537b7e0818bb147d5da326b865ea2f9d93f5c73#3-installation) to add `day8.re-frame/debux` to the `:dev` `:dependencies`. 
 
**Then**, within the namespace of yours which contains the event handlers to be traced (perhaps called `events.cljs`):

 1. Add the following `:require` to the `ns`:  `[debux.cs.core :refer-macros [fn-traced]]`
 2. When you register your event handler, use `fn-traced` instead of `fn`, like this: 
 
 ```clj
 (reg-event-db 
    :some-id
    (fn-traced [db event]   ;; <-- notice the use of `fn-traced` instead of `fn`
       ... code in here)))
 ```
