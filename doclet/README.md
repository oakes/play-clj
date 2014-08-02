This doclet generates the play-clj doc site.

## Directions

1. Download and extract the [libGDX source code](https://github.com/libgdx/libgdx/releases)
2. Run `lein uberjar` in this directory
3. Run the following command in this directory:

`javadoc -classpath /path/to/libgdx/gdx/src/:/path/to/libgdx/extensions/gdx-box2d/gdx-box2d/src/ -subpackages com.badlogic -doclet play_clj_doclet.core.Start -docletpath target/play-clj-doclet-0.0.1-SNAPSHOT-standalone.jar`
