# Capturing Epochs

**Status:** proposed

## Context:

### Intro

Conceptually, re-frame is built around an event loop. The user makes an action, which causes an event to be dispatched, which changes app-db, which causes subscriptions to rerun, which causes the UI to update. We will refer to this cycle as an epoch. A user developing a re-frame application will want to be able to debug from this perspective.

Currently, re-frame-10x offers three panels: app-db, subs, and traces. Each of these offers a view into the application state and allows the programmer to build up a mental model of what is happening. They are not going to go away, but there is room for a more integrated and holistic panel.

### Requirements

The new panel is organised around epochs. Information is grouped by epochs, and the user can switch between different epochs.

### Defining

There are several ways of defining an epoch:

* Starting when an event was dispatched and ending when a new event is dispatched. - This doesn't work well when one event causes others to be dispatched. It also doesn't give you very accurate timing for how long an epoch as a whole takes to run.
* Starting when an event was dispatched and ending when a new event is dispatched that causes the router to start running again. - This handles one event dispatching several other events, but it doesn't give you overall timing.
* Starting when an event was dispatched, and ending when there is a period of no traces being fired. - This is based on heuristics, rather than actually measuring.
* Starting when an event was dispatched, including all subscriptions and renders that happened, and ending when there is no re-render scheduled in Reagent. - What about events that trigger a dispatch to run in 50 ms, or other async callbacks?

We also have an additional wrinkle. Traces which are produced outside of an epoch are added to a mini epoch. This is for collecting traces which occur when Reagent re-renders, like when a local ratom hover state changes. No events are dispatched, so it is not a real epoch, but it is still useful information. We also have Figwheel re-renders which don't dispatch an event, but do cause a re-render and subscription creations and deletions. Epoch's will need to have a source property that can distinguish between user clicks, callbacks, figwheel re-renders, intra-epoch renders, and possibly other sources.

### Capturing epochs

From a JavaScript perspective, there are three separate calls which make up an epoch:

1. The initial dispatch from an on-click handler or callback. This adds the event to the queue but doesn't process it, instead deferring processing until the next tick.
2. Processing the event and updating app-db
3. Rendering the UI, which includes creating, running, disposing of subscriptions; creating and evaluating Hiccup (including sorting/filtering data structures); and React rendering. These are all intermingled due to the way that Reagent works.

## Decision:

* Each epoch has only one event in it, and starts when an event is handled, if multiple events are processed in the same router queue (either because the first event dispatched the second, or that two events were concurrently added to the queue) they will be treated as multiple epochs.
* End of epoch is when there is no longer any work in the reagent queue

### First approach

The start of the epoch will be defined as any event trace being emitted. The end of the epoch will be either a new event trace being emitted, or a Reagent callback being called when nothing is scheduled. This is not completely correct, as a downstream event will create its own epoch, but it should be good enough to start building a useful UI and gain more information about the approach.

After this is built we can review our understanding of what an epoch is and iterate on a second approach.

## Consequences:

TBA
