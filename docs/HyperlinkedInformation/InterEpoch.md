# Inter Epoch Traces

> "Music is the space between the notes." - Claude Debussy

re-frame-trace is built around the idea of epochs. An epoch captures all of the traces that are emitted by re-frame after handling an event. But what about the traces that are emitted when re-frame *isn't* handling an event? These are the inter-epoch traces.

Inter-epoch traces are emitted under (at least) four circumstances:

* Mouse hover state causing subscriptions to run/re-run
* Click/input events sent to local ratoms (rather than as events to app-db)
* Figwheel disposing old subscriptions and re-rendering your application 
* Resetting app-db in re-frame-trace

The first two of these are essential to your application, the latter two are incidental to the tooling.

re-frame-trace collects any inter-epoch subscription traces and shows them in the subs panel with the next epoch. They are broken out into a separate section marked: "Inter-Epoch Subscriptions".
