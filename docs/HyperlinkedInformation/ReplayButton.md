> There is currently no Replay button. This document is speculative. See https://github.com/Day8/re-frame-10x/issues/155

This document explains the operation of the "Replay" button, and how to use **The HotPlay Workflow**.

### Epoch Navigation 

By using the backwards and forwards arrows, `re-frame-10x` allows you to navigate through Epochs.
At any one moment, you are inspecting a single Epoch, for which we'll use the term "The Observed Epoch".

### On Click

When you click the "Replay" button, you are asking `re-frame-10x` to perform
an "Action Replay" of "The Observed Epoch", and this happens in two Steps: 
 - **Step 1** - the value in `app-db` is reset to the value it contained immediately prior to "The Observed Epoch"
 - **Step 2** - the event which caused "The Observed Epoch" is re-dispatched
 
So, Step 1 is "reestablish initial conditions" and Step 2 is "do it all again".

#### Further Notes:
  - In Step 1, the reset of `app-db` will trigger computation and trace.
    Subscriptions and views are run as the application returns to the "prior state", 
    but none of the associated trace is captured. It is all ignored.
  - Trace arising from Step 2 forms a normal, new Epoch. The Observed Epoch (which we are replaying) 
    is still there, untouched.
  - New Epochs (Step 2) are always added to the end of the Epoch list (never inserted amongst old Epochs). 
  - The user's experience is that they click the "Replay" button 
    and immediately view the result. So, after Step 2, the user is auto-navigated to this new epoch.

### Useful? 

It facilitates "The HotPlay Workflow": 
  - A. You `Observe` an Epoch (in `re-frame-10x`) to see if an event was correctly processed 
  - B. You notice a problem, and you `Edit` (fix) the event handler (or subs handler, view, etc) via Cursive, Emacs, etc.
  - C. Figwheel will re-compile and `Hotload` your correction
  - D: You click the `Replay` button
  - E: Back to A

Because of `Replay's` "Step 1", you get identical "initial conditions" for
each iteration of the workflow, and this is true blessing. It removes the nagging 
cognative load of "allowing for" slightly shifting state and its consequences, or
the effort of manually reestablishing identical application state before each iteration.

WARNING: obviously this only works when the state is contained within your SPA. When 
there's authorative remote state (a remote database?) there'll be more involved in 
returning to initial conditions each iteration.

### The Workflow Name

The initials of this Observe/Edit/Hotload/Replay process are OEHR, which, well, doesn't exactly roll off the tounge like REPL.
So we call it "HotPlay" because "Hotload" and "Replay" are the two central pieces of tech. We love you Figwheel.

