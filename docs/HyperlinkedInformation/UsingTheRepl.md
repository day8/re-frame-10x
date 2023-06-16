## Using Trace In Your REPL

`re-frame-10x` captures execution trace (data) and provides a UI for inspecting it. But, sometimes, you want more than this.  Sometimes you want to interact with this datain your REPL. 

This document explains how you can access trace data from within your REPL - so you can experiement with it. 

I'm talking here about using a browser-connected REPL - the kind offered by shadow-clj or figwheel - where any code you enter into your REPL, is ultimately executed within the content of browser-VM currently running your app. 

If you were to type the code `(my-ns/some-fn "blah" 2)` into your REPL and hit return, 
this browser-connected REPL process would proceed in three steps:
 1. your code will be compiled (to javascript)
 2. the resulting (javascript) code will be shipped across to the browser 
 3. the code will be run by the javascript browser-VM running your app 
 
Further notes ...

**The Step 1** compilation happens within the context of the REPL's *current namespace*)
but can use any part of your app's codebase, so long as it 
has been previously `required`. The code fragment above accesses `some-fn` 
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
in the same browser VM and that means **_it can 
be accessed from the REPL like any other part of your app_**. But, of course, you have to know how 
to reach into re-frame-10x and obtain it. 

To make this "reaching in" easy, `re-frame-10x` supplies an API function called `traced-result` 
in a namespace `day8.re-frame-10x`, which you can call from the REPL like this: 

```clj
(day8.re-frame-10x/traced-result 80 4)
```

Wait. Yes, it is a function call, but what are those arguments `80` and `4`?  And how will I know what alternative arguments to supply for my case?
Well, as you'll soon see, it actually doesn't matter and you won't be suppling them yourself. 
But, because your brain is curious and needs an answer, let's give it one, even if it doesn't matter, and I'm lying. 
Those numbers are "trace coordinates" which identify the particular piece of trace data you want.
Imagine that `80` is the `Epoch id` and `4` is the trace-id within that `Epoch`. Happier? Moving on. 


### Your Method

![Estim8 demo](/docs/images/repl.png)

To facilitate REPL exploration of trace data:
  1. `re-frame-10x` will write (ClojureScript) code into the clipboard (when you click on certain hyperlinks in the re-frame-10x UI)
  2. you then paste this code from the clipboard into your REPL to obtain access to trace data

Initially, you click on the **repl requires** hyperlink, and `re-frame-10x` will 
put into the clipboard the `require` code needed to access the `re-frame-10x` API.  
You then paste this code into your REPL and execute it. That's the setup.
 
Then, later, in the `re-frame-10x` UI you'll notice a small "repl" 
hyperlink against each piece of trace you hover over. Again, if you click it, `re-frame-10x` will put code into 
the clipboard which uses its own API together with the "trace coordinates" for the data you want. 

When you paste this clipboard code into the REPL, you are able to access to the exact 
piece of trace data you want.  What's in the clipboard will be something like `(day8.re-frame-10x/traced-result 80 4)`
but it actually doesn't matter what it is - re-frame just gives you an expression which 
retrieve the traced data you want. 

So, you might execute something this at the REPL: 
```clj
(count (day8.re-frame-10x/traced-result 80 4))
```

And, just to be clear, that `(day8.re-frame-10x/traced-result 80 4)` part would be what you pasted in
from the clipboard, and then wrapped it in a `count` call.

Or maybe you'd use it like this: 
```clj
(let [trace-data (day8.re-frame-10x/traced-result 80 4)]
  (count trace-data))
```

Or:
```cljs
(def tmp (day8.re-frame-10x/traced-result 80 4))
(count tmp)
```

So, how you use the expression in the clipboard is up to you.

Do you see now why the `80` and `4` are not meaningful or important to you. `re-frame-10x` 
supplies the right expression (form), and you just use it. It is opaque.

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
