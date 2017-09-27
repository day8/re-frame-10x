## Development

### Setting up re-frame-trace for development

You need both the re-frame-trace project _and_ a test project to develop it against. For example, you can use the [todo-mvc](https://github.com/Day8/re-frame/tree/master/examples/todomvc) project.

- Clone `re-frame-trace` to your machine:

  ```
  git clone git@github.com:Day8/re-frame-trace.git
  ```

- Go into the root folder of the test project you're using to develop re-frame-trace with.

  ```
  cd /your/project/folder
  ```

- Add re-frame-trace into this test project using the [instructions](#getting-started) above.

- Still in the test project, create a folder called `checkouts`, then enter the folder:

  ```
  mkdir checkouts
  cd checkouts
  ```

- Create a [relative symlink](https://superuser.com/questions/146231/how-do-i-create-a-relative-symbolic-link-in-linux) from your local re-frame-trace project in the checkouts folder. For example:

  ```
  ln -s ../relative/path/to/your/local/re-frame-trace re-frame-trace
  ```

- If you're using figwheel in the test project, you need to add the checkouts folder (`checkouts/re-frame-trace/src`) to `:cljsbuild :source-paths` in the `project.clj` file. If you're having trouble locating the right place to put this, it might help to look to a sample [project.clj](https://github.com/technomancy/leiningen/blob/stable/sample.project.clj) for inspiration. For example:

  ```
  :cljsbuild {:builds {:client {:source-paths ["checkouts/re-frame-trace/src"]}}}
  ```

- Now run your test project however you usually run it, and re-frame-trace should be in there. \o/

- Additionally, if modifying the `.less` CSS files, compile the css by running within the re-frame-trace directory:

  ```
  lein less auto
  ```

  to watch for changes, or one time by running:

  ```
  lein less once
  ```

  And then any time you want to reload the CSS, you have to **manually save/touch `styles.cljs`**. Figwheel will not do it for you. ([See below](#problems-while-developing-css) for details).


### Developing CSS

The styles for the trace panel are defined both inline and in a LESS file. To develop the styles, edit `resources/day8/re_frame/trace/main.less` and run

```
lein less auto
```

to watch the LESS file and automatically recompile on changes.

**Don't edit the CSS file `resources/day8/re_frame/trace/main.css` directly**, as it will be overwritten.

We are using CSS preprocessing because in order to isolate the panel styles, we are namespacing the panel styles with the id `#--re-frame-trace--`.

#### Problems while developing CSS

- You must touch or save the `styles.cljs` file to trigger a CSS reload if you're editing `main.less`. This is because `styles.cljs` slurps `main.css` with a macro that happens before Clojurescript compilation, so figwheel isn't aware of the changes.
- Did you run `lein less auto` or `lein less once` to compile LESS to CSS?
- Try clearing your browser cache/hard-reloading.
