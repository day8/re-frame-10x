# re-frame-trace

A trace window for re-frame

## Motivation

re-frame provides a data driven architecture, but we need to be able to inspect it. re-frame-trace takes inspiration from [redux-devtools](https://github.com/gaearon/redux-devtools), and provides several ways to visualise the structure and state of your re-frame application.

<img src="docs/images/trace-window.png" height="400px">

## How does it work?

re-frame has instrumentation to collect traces throughout various important points in the lifecycle of a re-frame app. re-frame-trace is a consumer of these traces, and provides visualisations of the traces. These traces have a well-defined structure, and will eventually be standardised, allowing other developers to create their own tooling to work against the traces. Currently, re-frame's tracing and re-frame-trace are in alpha and are subject to change at any time.

## Getting started

Compile your app with `:closure-defines: "re_frame.trace.trace_enabled_QMARK_" true` and `:preloads [day8.re-frame.trace.preload]`.

So when using leiningen, add the following to `project.clj`:

- `[day8.re-frame/trace "0.1.0"]` in `:profiles :dev :dependencies`

    ```cljs
    {:profiles
       {:dev
          {:dependencies [day8.re-frame/trace "0.1.0"] }}
    ```
- `:closure-defines: "re_frame.trace.trace_enabled_QMARK_" true` and `:preloads [day8.re-frame.trace.preload]` in `:compiler`

    ```cljs
        {:builds
           [{:id           "dev"
             :source-paths ["src" "dev"]
             :compiler     {...
                            :closure-defines      {"re_frame.trace.trace_enabled_QMARK_" true}
                            :preloads             [day8.re-frame.trace.preload]}}]}
    ```

By default, re-frame tracing is compiled out, so it won't impose a performance cost in production. The trade-off here is that you need to explicitly enable it in development.

The [preloads](https://github.com/clojure/clojurescript/wiki/Compiler-Options#preloads) option (`:preloads [day8.re-frame.trace.preload]`) has to be set in order to automatically monkeypatch Reagent to add appropriate lifecycle hooks. Yes this is gross, and yes we will try and make a PR to reagent to add proper hooks, once we know exactly what we need. The preload namespace also injects a div containing the devtools panel into the DOM.

Now you can start up your application. Once it is loaded, press Ctrl+H to slide open the trace panel and enable tracing. When the panel is closed, tracing is disabled.


## development

### setting up re-frame-trace for development

- Clone `re-frame-trace` to your machine.
- Go into the root directory of a project you'd like to use as a host to test re-frame-trace with. For example, you can use the [todo-mvc](https://github.com/Day8/re-frame/tree/master/examples/todomvc) project.
- Add re-frame-trace into this project using the [instructions](#getting-started) above.
- Create a folder called `checkouts:`
  ```
  mkdir checkouts
  ```
- Create a symlink from your local re-frame-trace project in the checkouts folder:
  ```
  ln -s path/to/your/local/re-frame-trace checkouts/re-frame-trace
  ```
