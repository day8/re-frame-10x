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

- Add re-frame-trace into this test project using the [instructions](README.md#installation) above.

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


### Developing CSS

The styles for the trace panel are defined both inline and in Garden styles in `day8.re-frame.trace.styles`.

We are using CSS preprocessing to isolate the panel styles, by namespacing the panel styles with the id `#--re-frame-trace--`.

#### Problems while developing CSS

- Try clearing your browser cache/hard-reloading.

### Updating the internal version of re-frame used

We want to use re-frame, but we don't want to use the re-frame that the host is using, or tracing will get very messy. Instead, we use [mranderson](https://github.com/benedekfazekas/mranderson) to create source dependencies of re-frame.

```console
$ lein do clean
$ lein with-profile mranderson source-deps
$ cp -r target/srcdeps/mranderson047 src 
```

### How does re-frame-trace build?? I don't see anything in the project.clj that looks like it will build.
    
When you add a path to the :source-paths of the test project used to develop the trace panel against, figwheel in the test project will know to watch and build it too.

### I'm developing against the re-frame todomvc project. Why are changes in my local re-frame repo reflected in the example project? Shouldn't it be pulling from clojars?

The re-frame todomvc depends on the source paths of the re-frame project in the same repo. It does this by adding adding `"../../src"` to `:source-paths`.
