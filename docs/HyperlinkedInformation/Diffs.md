## Showing Diffs

While it is useful to inspect values like `app-db`,
you are sometimes interested to see how a value has changed.  What are the `diffs`? 
During this Epoch, what got added or removed, and what was modified? 

This page explains what you are seeing in sections labelled "Only Before" and "Only After". 

Assume that a value is `X` before the start of an epoch, and 
ends up being modified to `X'`. To show such differences, `re-frame-10x` chooses 
to do a calculation best explained by this pseudo code:
```clj
(let [[only-before only-after _] (clojure.data/diff X X')]
   ...)
```

By [looking at the docs](https://clojuredocs.org/clojure.data/diff) for `clojure.data/diff`, you'll see
that it calculates how two values differ, and returns a triple of values. `re-frame-10x`
captures and displays to you the first two elements of this triple as "only before" and "only after"
respectively. The 3rd element is ignored because it's what hasn't changed, which isn't interesting.

So, to correctly interpret "Only Before" and "Only after", you'll need to spend 
time properly familiarising yourself with how `clojure.data/diff` works, but this will be
a worthwhile investment. 
