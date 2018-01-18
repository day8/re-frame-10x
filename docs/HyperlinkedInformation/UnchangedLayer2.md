
This document briefly explains why `re-frame-trace` gives you an option to 
ignore unchanged layer 2 subscriptions. 

### Background

The `re-frame` docs 
[make a distinction](https://github.com/Day8/re-frame/blob/master/docs/SubscriptionInfographic.md)
between `layer 2` and `layer 3` subscriptions:
  - `layer 2` subscriptions extract data directly from `app-db` and should be
    trivial in nature. There should be no computation in them beyond
    what is necessary to extract a value from `app-db`
  - `layer 3` subscriptions take values from `layer 2` nodes as inputs, and 
    compute a materialised view of those values. Just to repeat: they never directly 
    extract values from `app-db`.  They create new values where necessary, and because of it
    they to do more serious CPU work. So we never want to run a
    `layer 3` subscriptions unless it is necessary. 
    
This structure delivers efficiency. You see, **all** (currently instantiated) `layer 2` subscriptions 
will run **every** time `app-db` changes in any way. All of them. Every time.
And `app-db` changes on almost every event, so we want them to be computationally 
trivial. 

If the value of a `layer 2` subscription tests `=` to its previous value, then the further
propagation of values through the signal graph will be pruned.
The more computationally intensive `layer 3` subscriptions, and ultimately
the views, will only recompute if and when there has been a change in their data inputs.

We don't want your app recomputing views only to find that nothing has changed. Inefficient. 

### Back To Tracing

Because `layer 2` subs run on every single modification of `app-db`, and because
very often nothing has changed, their trace can be a bit noisy.  Yes, it happened, 
but it just isn't that interesting.

So `re-frame-trace` gives you the option of filtering out trace for 
the `layer 2` subscriptions where the value "this time" is the same as the 
value "last time".

On the other hand, if a `layer 2` subscription runs and its value is 
different to last time, that's potentially fascinating and you'll want to 
be told all about it. :-)   
