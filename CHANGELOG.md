# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## 1.2.0 (2021-11-03)

#### Added

- Add path annotation support to app-db panel inspectors. 
  Left click to set the inspector's path to the path of the clicked item.
  Right click to get a context menu to copy path, copy object or copy REPL forms.
  Thanks to [MawiraIke](https://github.com/MawiraIke).

#### Changed

- Set devtools max-no-body-items pref to 10K. Thanks to [MawiraIke](https://github.com/MawiraIke).

#### Fixed

- Use component name as operation name for tracing. See #322. Thanks to [p-himik](https://github.com/p-himik).
- Fix subs ID overflowing on subs tab. Thanks to [MawiraIke](https://github.com/MawiraIke).

## 1.1.13 (2021-07-27)

#### Fixed

- Fix overuse of `apply` fn in cljs-devtools wrapper

## 1.1.12 (2021-07-23)

#### Added

- Add leiningen alias for clj-kondo
- Add lein-count leiningen plugin
- Add lein-pprint leiningen plugin

#### Changed

- Upgrade shadow-cljs to 2.15.2
- Replace lein-ancient leiningen plugin with antq
- Upgrade ClojureScript to 1.10.879

#### Fixed

- Fix linter errors and warnings
- Reduce subscription load in cljs-devtools component

## 1.1.11 (2021-07-14)

#### Changed

- Upgrade re-highlight to 1.1.0. Includes [highlight.js 11.1.0](https://github.com/highlightjs/highlight.js/blob/main/CHANGES.md#version-1110).

## 1.1.10 (2021-07-07)

#### Fixed

- Fix performance regression in 1.1.* due to rebuilding cljs-devtools configuration on every render.

## 1.1.9 (2021-07-05)

#### Fixed

- Fix crash when `window.localStorage` is not available. See [#308](https://github.com/day8/re-frame-10x/issues/308). Thanks to [@shepheb](https://github.com/shepheb).

#### Changed

- Upgrade inlined spade to git sha 93ef290

## 1.1.8 (2021-06-23)

#### Fixed

- Fix performance of auto-scrolling epoch navigation by using debounce.

#### Changed

- Upgrade re-highlight to 1.0.0. Includes [highlight.js 11.0.1](https://github.com/highlightjs/highlight.js/blob/main/CHANGES.md#version-1100).

## 1.1.7 (2021-06-22)

#### Fixed

- Fix settings being mangled by false `get`. `refer-clojure :exclude` is :scream:. Fixes [#307](https://github.com/day8/re-frame-10x/issues/307). 
- Fix incorrect key for `external-window?` setting

## 1.1.6 (2021-06-22)

### Fixed

- Fix exception in traces panel when unchecking all category filters. Fixes [#310](https://github.com/day8/re-frame-10x/issues/310).

## 1.1.5 (2021-06-22)

### Fixed

- Fix exception in ctrl-h (toggle show 10x). Fixes [#306](https://github.com/day8/re-frame-10x/issues/306).

## 1.1.4 (2021-06-20)

### Fixed

- Guard against 'Index out of bounds' exception in timing panel

## 1.1.3 (2021-06-08)

### Fixed

- Fix cljs-devtools expansion styles (e.g. app-db panel inspectors, epoch navigation)
- Fix use of :dispatch-later effect when opening/closing 10x window. See [#306](https://github.com/day8/re-frame-10x/issues/306).

### Changed

- Change cljs-devtools path expansion default to false

## 1.1.2 (2021-06-08)

### Fixed

- Fix ordering of inspectors in app-db panel.

## 1.1.1 (2021-05-26)

### Fixed

- Fix epoch navigation cljs-devtools styling for very large event vectors. See [#303](https://github.com/day8/re-frame-10x/issues/303).
- Fix compiler warning.

## 1.1.0 (2021-05-25)

### Added

- Add support for app-db paths reaching into lists. Fixes [#169](https://github.com/day8/re-frame-10x/issues/169)

### Changed

- Change to a new Nord-based theme and new epoch navigation design that always displays a list of
  epoch history.
- Change root container to use shadow-dom. Fixes long-standing issues with unsetting styles so that styling from the
  host page (i.e. application) does not override 10x styling.
- Change styles to use [spade](https://github.com/dhleong/spade)
- Broke monolithic events and subs namespaces up into individual namespaces alongside the associated
panels/views.

### Fixed

- Fix unsafe assumption that reagent component-name is always a string. Fixes
  [#236](https://github.com/day8/re-frame-10x/issues/236).
- Remove `deps.cljs` dependency on `react` and `react-dom` as `reagent` provides these dependencies transitively. Fixes issue reported
in [#188](https://github.com/day8/re-frame-10x/issues/188#issuecomment-487717223).

## [1.0.2] - 2021-03-23

### Fixed

- Replace react-highlight.js with re-highlight. Fixes [day8/re-frame-template#150](https://github.com/day8/re-frame-template/issues/150) and [#288](https://github.com/day8/re-frame-10x/issues/288)

## [1.0.1] - 2021-02-26

### Fixed

- Migrate componentWillUpdate to getSnapshotBeforeUpdate. Fixes [#274](https://github.com/day8/re-frame-10x/issues/274)

## [1.0.0] - 2021-02-25

This version has been upgraded to support the latest re-frame 1.1.x, reagent 1.0.x and React 17.

It does NOT support earlier versions, especially of reagent prior to 1.0.0 or React 16.

### Changed

- Upgrade re-frame to 1.1.2
- Upgrade reagent to 1.0.0 (incl React 17.0.1)
- Upgrade garden to 1.3.10 

## [0.7.0] - 2020-07-16

### Changed

- Upgrade zprint to 1.0.1

## [0.6.7] - 2020-07-08

### Fixed

- Quick fix for Issue #270 - Broken Code formatting in Events tab

## [0.6.6] - 2020-06-29

### Changed

- Upgrade [re-frame-tracing to 0.5.6](https://github.com/day8/re-frame-debux/blob/master/CHANGELOG.md)
- Upgrade binaryage/devtools to 1.0.2
- Upgrade ClojureScript to 1.10.773
- Upgrade shadow-cljs to 2.9.10

## [0.6.5] - 2020-05-15

### Fixed

- Revert change from 0.6.1 to improve performance of switching tabs. This change
  caused 100% CPU usuage on a single core when performing simple unrelated
  interactions on a page, such as hovering over a button with hover styles. 
  For fast machines with many cores it was not recognisable but for slower 
  machines already under load it slows things down to a crawl. 

## [0.6.4] - 2020-05-01

### Fixed

- Fix scroll bar showing when not required on app-db view. Thanks to [@marianafantini](https://github.com/marianafantini).
  See [#260](https://github.com/day8/re-frame-10x/pull/260).

## [0.6.3] - 2020-04-30

### Fixed

- Fix width and overflow styles to enable scrolling large app-db horizontally.
  Thanks to [@marianafantini](https://github.com/marianafantini). See [#259](https://github.com/day8/re-frame-10x/pull/259).

### Changed

- Upgrade ClojureScript to [1.10.748](https://clojurescript.org/news/2020-04-24-release). 
  Required by recent shadow-cljs releases which in turn will enable faster
  GitHub Action builds.
- Upgrade shadow-cljs to 2.8.104 for [faster builds with a valid compiler cache on GitHub Actions](https://github.com/thheller/shadow-cljs/issues/673#issuecomment-619564184).

## [0.6.2] - 2020-04-08

### Fixed

- Fix regressions caused during manual inlining of dependencies.

## [0.6.1] - 2020-04-05

### Changed

- Replace all icons with material design icons for a more consistent look and feel.
- Improve performance of switching tabs.

### Fixed

- Various minor styling bugs.
- Exception in todomvc example route.

## [0.6.0] - 2020-03-08

### Changed

- Upgrade reagent to [0.10.0](https://github.com/reagent-project/reagent/blob/master/CHANGELOG.md#0100-2020-03-06)

### Fixed

- Replaced use of removed `reagent.interop` namespace macros with `goog.object` equivalent. Might use `binaryage/oops`
  in future but that will require dep inlining so this is OK for now as a quick fix for reagent 0.10.0.
- Replaced use of `component/component-path` with `component/component-name`.
- Use `reagent.dom/render` instead of deprecated `reagent.core/render` fn in example.

## [[0.5.2] - 2020-02-14](https://github.com/day8/re-frame-10x/runs/444976485?check_suite_focus=true)

### Fixed

- Fix shadow-cljs compiler warning for react dependencies greater than 16.9.0.
  See [#256](https://github.com/day8/re-frame-10x/pull/256). Thanks to
  [@benalbrecht](https://github.com/benalbrecht).

## [[0.5.1] - 2020-02-13](https://github.com/day8/re-frame-10x/runs/443412121?check_suite_focus=true)

### Fixed

- Fix undeclared variable compiler warnings caused by manual dependency inlining in 0.5.0.


## [[0.5.0] - 2020-02-13](https://github.com/day8/re-frame-10x/runs/443393433?check_suite_focus=true)

### Fixed

- Fix app-db diff regression introduced in 0.4.5.
- Fix styling override regressions introduced in 0.4.6.
- Fix squashed replay button icon

### Changed

- Upgrade react to [16.9.0](https://reactjs.org/blog/2019/08/08/react-v16.9.0.html)
- Upgrade reagent to [0.9.1](https://github.com/reagent-project/reagent/blob/master/CHANGELOG.md#091-2020-01-15)
- Upgrade re-frame to [0.11.0](https://github.com/day8/re-frame/blob/master/CHANGELOG.md#0110-2020-01-20)
- Upgrade binaryage/devtools to [1.0.0](https://github.com/binaryage/cljs-devtools/releases/tag/v1.0.0)

New versions of reagent and re-frame were inlined by hand due to 
[benedekfazekas/mranderson#44](https://github.com/benedekfazekas/mranderson/issues/44)
or similar issue.

### Removed

- Remove cljsjs/create-react-class dependency. Fixes [#247](https://github.com/day8/re-frame-10x/issues/247)

## [0.4.7] - 2020-01-31

### Fixed

- Fix app-db v-scroll regression introduced in 0.4.5.

## [0.4.6] - 2020-01-30

### Fixed

- Fix icon rendering differences between some browsers. Everything should now 
  be using inline SVGs instead of a mix of SVG data URLs and unicode characters.
  Specifically results in a better experience with Edge (EdgeHTML, not Chromium
  based) and Firefox.

## [[0.4.5] - 2019-11-01](https://github.com/day8/re-frame-10x/runs/284984341)

### Added

- Enable programmatically opening or closing panel. See [#210](https://github.com/day8/re-frame-10x/pull/210). Thanks to
  [@mainej](https://github.com/mainej).

### Changed

- Upgrade zprint to 0.5.1 to fix "string/ends-with?" warning. See [#253](https://github.com/day8/re-frame-10x/pull/253).
  Thanks to [@Quezion](https://github.com/Quezion).
- Upgrade todomvc example shadow-cljs to 2.8.69.
- Upgrade todomvc example karma to 4.4.1.
- Upgrade todomvc example karma-junit-reporter to 2.0.1.
- Migrate to me.arrdem/lein-git-version.
- Migrate to continuous deployment for releases via GitHub Actions.

### Fixed

- Fix bug in todomvc example local storage load that prevented app startup completing successfully.

### Removed

* Remove dependency on react-flip-move. This caused a conflict with an application level dependency and adds very little
  value to 10x itself so was deemed unnecessary complexity. *Potentially albeit unlikely breaking change* if you depend
  on react-flip-move in your application and do not have it already in your own project's dependencies. In that case
  simple add `react-flip-move` as an npm dependency for shadow-cljs users, or the `cljsjs/react-flip-move` package for
  everyone else.

## [0.4.4] - 2019-10-14

### Changed

* Upgrade todomvc example shadow-cljs to 2.8.67

### Added

* Event/epoch history explorer. See [#244](https://github.com/day8/re-frame-10x/pull/244).
  Thanks to [@uosl](https://github.com/uosl).

### Fixed

* Prevent nil component path from trace map. See [#245](https://github.com/day8/re-frame-10x/pull/245).
  Thanks to [@dpsutton](https://github.com/dpsutton).

## [0.4.3] - 2018-09-11

### Changed

* Upgrade highlight.js to 9.15.10 

### Fixed

* Fix `unmountComponentAtNode` exception when external window is open. [#234](https://github.com/day8/re-frame-10x/issues/234)
  Possibly also reduces or fixes 'triangles frozen' problem. [#209](https://github.com/day8/re-frame-10x/issues/209)
* Fix scrolling content in trace and subs tabs. [#246](https://github.com/day8/re-frame-10x/issues/246)
* It is no longer possible to resize the 10x panel beyond 90% of the available
  window width thereby avoiding a situation where it was impossible to resize
  to a smaller width if dragged too far; i.e. the panel would get 'stuck'
  covering all the window content.

## [0.4.2] - 2018-07-18

### Fixed

* shadow-cljs compatibility 

### Changed

* Upgraded re-frame to 0.10.8
* Upgraded zprint to 0.4.16

## [0.4.1] - 2019-06-11

### Changed

* Upgraded ClojureScript to 1.10.520
* Upgraded Clojure to 1.10.1
* Upgraded reagent to 0.8.1
* Upgraded garden to 1.3.9
* Upgraded mranderson to 0.5.1
* Upgraded create-react-class to 15.6.3-1
* Upgraded react to 16.8.6
* Upgraded react-dom to 16.8.6
* Upgraded react-flip-move to 3.0.3
* Upgraded highlight.js to 9.15.8

## [0.4.0] - 2019-03-30

### Removed

* Support for React 15/Reagent 0.7.0 and below. The last version of re-frame-10x that supports these versions is 0.3.7. - [#229](https://github.com/day8/re-frame-10x/issues/229)

### Added

* Added an explicit dependency on create-react-class in preparation for it being removed as a dependency in the upcoming Reagent 0.9.0. [#224](https://github.com/day8/re-frame-10x/issues/224)
* Enabled subscription pinning and searching by default.
* There is now a sample project at [examples/todomvc](./examples/todomvc) which you can use to develop against re-frame-10x, or use for comparison when setting up re-frame-10x on your own project.

### Changed

* Use npm-style names for JS libs. This should improve compatibility with Shadow CLJS users, and people using ClojureScript's `:npm-deps`. @deraen - [#201](https://github.com/day8/re-frame-10x/pull/201)

### Fixed

* Make Ctrl-H show/hide shortcut work correctly when using different keyboard layouts. [#231](https://github.com/day8/re-frame-10x/issues/231)

### Internal Changes

* Upgraded to [mranderson 0.5.0](https://github.com/benedekfazekas/mranderson/blob/master/CHANGELOG.md#050). This is an internal change and shouldn't be visible to end-users of 10x, but it's listed here in case you run into any trouble.

## [0.3.7] - 2019-02-19

### Deprecated

This is the last release of re-frame-10x to support from React 15/Reagent 0.6.0-0.7.0 and React 16/Reagent 0.8.0. The next release of re-frame-10x will only support React 16/Reagent 0.8.0 and up. See [#229](https://github.com/day8/re-frame-10x/issues/229) for more context.

### Added

* Added support for searching and pining subscriptions. For now this is available behind the [debug flag](https://github.com/day8/re-frame-10x/blob/master/DEVELOPERS.md). @shen-tian - [#217](https://github.com/day8/re-frame-10x/pull/217)
* Added reader for UUID tagged literals in app-db paths, e.g. `#uuid 7ad6b5f5-e419-4681-a960-e7b35d3de0b1`

### Fixed

* Show a useful error message if browsers block opening the external window. @kajism - [#228](https://github.com/day8/re-frame-10x/pull/228)
* Fix Firefox rendering issue. @solatis - [#223](https://github.com/day8/re-frame-10x/pull/223)
* Prevent warnings about using private vars by using var-quote.
* Hide the replay button when there is no event to replay.

### Changed

* Bumped zprint version to 0.4.15. @jacekschae - [#226](https://github.com/day8/re-frame-10x/pull/226)
* Sort subscriptions alphabetically. @shen-tian - [#217](https://github.com/day8/re-frame-10x/pull/217)

## [0.3.6] - 2018-12-11

Fixed broken merges in 0.3.4 and 0.3.5 when updating the bundled version of re-frame.

## [0.3.5] - 2018-12-06

### Fixed

* Attempted to fix the namespaces when updating the bundled version of re-frame, but we didn't fix everything, so this release should not be used.

## [0.3.4] - 2018-12-06

### Fixed

* Added mising `clojure.data` require.
* Enable zooming in popout window
* Use KeyboardEvent.code instead of KeyboardEvent.key to toggle inspection window

### Changed

* Set default number of retained epochs to 25 (was previously 5).
* Update bundled version of re-frame to 0.10.6

## [0.3.3] - 2018-04-23

### Changed

* The react-16 series of releases has been updated to internally use the newly released [reagent 0.8.0](https://github.com/reagent-project/reagent/blob/master/CHANGELOG.md#080-2018-04-19). This shouldn't conflict with the version of reagent you are using, as long as it also depends on React 16.
* Code traces are now truncated after the first 50 results to avoid overwhelming the browser and the user. There are smarter strategies to handle this that will come in the future, but this at least avoids pathological performance issues with large loops.

### Fixed

* The event tab now correctly displays RegExp data in the data browser.

## [0.3.2] - 2018-04-10

### Changed

* Updated [cljs-devtools](https://github.com/binaryage/cljs-devtools) to 0.9.10, which includes a fix to render ClojureScript 1.10's MapEntry's in a cleaner fashion. Note, if you are using cljs-devtools directly (and you should be!) you will need to update your own version to 0.9.10 or higher.
* Efficiently print results in the code browser, so you only have to pay for the cost of printing what is visible, not the entire data structure. This should result in a big performance boost on apps which have a lot of data in app-db.

## [0.3.1] - 2018-04-05

### Added

* Replay button. This lets you replay a previously run event, with the same app-db context that it had when it ran. See the docs on the [HotPlay Workflow](/docs/HyperlinkedInformation/ReplayButton.md) for ways you can use this button.
* A new REPL button on each traced code form copies a function to your clipboard to access the traced value. You can paste it into your REPL to operate on that value with the full power of the ClojureScript programming language. This is a similar idea to the [scope-capture](https://github.com/vvvvalvalval/scope-capture) library.
* Popout windows now preserve their previous dimensions and screen position (note that Chrome won't let us reposition windows across displays).
* Code tracing has added indentation, so you can visualise the call stack of a function and more easily understand how the calls in your traced function flow.
* Hovering over a code trace shows a preview of the value so you can easily scan it.
* Double clicking on the code section toggles show the full captured function, or just 10 lines.

### Changed

* Improved the vertical space and visual design of the 'pods' in the subs and app-db panels.
* Removed the play/paused distinction. It was confusing for people and unclear what its purpose was. re-frame-10x will navigate to the most recent event that arrives if you were looking at a previous event.
* Popout windows are now titled "re-frame-10x | \<parent window title\>"

### Fixed

* Added syntax highlighting for more elements in the Event code blocks

## [0.3.0] - 2018-03-24

### Added

* Form level code tracing. This lets you inspect all of the intermediate results of an event handlers execution. More info on how to set it up at https://github.com/day8/re-frame-debux.

### Changed

* New visual style for app-db and subs panels

## [0.2.1] - 2018-03-19

### Changed

* Set default behaviour on first launch to show the re-frame-10x panel instead of keeping it hidden. This will help people better debug their setup when they are configuring re-frame-10x.

### Fixed

* Bug where under certain rare circumstances relating to the structure of your app-db, the Event panel could throw an error when transitioning from one epoch to another.
* Refer to the correct Closure define to enable tracing [#170](https://github.com/day8/re-frame-10x/issues/170).
* Add missing requires in parts panel [#164](https://github.com/day8/re-frame-10x/pull/164)

## [0.2.0] - 2018-02-20

### Upgrade notes

* re-frame-trace has been renamed to re-frame-10x! To upgrade you need to do two things:
  * Update your dependency from `day8.re-frame/trace "0.1.21` to `day8.re-frame/re-frame-10x "0.2.0`
  * Update your preload from `day8.re-frame.trace.preload` to `day8.re-frame-10x.preload`. If you don't update the preload you will get a deprecation warning, reminding you to update it.

## [0.1.22] - 2018-02-20

### Changed

* This is the final release of this project under the `day8.re-frame/trace` artifact ID. The only change from 0.1.21 is that this version will print a warning to the console every time you load it, reminding you to update to `day8.re-frame/re-frame-10x`.

## [0.1.21] - 2018-02-14

### Fixed

* Avoid throwing an error `No item 0 in vector of length 0` under certain circumstances.

## [0.1.20] - 2018-02-14

### Upgrade notes

To take advantage of the more granular timing info in this version, you will need to upgrade to re-frame 0.10.5.

### Added

* A time-travelling debugger. Navigating forwards and backwards through the event history updates app-db to match. Be careful when using this with a stateful backend; as in the movies if you change too much or go too far back, the future can become unpredictable.

### Improved

* Improve Timing panel to show more granular timing info.

### Fixed

* Settings panel has a scroll bar if your screen height is too small.

## [0.1.19] - 2018-02-09

### Changed

* Massive overhaul to how subscriptions are processed.
  * Subscriptions that exist but weren't run, now show up in the subscription panel.
  * Subscription creations and disposals that happen during figwheel reloads or otherwise outside of the re-frame event domino cycle are now correctly handled. If any of these happen, they show up in the new section Intra-Epoch Subscriptions.
  * All of the actions that happen to a subscription within an epoch are now shown. This lets you spot unusual behaviour like a subscription being created but not-run, or a subscription running multiple times.
  * Present better explanation messages when viewing the diff section for a sub where the value is unchanged, not run yet, or only run once.

### Fixed

* Garden source dependencies are now working if you don't have your own dependency on Garden.
* New app-db path inspectors default to `"""` instead of `"[]"` so you can see the help text.

## [0.1.18] - 2018-01-31

### Fixed

* Garden source dependencies now work if you don't have your own dependency on Garden.


## [0.1.17] - 2018-01-31

This version requires re-frame 0.10.4 to make use of the newly added Event panel.

### Added

* New event panel. This panel shows the coeffects given to your event handler, the effects your event handler produced, and all of the interceptors in the chain.
* Debugging instructions if re-frame-trace fails to start.
* Setting to drop low level traces. This reduces the memory overhead of re-frame-trace as we can drop more traces that you are unlikely to want most of the time.
* Diff the previous value of a subscription with its current value.

### Changed

* In the subs panel "Ignore **n** layer 2 subs" is now "Ignore **n** unchanged layer 2 subs". This is a more useful filter, as you can filter out noisy layer 2 subscriptions, while still seeing the changes that do happen to layer 2 subs.
* The version of Garden that re-frame-trace uses is now bundled as a source dependency so you should no longer get conflicts if you use Garden 2.
* Refactored re-frame-trace trace parsing internals to incrementally parse new traces.
* Clicking on a trace's expanded information now prints the entire trace to the console instead of just the tags.
* Improved efficency of rendering views that do not need to filter out view namespaces.
* app-db and subs panel now have a slightly more responsive design.

### Fixed

* External windows not loading
* All app-db and subscription path expansions are now independent of each other [#134](https://github.com/day8/re-frame-trace/issues/134).
* Layer 2/3 calculations are more accurate now. We now use the last seen layer level when a subscription runs, to inform it's layer level if it was created or destroyed.
* View namespaces that are ignored are no longer shown when showing traces for all epochs.
* Distinguish between subscriptions that return `nil` values and those that haven't run yet.
* Timing panel not showing elapsed event processing time.

## [0.1.16] - 2018-01-26

There is now a React 16 variant of re-frame-trace available under the version `0.1.16-react16`. If your application uses React 16 and Reagent 0.8.0-alpha2 or higher, this is the version that you will need to use.

### Added

* Setting to control how many epochs are retained
* Setting to reset all epochs
* Setting to ignore epochs
* Setting to filter out trace for views from uninteresting namespaces

### Changed

* Updated bundled re-frame version to 0.10.3, and bundled reagent version to 0.7.0. This shouldn't impact your project's dependencies as they are source bundled via [mranderson](https://github.com/benedekfazekas/mranderson).
* Add hyperlinks to docs

### Fixed

* Set a print limit of 400 characters in the event header, to prevent very large events from DOSing the host application.
* XML encode # character in SVGs, fixing [#130](https://github.com/day8/re-frame-trace/issues/130).
* Fix the reset tracing button in the traces panel.
* Fix a bug when there is only one traced event, re-frame-trace would allow you to go back an epoch, throwing an exception.

## [0.1.15] - 2018-01-24

### Added

* The app-db panel now has buttons to reset app-db to the state at the beginning or end of any epoch.
* The subscription panel shows subscriptions that were created and destroyed.
* The trace panel automatically filters traces to just the current epoch
* A new timing panel shows basic timing statistics around event processing and rendering. More to come here.

### Changed

* re-frame-trace is now organised around epochs. An epoch includes an event being processed, subscriptions running in response, and the view re-rendering.
* A new visual design language.

### Fixed

Lots of quality of life fixes:

* Lower count of items in sequence before expanding icon shows [#126](https://github.com/day8/re-frame-trace/issues/126)
* Only allow one time based filter at a time [#125](https://github.com/day8/re-frame-trace/issues/125)
* Persist trace expansions when changing tab [#105](https://github.com/day8/re-frame-trace/issues/105)
* Hide :sub/create traces for cached subscriptions [#93](https://github.com/day8/re-frame-trace/issues/93)
* Persist filter category settings to localstorage [#121](https://github.com/day8/re-frame-trace/issues/121)
* Highlight events in trace window [#106](https://github.com/day8/re-frame-trace/issues/106)

## [0.1.14] - 2017-12-19

### Added

* New state snapshotting feature: Click the camera to snapshot app-db, and click the load button to restore to your snapshot. Snapshots only persist for the browser session, they aren't available after reloading the browser.
* X button to remove app-db paths

### Changed

* Switched from LESS to Garden styles. Now interactive development and debugging of re-frame-trace is even faster.
* Reopen/reattach external popup windows when reloading host application
* Reorganise namespace layout and remove (hidden) subviz panel and D3 dependency. This may return in the future though.
* Only sort subscriptions by the subscription key, not the whole vector
* Visual tweaks

### Fixed

* Set panel width correctly so you can see the whole panel at all times. Previously the right edge was just off screen.

## [0.1.13] - 2017-11-23

**N.B.** To make the best use of the new subscriptions panel, you also need to update to re-frame v0.10.3-alpha1.

### Added

* Bright yellow text when re-frame trace is preloaded, but tracing is not enabled.
* Add preliminary subscriptions panel. Still a bit buggy and not very pretty, but it should be useful even in this state.

## [0.1.12] - 2017-11-16

### Added

* Preliminary support for React 16 component paths. [#89](https://github.com/day8/re-frame-trace/pull/89)
* External popout window. [#92](https://github.com/day8/re-frame-trace/issues/92)
* Namespace aware truncation in the traces panel. [#100](https://github.com/day8/re-frame-trace/pull/100)
* App DB expansions and contractions now persist across reloads. Note the [limitations](https://github.com/day8/re-frame-trace/blob/master/docs/README.md#app-db-path-expansions) with this feature.

### Changed

* Hide index spans (the blocks next to each element counting the number of elements in a collection)

## [0.1.11] - 2017-10-27

### Changed

* Rename app-state panel to app-db
* Visual improvements to data viewer

## [0.1.10] - 2017-10-27

### Removed

* Subvis panel has been commented out while it is in a broken state.

## [0.1.9] - 2017-10-27

### Added

* Limit captured traces to a maximum of 4,000 to prevent performance slowdowns.

### Changed

* Speed up animations for autoscrolling to bottom of list
* Print full keyword for operation name

### Fixed

* Bad merge for handling window width changing.

## [0.1.8] - 2017-10-25

### Added

* Add a new App State panel. This lets you visualise your application state and inspect it. You can add multiple selections to view a subset of your application state.

### Fixed

* Handle window viewport size changing and other dragging bugs. This _should_ be the last of them, but open an issue if you still find glitches when resizing.

## [0.1.7] - 2017-09-25

### Changed

* Improve style resetting
* Visual improvements

### Fixed

* Fix panel resizing behaviour under edge cases

## [0.1.6] - 2017-09-05

### Added

* Log trace data to the console when clicking on it.

## [0.1.5] - 2017-08-31

### Changed

* Put CSS in resources directory

## [0.1.4] - 2017-08-31

### Added

* Save settings to localstorage to persist across reloads
* Autoscroll to the bottom of the traces panel when at the bottom
* Use LESS for CSS

## [0.1.3] - 2017-08-24

### Added

* Show/hide traces

## [0.1.2] - 2017-08-16

### Changed

* Move styles and components to trace folder

## [0.1.1] - 2017-08-16

### Added

* Add event filtering
* Pin the filter input to the top of the panel

### Changed

* Improve installation instructions

### Fixed

* Fix panel resizing

## [0.1.0] - 2017-05-02

### Changed

* Artifact coordinates changed from `day8.re-frame/abra` to `day8.re-frame/trace`.

## [0.0.9] - 2017-05-02

### Changed

* The preloads namespace now adds the tracing panel to the DOM automatically, so you don't need to change any of your app code to bring it in. [#14](https://github.com/day8/re-frame-trace/pull/14) via [Dexter Gramfors](https://github.com/Dexterminator).
  **Migration steps:** Remove any explicit rendering instructions for `day8.re-frame.trace/devtools` in your app, as this is automatically added now.

## [0.0.8] - 2017-04-13

### Added

* There is now a preload namespace you can use to configure re-frame-trace. Install details are in the README.md. [#13](https://github.com/day8/re-frame-trace/pull/13) via [Dexter Gramfors](https://github.com/Dexterminator).

## [0.0.7] - 2017-04-13

This was a botched deploy. See 0.0.8.

## [0.0.6] - 2017-03-28

### Added

* Added getting started instructions to README.
