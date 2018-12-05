# Advanced Setup

If your project [does not use a `:main` config](../README.md#important-prerequisites) then you will need to do more work to setup your project.

You will need to manually set the Closure defines, and require `day8.re-frame-10x.preload` on your page before you initialize your application. An example:

```html
<script>var CLOSURE_UNCOMPILED_DEFINES = {"re_frame.trace.trace_enabled_QMARK_":true};</script> <!--Make this come first-->
<script src="/cljs/goog/base.js" type="text/javascript"></script>
<script src="/cljs/main-dev.js" type="text/javascript"></script>
<script>goog.require("day8.re_frame_10x.preload");</script> <!--Add this before the app's goog.require-->
<script type="text/javascript">goog.require("todomvc.core");</script>

<script>
        window.onload = function () {
            todomvc.core.main();
        }
</script>
```

These are the two key lines that need to be added to your bootstrap process.

```js
var CLOSURE_UNCOMPILED_DEFINES = {"re_frame.trace.trace_enabled_QMARK_":true};
document.write('<script>goog.require("day8.re_frame_10x.preload");</script>');
```

This setup is inherently project specific, so open an issue if you have trouble, and we'll try and figure it out. Or if possible switch to using a `:main`.

## Using :optimizations :none is recommended

re-frame-10x is designed for development time debugging. The overhead that it imposes would not be suitable for production builds, so it is compiled out by default. If you really want to use re-frame-10x with a production build, you will need to make sure that the `goog.DEBUG` [Closure define](https://clojurescript.org/reference/compiler-options#closure-defines) is `true` to ensure that the tracing isn't dead code eliminated.

There may be more hurdles to cross after this one, Here Be Dragons, if you figure it out, let us know!

## Using Shadow CLJS?

Shadow CLJS doesn't support cljsjs, which we use for some of re-frame-10x's dependencies. Instead, you need to add these via npm/yarn, and make sure you are using shadow-cljs 2.2.22 or higher.

```console
$ yarn add highlight.js react-highlight.js react-flip-move
```
