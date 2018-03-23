### Event Code Tracing

This panel shows detailed, form-by-form trace for the `event handler` code. The (data) result for each `form` can be inspected. 

[TODO: Insert good example screenshot]

### Setup

Within your `project.clj`:

 1. Add `[day8.re-frame/debux "0.5.0-SNAPSHOT"]` to the `:dev` `:dependencies` section
 2. Add `"debux.cs.core.trace_enabled_QMARK_" true` to the `:closure-defines` section
 
Within the namespace containing the events to be traced (perhaps called `events.cljs`):

 1. Add the following `:require` to the `ns`:  `[debux.cs.core :refer-macros [fn-traced]]`
 2. When you register your event handler, use `fn-traced` instead of `fn`, like this: 
 
 ```clj
 (reg-event-db 
    :some-id
    (fn-traced [db event]   ;; <-- noite the use of `fn-traced` instead of `fn`
       ... code in here)))
 ```
