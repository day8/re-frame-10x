# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]

### Changed

* Switched from LESS to Garden styles. Now interactive development and debugging of re-frame-trace is even faster.
* Reopen/reattach external popup windows when reloading host application

## [0.1.13] - 2017-11-23

**N.B.** To make the best use of the new subscriptions panel, you also need to update to re-frame v0.10.3-alpha1.

### Added

* Bright yellow text when re-frame trace is preloaded, but tracing is not enabled.
* Add preliminary subscriptions panel. Still a bit buggy and not very pretty, but it should be useful even in this state.

## [0.1.12] - 2017-11-16

### Added

* Preliminary support for React 16 component paths. [#89](https://github.com/Day8/re-frame-trace/pull/89)
* External popout window. [#92](https://github.com/Day8/re-frame-trace/issues/92)
* Namespace aware truncation in the traces panel. [#100](https://github.com/Day8/re-frame-trace/pull/100)
* App DB expansions and contractions now persist across reloads. Note the [limitations](https://github.com/Day8/re-frame-trace/blob/master/docs/README.md#app-db-path-expansions) with this feature.

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

* The preloads namespace now adds the tracing panel to the DOM automatically, so you don't need to change any of your app code to bring it in. [#14](https://github.com/Day8/re-frame-trace/pull/14) via [Dexter Gramfors](https://github.com/Dexterminator).
  **Migration steps:** Remove any explicit rendering instructions for `day8.re-frame.trace/devtools` in your app, as this is automatically added now.

## [0.0.8] - 2017-04-13

### Added

* There is now a preload namespace you can use to configure re-frame-trace. Install details are in the README.md. [#13](https://github.com/Day8/re-frame-trace/pull/13) via [Dexter Gramfors](https://github.com/Dexterminator).

## [0.0.7] - 2017-04-13

This was a botched deploy. See 0.0.8.

## [0.0.6] - 2017-03-28

### Added

* Added getting started instructions to README.
