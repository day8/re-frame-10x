## Using The REPL

`re-frame-10x` captures trace data. When debugging,
it can be useful to experiment with this trace data in your REPL. This document 
explains how to make that happen.

### The Concepts

Just to be clear, we're talking about the kind of 
REPL offered by figwheel - a browser-connected REPL, where code execution happens in the browser-VM running your app. 

At such a REPL, if you were to
type in `(my-ns/some-fn "blah" 2)` and hit return, 
then:
 1. this code will compiled within the context of the REPL's *current namespace*
 2. the resulting (javascript) code will be shipped across to the browser 
 3. the code will be run by the VM also running your app 

**The Step 1** compilation can use any part of your app's codebase, so long as it 
has been previously `required`. The example code fragment above accessed `some-fn` 
in the namespace `my-ns`, so to make it work, something like the following would
previously have happened in your REPL: 
```clj
(require '[my-ns])
```
or maybe
```clj
(require '[some-app.mine :as my-ns])
```

**For Step 3**, when the code is executing in the browser, it can access any 
part of your running app. 

### The Running App

Until this moment, it may not have occurred to you that `re-frame-10x` is effectively
a part of your app. Yes, it certainly looks separate, but it is running alongside your app 
in the same browser VM, and it is a code dependency of your app, which means it can 
be accessed from the REPL like any other part of your app.

So, if we knew enough about the way `re-frame-10x` stored
trace data, we could use the REPL to reach into its 
data structures and access anything we wanted.

So, let's flesh this concept out a bit. Imagine that `re-frame-10x` supplied an API function called, say, `traced-result` 
in a namespace called, say, `day8.re-frame-10x`, then you could evaluate the following at the REPL: 

``` 
(day8.re-frame-10x/traced-result 80 4)
```

and this call would return the trace data at the "trace coordinates" 80 4. If it helps 
to understand, imagine that `80` is the `Epoch id` and `4` is the trace-id within that `Epoch`.
(Warning I just made that up, and it doesn't matter)

In most REPLs, you would have previously `required` the `day8.re-frame-10x` namespace.

### The Method

![Estim8 demo](/docs/images/repl.png)

To facilitate REPL exploration, `re-frame-10x` writes (ClojureScript) code into the clipboard.
You then paste this code into your REPL to obtain access to trace data.

Initially, you click on the **repl requires** hyperlink, and `re-frame-10x` will 
put into the clipboard the `require` code needed to access the `re-frame-10x` API.  
You then paste this code into your REPL and execute it. 
 
Then, later, in the `re-frame-10x` UI you'll notice a small "repl" 
button against each piece of trace you hover over. Again if you click it, `re-frame-10x` will put code into 
the clipboard which uses its own API together with the "trace coordinates" for to the data you want. 

When you paste this code into the REPL, you are able to access to the exact 
piece of trace data you want.

So, you might execute something this at the REPL: 
```clj
(count (day8.re-frame-10x/traced-result 80 4))
```

That `(day8.re-frame-10x/traced-result 80 4)` part would be what you pasted in
from the clipboard.

Or maybe you'd do this
```clj
(let [trace-data (day8.re-frame-10x/traced-result 80 4)]
  (count trace-data))
```
Or:
```cljs
(def tmp (day8.re-frame-10x/traced-result 80 4))
(count tmp)
```


### Why This Way?

This approach carefully avoids the often-problematic need to 
serialise trace data held by `re-frame-10x` in the browser and then 
transfer it over the wire to the REPL for use. 

**First,** when you are tracing large amounts of data, serialisation and then 
later de-serialisation will 
take too long, and you'd loose the advantages 
of structural sharing along the way. 

**Also,** to serialise/de-serialise sometimes requires special extensions to be 
present for the reader literals, etc.  All too complicated. Best avoided.  
