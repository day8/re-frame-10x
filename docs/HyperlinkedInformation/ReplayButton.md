This document explains the operation of the "Replay" button, and how to use a **HotPlay Loop**.

By using the backwards and forwards arrows, `re-frame-trace` allows you to navigate through Epochs.
At any one time, you can be inspecting a single Epoch, which we'll term "The Observed Epoch". 

When you click the "Replay" button, you are asking `re-frame-trace` to perform
an "Action Replay" of "The Observed Epoch", and this happens in two Steps: 
1. the value in `app-db` is reset to the value it had immediately prior to "The Observed Epoch"
2. the event which caused "The Observed Epoch" is re-dispatched

Further Notes:
  - In Step 1, the reset of `app-db` will trigger dominoes 4,5,6, causing
    subscriptions and views to rerun, as the application returns to the "prior state" 
    but none of this trace is captured. It is all ignored.
  - All trace arising in Step 2 forms a normal, new Epoch. The (original) Observed Epoch is 
    still there, untouched.
  - The new Epoch (Step 2) is added to the end of the existing Epoch list. It is the newest, most recent Epoch. 
  - The user is auto-navigated to view this new epoch. So the user 
    immediately sees the results for this new Epoch - the timings etc.

### Useful? 

Hell, yes, massively. There's a productive development process we call "A HotPlay Loop" which works like this:
  - A. You **observe** an Epoch (in `re-frame-trace`) to see if an event was correctly processed 
  - B. You notice a problem, and you **edit** (correct) the event handler (or subscription handler, view, etc).
  - C. Figwheel will re-compile and **Hotload** your correction
  - D: You click the **Replay** button
  - E: Back to A

The initials of this process are OEHR, which doesn't exactly roll off the tounge like REPL.
So we just call it "The HotPlay Loop" (Hotload and Replay).

