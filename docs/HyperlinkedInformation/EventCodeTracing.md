# Event Code Tracing

This panel can show the actual code of the event along with all of it's intermediate values.

[TODO: Insert good example screenshot]

To get to this magic going, you need to make a few adjustments to your project:

 1. Add `[day8.re-frame/debux "0.5.0-SNAPSHOT"]` to the `:dev` `:dependencies` section in project.clj
 2. Add `"debux.cs.core.trace_enabled_QMARK_" true` to the `:closure-defines` section in project.clj
 3. Add `[debux.cs.core :refer-macros [fn-traced]]` to the `:require` section of the event code file(s)
 4. Replace `fn` with `fn-traced` in the events to be traced in this panel
