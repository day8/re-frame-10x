# re-frame-trace

`re-frame-trace` let's you look at the inner workings of a running `re-frame` application. It presents as a programmer's dashboard, delivering curated insight and illumination. Because you know more, your capacity to debug will increase.

### It Is Epoch Oriented 

`re-frame` applications are computationally regular. First an event happens,
and then boom, boom, boom go a series of known computational steps (aka dominoes), 
in a known order. When this chain reaction completes, 
a `re-frame` app enters a quiescent state waiting for another 
event to kick off the next iteration of the same process.

Each `re-frame` event and its consequent computation forms a bounded "epoch" 
which can be inspected, analysed and understood independently of other epochs. This 
tool is epoch-oriented - it shows you one at a time.

And, yes, it has "time travel debugger" capabilities - you can go backwards
and forwards through epochs - but that's really not the most interesting or powerful 
aspect of what `re-frame-trace` delivers.

### It Is About Trace Data

As it runs, `re-frame` logs "trace" as data (not strings). 
And this data trace provides an x-ray of your app's functioning.

`re-frame-trace` is essentially a consumer, processor and displayer of this x-ray trace data.

### It Is About The Data Flow

While re-frame is a functional framework, it is
strongly defined by its "data oriented" design. `re-frame`
"flows" data, in a loop, through the functions you provide.
To understand what is happening in a `re-frame` app, you must understand
what data is "happening".     

### It is Just About Data

So, data is at the core of `re-frame-trace` in both of the ways just described, 
and that should not be the slightest bit surprising.

Each time you put a `println` into your program, 
you are printing out what? And why?  Data is the fuel of your debugging investigation.

Have you seen LightTable?  It was attractive because it co-renders data and 
code. The data presented is the "paper trail" by which you can better percieve 
the dynamics of the code.

### It Is A Data Dashboard 

Observing raw data is certaintly interesting, but it isn't enough. Data is the substrate
we want to leverage.  Apart from anything else, there's often too much data - you 
can drown in the detail.

So, `re-frame-trace` tries to be a "dashboard" which curates this
"raw data" into "information" through various kinds of analysis 
and "roll ups". It should deliver insight "at a glance", while still allowing 
you to drill down into the detail.

### Which Is Helpful How?

Four ways:

  1. It helps you to learn `re-frame`.  Simply looking at 
     the "raw traces" provides insight into how it operates. Even experienced
     re-framians, er, like me, have learned a lot.
     
  2. It helps you to explore and learn an unfamiliar `re-frame` codebase.
     When I click, over here, on this "X" button, it shows me what event is `dispatch`-ed 
     and in what namespace the associated event handler is registered.  And, 
     "oh look, that's interesting - four subscriptions recalculated". Etc.
     
  3. It helps you with debugging. You see an x-ray of your app's functioning. 
     In particular, it will assist you to write and debug 
     event handlers, which is useful because they hold most of the logic 
     in your `re-frame` apps.
     
  4. It helps you to find performance problems and/or detect where there is 
     unnecessary computation occurring.

Deeper knowledge is the goal. Easier debugging is the symptom.

### Temporary Warning 

> Some of the claims above are aspirational. `re-frame-trace` [remains a WIP](https://github.com/Day8/re-frame-trace/issues/118).


## The Sausage, Not The Sizzle

We debated internally about the name `re-frame-trace`.  While `-trace` is accurate, it is also 100% sausage because it talks about low level function, and not higher level benefit (sizzle, sizzle).  I wanted to call it `vox-datum` (voice of the data) but that was cruelly rejected, for reasons I don't care to remember. The pain. I mean, who the hell doesn't like a pretentious Latin name??  Philistines.  Anyway, `-insight` and `-illumination` are the benefits, but they made the name waaaay too long.  Naming things - it's just a nightmare!  As is inertia.  So, `-trace` it remains. 

## A Visual Sampler

Slightly out of date, but indicative ...  

<img src="docs/images/re-frame-trace-demo.gif" height="500px">

## Installation

If you are using leiningen, modify `project.clj` in the following ways. When puzzling over the various possible leiningen configurations, it's often helpful to look at a sample [project.clj](https://github.com/technomancy/leiningen/blob/stable/sample.project.clj).

[![Clojars Project](https://img.shields.io/clojars/v/day8.re-frame/trace.svg)](https://clojars.org/day8.re-frame/trace)

- Update your re-frame dependency to at least `0.10.4` - `[re-frame "0.10.4"]`.

- Add re-frame-trace as a dev dependency by placing `[day8.re-frame/trace "VERSION"]` within `:profiles :dev :dependencies`. For example:

  ```cljs
  :profiles
     {:dev
        {:dependencies [[some-other-package  "0.0.0"]
                        [day8.re-frame/trace "0.0.0 (see version above)"]] }}
  ```
  
  If your project uses React 16 and Reagent 0.8.0-alpha2 (or higher) then you will need to add the qualifier `-react16` to the version, e.g. `[day8.re-frame/trace "0.0.0-react16"]`.

- Locate the `:compiler` map under `:dev` and add:

  - `:closure-defines      {"re_frame.trace.trace_enabled_QMARK_" true}`
  - `:preloads             [day8.re-frame.trace.preload]`

  For example:

  ```cljs
  {:builds
     [{:id           "dev"
       :source-paths ["src" "dev"]
       :compiler     {...
                      :closure-defines      {"re_frame.trace.trace_enabled_QMARK_" true}
                      :preloads             [day8.re-frame.trace.preload]
                      }}]}
  ```

[cljs-devtools](https://github.com/binaryage/cljs-devtools) is not required to use re-frame-trace, but it is highly recommended.

## Usage

- **Make sure you have followed all of the installation instructions above.**

- Start up your application.

- Once it is loaded, focus the document window and press `ctrl-h` to slide open the trace panel and enable tracing.

- When the panel is closed, tracing is disabled.

## Use Cases

### app-db

* Inspect a portion of app-db's state with the path inspector, allowing you to focus on just the parts you care about.
* Reset app-db to before an event was run to run it again, instead of resetting the whole application
* Toggle app-db before and after states for running an event, to inspect UI changes. 

### Timing

* Answer the question "Why is my app slow when it runs this event?"
* See whether time is spent in processing an event, or rendering the changes 

## Troubleshooting

* Try a `lein clean`
* Make sure you have followed all the installation steps.

### If re-frame-trace throws an exception on startup

* Reset the settings to factory defaults in the settings panel
* If you can't load the settings panel, run `day8.re_frame.trace.factory_reset_BANG_()` in the JavaScript console.
* If neither of those work, remove all of the keys with the prefix `day8.re-frame.trace` from your browser's Local Storage.

## How does it work?

re-frame is instrumented - all important activity generates trace data. 

`re-frame-trace` consumes this trace data and renders useful visualisations of the `re-frame` process. Currently, re-frame's tracing capabilities are in alpha and are subject to change at any time. We're testing the utility of the the trace by building an app on top. 

By default, re-frame tracing is "compiled out", so it won't impose a performance cost in production. The trade-off here is that you need to explicitly enable it in development.

The [preloads](https://github.com/clojure/clojurescript/wiki/Compiler-Options#preloads) option (`:preloads [day8.re-frame.trace.preload]`) has to be set in order to automatically monkeypatch Reagent to add appropriate lifecycle hooks. Yes this is gross, and yes we will [make a PR to reagent to add proper hooks](https://github.com/Day8/re-frame-trace/issues/115), once we know exactly what we need. The preload namespace also injects a div containing the devtools panel into the DOM.

## Developing/Contributing

If you want to work on re-frame-trace, see [DEVELOPERS.md](DEVELOPERS.md).

## Citations

* [open](https://thenounproject.com/search/?q=popout&i=334227) by Bluetip Design from the Noun Project
* [reload](https://thenounproject.com/adnen.kadri/collection/arrows/?i=798299) by Adnen Kadri from the Noun Project
* [Camera](https://thenounproject.com/search/?q=snapshot&i=200965) by Christian Shannon from the Noun Project
* [Delete](https://thenounproject.com/term/delete/926276) by logan from the Noun Project
* [Settings](https://thenounproject.com/search/?q=settings&i=1169241) by arjuazka from the Noun Project
* [Wrench](https://thenounproject.com/icon/1013218/) by Aleksandr Vector from the Noun Project
* [pause](https://thenounproject.com/icon/1376662/) by Bhuvan from the Noun Project
* [play]() by Bhuvan from the Noun Project
* [Log Out](https://thenounproject.com/icon/54484/) by Arthur Shlain from the Noun Project
