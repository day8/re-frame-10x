This document explains what you are seeing in sections
labelled "Only Before" and "Only After".

In various places, `re-frame-trace` allows you to inspect values like `app-db`. 
Knowing the current value is useful, but 
you are sometimes more interested to know how a value has changed. 
The value might be `X` before the start of this epoch, and 
ended up as `X'`.  So `re-frame-trace` will be showing you `X'` but you might woonder
how `X` and `X'` are different. What got added or removed, and what was modified? 

To show such differences, `re-frame-trace` chooses to do a calculation best explained by this pseudo code:
```clj
(let [[only-before only-after _] (clojure.data/diff X X')]
   ...)
```
Remember `X` is the value immediately `before` (this epoch). And `X'` is the value `after` (the epoch has completed). 

By [looking at the docs](https://clojuredocs.org/clojure.data/diff) for `clojure.data/diff`, you'll see
that it calculates how two values differ, and returns a triple of values. `re-frame-trace`
captures and displays the first two elements of this triple as "only before" and "only after"
respectively. The 3rd element is ignored because it's what hasn't changed, which isn't interesting.

So, to correctly interpret "Only Before" and "Only after", you'll need to spend a bit
of time properly familiarising yourself with how `clojure.data/diff` works, but
it will be a worthwhile investment of your time. 
