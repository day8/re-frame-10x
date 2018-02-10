# Intra Epoch Traces

> "Music is the space between the notes." - Claude Debussy

`re-frame-trace` is built around the idea of epochs. An epoch captures all of the trace emitted by re-frame during the handling an event. But what about the trace emitted when re-frame *isn't* handling an event? These are the intra-epoch traces.

Intra-epoch traces are emitted under (at least) three circumstances:

* Views sometimes have local state in the form of local ratoms which control, for example, the appearance of popups or mouseover annimations. When the state of these ratoms change, dependent views will likely rerender, and the trace to do with this rerendering will flow from reagent to `re-frame`trace`. But there'll be no "current" epoch to put it in.  Because there was no event (just a pseudo event caused by the ratom reset)
* A Figwheel recompile and subsequent code reload will cause all existing subscriptions to be destroyed, andwill trigger a complete re-render of your application
* `re-frame-trace` itself resetting the value in `app-db`. Again there will be a flurry of subscription and view trace, but no epoch, because there was no event.

The first of these is essential to the functioning of your application, whidh makes their trace interesting and informative. The last two are incidental to the tooling and are probably best ignored. Probably.

`re-frame-trace` collects any intra-epoch subscription traces and shows them in the subs panel with the next epoch. They are broken out into a separate section marked: "Intra-Epoch Subscriptions".
