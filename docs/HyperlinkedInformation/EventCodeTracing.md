## Event Handler Tracing

Look deeply into the execution trace of your event handler.

This panel shows how your event handler "executed" - form by form. It allows you to inspect every argument and every return value, and it orients execution trace back to the original source code.

### Show Me

Source code for the event handler is show at the top, and the form by form exection trace is shown as row after row underneath. The indent level of the row indicates the nesting of the form being executed. When you mouse hover over a row of trace, it highlights the original source code.

![Estim8 demo](/docs/images/estim8-demo.png)

### Setup

**First**, adjust your `project.clj` by following [these instructions](https://github.com/Day8/re-frame-debux/blob/master/README.md#installation) to add `day8.re-frame/tracing` to the `:dev` `:dependencies`. 
 
**Then**, within the namespace of yours which contains the event handlers to be traced (perhaps called `events.cljs`):

 1. Add the following `:require` to the `ns`:  `[day8.re-frame.tracing :refer-macros [fn-traced]]`
 2. When you register your event handler, use `fn-traced` instead of `fn`, like this: 
 
 ```clj
 (reg-event-db 
    :some-id
    (fn-traced [db event]   ;; <-- notice the use of `fn-traced` instead of `fn`
       ... code in here))
 ```
