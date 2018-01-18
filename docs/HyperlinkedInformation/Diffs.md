## Only Before and Only After 

This document briefly explains what you are seeing in those mysterious sections
labelled "Only Before" and "Only After".  Enlightenment is nigh.

`re-frame-trace` displays important values like, for example, what's
currently in `app-db` or in a given subscription. While current values are good,
you are often more interested to know how a value has been changed by the epoch. 
The value might start of as, say, `X` (before the event happened) but 
ended up as `X'`.  How are `X` and `X'` different, you wonder? 
What got added or removed? What was modified? 

So, how then to display changes in a way that's easy to grok?  I'm glad you asked. 
`re-frame-trace` chooses to do a calculation best shown by this pseudo code:
```clj
(let [[only-before only-after _] (clojure.data/diff X X')]
   ...)
```
Remember X is the value `before` (this epoch). And `X'` is the value `after` (the epoch has completed). 

By [looking at the docs](https://clojuredocs.org/clojure.data/diff) on `diff`, you'll see
that it calculates how two values differ, and returns a triple of values. `re-frame-trace`  
captures and displays the first two elements of this triple as "only before" and "only after" 
respectively. The 3rd element is not very interesting because it tells us what hasn't changed, so it isn't shown.  

To correctly interpret "Only Before" and "Only after", you'll need to spend a bit 
of time properly familiarising yourself with how `clojure.data/diff` works, but
it will be a worthwhile investment. 
