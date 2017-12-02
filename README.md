# re-frame-trace

A debugging dashboard for re-frame epochs.

**Status:** Alpha.

[![Clojars Project](https://img.shields.io/clojars/v/day8.re-frame/trace.svg)](https://clojars.org/day8.re-frame/trace)

**Note** [the latest version 0.1.13](https://github.com/Day8/re-frame-trace/releases/tag/0.1.13) ALSO requires the latest version of re-frame itself - `v0.10.3-alpha1`.

## What Is It?

`re-frame` applications have a regular computational path - first an event happens,
and then boom, boom, boom go a series of dominos, before a final quiescent state is
reached. Nothing happens without an event, but when one does happen, it is the same process each time thereafter.

`re-frame-trace` is a programmer's dashboard 
which renders this regular computational process, allowing it to be inspected, understood and debugged.

## Aspirational goals

Here's the vision for what we'd like `re-frame-trace` to deliver (eventually):
  - Learning. If I'm new to re-frame, the dashboard should assist me to understand the 
    dominoes and the data flows involved. 
  - Codebase familialisation. It should help me to explore how an unfamiliar application is wired together. When I click 
    on this button "X", it shows me what event is `dispatch`-ed and in what namespace the associated event handler is registered.  And, 
    "oh look, that's interesting - four subscriptions recalculated". Etc.
  - Debugging assitance. Particularly assitance for writing event handlers which hold most of the application logic. 
  - A method for finding performance problems and detecting where there is unnecessary computation.

## A Dashboard?  

`re-frame` generates detailed "trace data" as it runs, but much of it will be low level and uninteresting, much of the time. 
As a "dashboard", `re-frame-trace` shows interesting, high level information "at a glance", while also allowing you to drill down and explore the detail as necessary.

## What's An Epoch? 

Each `re-frame` event and its consequent computation forms a logically discrete "epoch" which can be analysed and inspected independently. The dashboard design is epoch-oriented.
   
## Sampler

<img src="docs/images/trace-window.gif" height="400px">

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

## Troubleshooting

* Try a `lein clean`
* Make sure you have followed all the installation steps.

## How does it work?

re-frame is instrumented - all important activity generates trace data. `re-frame-trace` is a consumer of this trace data which renders useful visualisations of the `re-frame` process. Currently, re-frame's tracing capabilities are in alpha and are subject to change at any time. We're testing the utility of the the trace by building an app on top. 

By default, re-frame tracing is compiled out, so it won't impose a performance cost in production. The trade-off here is that you need to explicitly enable it in development.

The [preloads](https://github.com/clojure/clojurescript/wiki/Compiler-Options#preloads) option (`:preloads [day8.re-frame.trace.preload]`) has to be set in order to automatically monkeypatch Reagent to add appropriate lifecycle hooks. Yes this is gross, and yes we will try and make a PR to reagent to add proper hooks, once we know exactly what we need. The preload namespace also injects a div containing the devtools panel into the DOM.

## Developing/Contributing

If you want to work on re-frame-trace, see [DEVELOPERS.md](DEVELOPERS.md).

## Citations

[open](https://thenounproject.com/search/?q=popout&i=334227) by Bluetip Design from the Noun Project
