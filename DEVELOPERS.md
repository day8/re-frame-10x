## Development

### Using the example project (easy)

There is an example TodoMVC project in `/examples/todomvc` in this repo. It is a pretty basic
application, but in many cases is all you need to work on re-frame-10x.

```console
$ cd examples/todomvc
$ lein do clean, dev-auto
```

### Setting up re-frame-10x for development (more advanced)

If you want to test and develop re-frame-10x against your own re-frame projects, you will need to do some more advanced setup using the leiningen checkouts feature.

You need both the re-frame-10x project _and_ a test project to develop it against. For example, you can use the [todo-mvc](https://github.com/day8/re-frame/tree/master/examples/todomvc) project in the re-frame repo.

- Clone `re-frame-10x` to your machine:

  ```
  git clone git@github.com:day8/re-frame-10x.git
  ```

- Go into the root folder of the test project you're using to develop re-frame-10x with.

  ```
  cd /your/project/folder
  ```

- Add re-frame-10x into this test project using the [instructions](README.md#installation) above.

- Still in the test project, create a folder called `checkouts`, then enter the folder:

  ```
  mkdir checkouts
  cd checkouts
  ```

- Create a [relative symlink](https://superuser.com/questions/146231/how-do-i-create-a-relative-symbolic-link-in-linux) from your local re-frame-10x project in the checkouts folder. For example:

  ```
  ln -s ../relative/path/to/your/local/re-frame-10x re-frame-10x

  mklink /d /j re-frame-10x ..\relative\path\to\your\local\re-frame-10x [Windows]
  ```

- If you're using figwheel in the test project, you need to add the checkouts folder (`checkouts/re-frame-10x/src`) to `:cljsbuild :source-paths` in the `project.clj` file. If you're having trouble locating the right place to put this, it might help to look to a sample [project.clj](https://github.com/technomancy/leiningen/blob/stable/sample.project.clj) for inspiration. For example:

  ```
  :cljsbuild {:builds {:client {:source-paths ["checkouts/re-frame-10x/src"]}}}
  ```

- re-frame-10x has a debug panel useful when developing it. You can enable it by adding the :closure-define `"day8.re_frame_10x.debug_QMARK_" true` to your compiler settings.

- Now run your test project however you usually run it, and re-frame-10x should be in there. \o/


### Developing CSS

The styles for the trace panel are defined both inline and in Garden styles in `day8.re-frame-10x.styles`.

We are using CSS preprocessing to isolate the panel styles, by namespacing the panel styles with the id `#--re-frame-10x--`.

#### Problems while developing CSS

- Try clearing your browser cache/hard-reloading.

### Updating the internal version of re-frame used

We want to use re-frame, but we don't want to use the re-frame that the host is using, or tracing will get very messy. Instead, we use [mranderson](https://github.com/benedekfazekas/mranderson) to create source dependencies of re-frame and reagent.

Run `./source-deps.sh` to update the source dependencies.

### How does re-frame-10x build?? I don't see anything in the project.clj that looks like it will build.

When you add a path to the :source-paths of the test project used to develop the trace panel against, figwheel in the test project will know to watch and build it too.

### I'm developing against the re-frame todomvc project. Why are changes in my local re-frame repo reflected in the example project? Shouldn't it be pulling from clojars?

The re-frame todomvc depends on the source paths of the re-frame project in the same repo. It does this by adding adding `"../../src"` to `:source-paths`.
