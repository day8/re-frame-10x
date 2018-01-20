# re-frame-trace

`re-frame-trace` is a programmer's dashboard. It helps you to see inside a running `re-frame` 
application, allowing you to better understand it and debug it. 
 
**Status:** Alpha.  [![Clojars Project](https://img.shields.io/clojars/v/day8.re-frame/trace.svg)](https://clojars.org/day8.re-frame/trace)

**Note** [the latest version 0.1.14](https://github.com/Day8/re-frame-trace/releases/tag/0.1.14) ALSO requires the latest version of re-frame itself - `v0.10.3-alpha2`.

### Helpful How?

Four ways:

  1. Help you to learn `re-frame`.  If you are new to `re-frame`, looking at 
     the raw "Traces" it will assist you to understand the data flows involved 
     (the dominoes). 
  2. Help you to explore and learn an unfamiliar `re-frame` codebase.
     When I click on this "X" button, it shows me what event is `dispatch`-ed 
     and in what namespace the associated event handler is registered.  And, 
     "oh look, that's interesting - four subscriptions recalculated". Etc.
  3. Help you with debugging. You see an x-ray of your app's functioning. 
     In particular, it will assist you to write and debug 
     event handlers, which is useful because they hold most of the logic 
     in your `re-frame` apps.
  4. Helps you to find performance problems and/or detect where there is 
     unnecessary computation occurring.

> This list is aspirational. `re-frame-trace` remains a WIP. We're getting there. 

### Epoch Oriented 

`re-frame` applications are computationally regular. First an event happens,
and then boom, boom, boom go a series of known computational steps (dominoes), 
in a known order. 
At the end of it, a `re-frame` app lapses into a quiescent state waiting for another 
event to kick off the next iteration of the same cycle.

Each `re-frame` event and its consequent computation forms a bounded "epoch" 
which can be inspected, analysed and understood independently of other epochs. This 
tool is epoch-oriented.

And, yes, it has "time travel debugger" capabilities - you can go backwards
and forwards through epochs - but that's really not the most interesting or powerful 
aspect of what `re-frame-trace` delivers.

### It is about Data

As it runs, `re-frame` generates detailed "trace" which is captured as data, not strings.
This trace provides an x-ray of your app's functioning.

In addition, re-frame is as much "data oriented" as it is functional in design.
It "flows" data, in a loop, through the functions you provide.

So, data is at the core of `re-frame-trace` and that's a powerful and leverageable substrate. 

### Data Dashboard 

Except, there's often too much data - too much detail.

So, `re-frame-trace` tries to be something of a  "dashboard" in the sense that
it tries to turn "raw data" into "information" through curated analysis, and "roll ups"
designed to deliver insight "at a glance". But still allowing you to "drill into the detail".

Right. So, this tool an epoch-oriented, interactive data dashboard for 
gaining insights and assisting debugging. But, it is also a work in progress, 
so these magnificent descriptions run well ahead of what is delivered right now.
But we're getting there.


## A Visual Sampler

<img src="docs/images/re-frame-trace-demo.gif" height="500px">

## Installation

If you are using leiningen, modify `project.clj` in the following ways. When puzzling over the various possible leiningen configurations, it's often helpful to look at a sample [project.clj](https://github.com/technomancy/leiningen/blob/stable/sample.project.clj).

[![Clojars Project](https://img.shields.io/clojars/v/day8.re-frame/trace.svg)](https://clojars.org/day8.re-frame/trace)

- Add re-frame-trace as a dev dependency by placing `[day8.re-frame/trace "VERSION"]` within `:profiles :dev :dependencies`. For example:

  ```cljs
  :profiles
     {:dev
        {:dependencies [[some-other-package  "0.0.0"]
                        [day8.re-frame/trace "0.0.0 (see version above)"]] }}
  ```

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
                      :preloads             [day8.re-frame.trace.preload]}}]}
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

## How does it work?

re-frame is instrumented - all important activity generates trace data. `re-frame-trace` consumes this trace data and renders useful visualisations of the `re-frame` process. Currently, re-frame's tracing capabilities are in alpha and are subject to change at any time. We're testing the utility of the the trace by building an app on top. 

By default, re-frame tracing is "compiled out", so it won't impose a performance cost in production. The trade-off here is that you need to explicitly enable it in development.

The [preloads](https://github.com/clojure/clojurescript/wiki/Compiler-Options#preloads) option (`:preloads [day8.re-frame.trace.preload]`) has to be set in order to automatically monkeypatch Reagent to add appropriate lifecycle hooks. Yes this is gross, and yes we will try and make a PR to reagent to add proper hooks, once we know exactly what we need. The preload namespace also injects a div containing the devtools panel into the DOM.

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
