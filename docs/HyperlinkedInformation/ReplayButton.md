This document explains the operation of the "Replay" button, and how to use **The HotPlay Workflow**.

### Epoch Navigation 

By using the backwards and forwards arrows, `re-frame-trace` allows you to navigate through Epochs.
At any one moment, you are inspecting a single Epoch, for which we'll use the term "The Observed Epoch".

### On Click

When you click the "Replay" button, you are asking `re-frame-trace` to perform
an "Action Replay" of "The Observed Epoch", and this happens in two Steps: 
 - **Step 1** - the value in `app-db` is reset to the value it contained immediately prior to "The Observed Epoch"
 - **Step 2** - the event which caused "The Observed Epoch" is re-dispatched
 
So, Step 1 is "reestablish initial conditions" and Step 2 is "do it all again".

### Further Notes:
  - In Step 1, the reset of `app-db` will trigger dominoes 4,5,6, causing
    subscriptions and views to run as the application returns to the "prior state", 
    but none of the trace for this is captured. It is all ignored.
  - All trace arising in Step 2 forms a normal, new Epoch. The (original) Observed Epoch (which we are replaying) 
    is still there, untouched.
  - The new Epoch (Step 2) is added to the end of the existing Epoch list. It becomes the most recent Epoch. 
  - The user's experience is that they click the "Replay" button 
    and immediately view the result. So, after Step 2, the user is auto-navigated to this new epoch.

### Useful? 

It facilitates "The HotPlay Workflow": 
  - A. You `Observe` an Epoch (in `re-frame-trace`) to see if an event was correctly processed 
  - B. You notice a problem, and you `Edit` (correct) the event handler (or subs handler, view, etc) via Cursive, Emacs, etc.
  - C. Figwheel will re-compile and `Hotload` your correction
  - D: You click the `Replay` button
  - E: Back to A

Because of `Replay's` "Step 1", you effortlessly get identical "initial conditions" 
each iteration of the workflow, which removes a lot of incidental cognative load
(struggling around trying to put the application back into the right state for each new iteration)
and keeps the iterations snappy. Flow.

WARNING: obviously this works best when the state is contained in your SPA. When 
there's remote state (a remote database?) it gets trickier to return to initial conditions.

### The Workflow Name

The initials of this Observe/Edit/Hotload/Replay process are OEHR, which, will, it doesn't exactly roll off the tounge like REPL.
So we call it "HotPlay" because "Hotload" and "Replay" are the two central pieces of tech. We love you Figwheel.

