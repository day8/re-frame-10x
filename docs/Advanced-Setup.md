# Advanced Setup

If your project [does not use a `:main` config](../README.md#important-prerequisites) then you will need to do more work to setup your project.

You need this JavaScript to be included on your page before you initialize your application:

```js
var CLOSURE_UNCOMPILED_DEFINES = {"re_frame.trace.trace_enabled_QMARK_":true};
document.write('<script>goog.require("day8.re_frame_10x.preload");</script>');

// Must come before you initialize your application, but after `goog` and your other dependencies have been loaded.
```

This setup is inherently project specific, so open an issue if you have trouble, and we'll try and figure it out. Or if possible switch to using a `:main`.
