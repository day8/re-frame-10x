### Event Handler Tracing

This panel provides an interactive, explorative UI through which you can browse the form-by-form execution trace of the ClojureScript code in your event handlers.  You can inspect the data produced by every ClojureScript form, while retaining overall context. 

### Show Me

Here's what it would look like if you had it setup:

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
