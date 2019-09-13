## Using Trace The REPL

`re-frame-10x` captures trace data. 

The `re-frame-10x` UI allows you to inpsect the data, which is mighty good. But often you want to do more. This document explains how to access this traced data from within your REPL - so you can experiement with it. 

Here, we're talking about using a browser-connected REPL - the kind offered by shadow-clj or figwheel. Any code you type in, is ultimately executed by the browser-VM currently running your app. 

If you type in `(my-ns/some-fn "blah" 2)` and hit return, 
the REPL process proceeds in three steps:
 1. your code will be compiled within the context of the REPL's *current namespace*
 2. the resulting (javascript) code will be shipped across to the browser 
 3. the code will be run by the javascript browser-VM running your app 
 
Further notes ...

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

`re-frame-10x` is effectively a part of your app. 

Yes, it looks separate, but it is running alongside your app 
in the same browser VM. It just a code dependency of your app, and that means **_it can 
be accessed from the REPL like any other part of your app_**.

`re-frame-10x` stores trace data, and it can be accessed by you from the REPL, just like any other data stored in your app. 
But, of course, you'd have to know how to reach into re-frame-10x and obtain this traced data. 

To make this easier, `re-frame-10x` supplies an API function called `traced-result` 
in a namespace `day8.re-frame-10x`, which you can call from the REPL like this: 

``` 
(day8.re-frame-10x/traced-result 80 4)
```

Such a call would return the trace data at the "trace coordinates" `80 4`. Imagine that 
`80` is the `Epoch id` and `4` is the trace-id within that `Epoch`.
(Warning I just made that up. As you'll see below, it doesn't matter to you what `80` and `4` represent)

In most REPLs, you would have previously `required` the `day8.re-frame-10x` namespace.

### Your Method

![Estim8 demo](/docs/images/repl.png)

To facilitate REPL exploration:
  1. `re-frame-10x` will write (ClojureScript) code into the clipboard (when you click on certain things in the re-frame-10x UI)
  2. you then paste this code into your REPL to obtain access to trace data

Initially, you click on the **repl requires** hyperlink, and `re-frame-10x` will 
put into the clipboard the `require` code needed to access the `re-frame-10x` API.  
You then paste this code into your REPL and execute it. 
 
Then, later, in the `re-frame-10x` UI you'll notice a small "repl" 
button against each piece of trace you hover over. Again if you click it, `re-frame-10x` will put code into 
the clipboard which uses its own API together with the "trace coordinates" for the data you want. 

When you paste this code into the REPL, you are able to access to the exact 
piece of trace data you want.  What's in the clipboard might look something like `(day8.re-frame-10x/traced-result 80 4)`. This expression will retrieve the traced data you want. 

So, you might execute something this at the REPL: 
```clj
(count (day8.re-frame-10x/traced-result 80 4))
```

And that `(day8.re-frame-10x/traced-result 80 4)` part would be what you pasted in
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

So the expression you paste in `(day8.re-frame-10x/traced-result 80 4)` can be used within your broarder REPL experience.  That's just the expression you use to obtain the data. 

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
