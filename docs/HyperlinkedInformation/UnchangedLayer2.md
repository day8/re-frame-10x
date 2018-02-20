In the "subs" tab, you have the option to
ignore unchanged layer 2 subscriptions. This document explains why. 

### Background

`re-frame`
[makes a distinction](https://github.com/Day8/re-frame/blob/master/docs/SubscriptionInfographic.md)
between `layer 2` and `layer 3` subscriptions:
  - **`layer 2` subscriptions extract data directly from `app-db`**. They should be
    trivial in nature, which is to say there should be no computation in them beyond
    what is necessary to extract a value from `app-db`
  - **`layer 3` subscriptions never take values from `app-db`**. Instead, they have 
    `layer 2` nodes as inputs (or other `layer 3`), and they do the more serious CPU work of computing
    a materialised view of those values.  
    
    
We never want to run a `layer 3` subscriptions unless it is necessary, whereas `layer 2` 
subscriptions are so trivial that we don't mind running them unnecessarily. 

This structure delivers efficiency. You see, **all** (currently instantiated) `layer 2` subscriptions
**will run every time `app-db` changes in any way**. All of them. Every time.
And `app-db` changes on almost every event, so we want them to be computationally
trivial.

If the value of a `layer 2` subscription tests `=` to its previous value, then the further
propagation of values through the signal graph will be pruned.
The more computationally intensive `layer 3` subscriptions, and ultimately
the views, will only recompute if and when there has been a change in their data inputs.

We don't want your app recomputing views only to find that nothing has changed. That would be inefficient.

### Back To Tracing

Because `layer 2` subs run on every single modification of `app-db`, and because
very often nothing has changed, their trace can be a bit noisy.  Yes, it happened,
but it just isn't that interesting.

So `re-frame-10x` gives you the option of filtering out trace for
the `layer 2` subscriptions where the value "this time" is the same as the
value "last time".

On the other hand, if a `layer 2` subscription runs and its value is
different to last time, that's potentially fascinating and you'll want to
be told all about it. :-)

### Why do I sometimes see "Layer ?" when viewing a subscription?

To determine whether a subscription is a layer 2 or layer 3, re-frame-10x
looks at the input signals to a subscription. If one of the input signals is
app-db then the subscription is a layer 2 sub, otherwise it is a layer 3. If
a subscription hasn't run yet, then we can't know if it is a layer 2 or 3.

In almost all cases, a subscription will be created (by `(subscribe [:my-sub])`)
and run (by dereferencing the subscription)  within the same epoch, providing
the layer level. If you see "Layer ?" this means that a subscription was created
but not used. This may indicate a bug in your application, although there are
cases where this is ok.

In most cases, after a few more epochs, that subscription will have run, and we
know it's layer level, and can use it for any subscriptions shown on any future
(and past) epochs.
