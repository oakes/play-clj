(in-ns 'play-clj.core)

; graphics

(defn clear!
  "Clears the screen with a uniform color, defaulting to black.

    (clear!)
    (clear! 0.5 0.5 1 1)"
  ([]
    (clear! 0 0 0 0))
  ([r g b a]
    (doto (Gdx/gl)
      (.glClearColor (float r) (float g) (float b) (float a))
      (.glClear (bit-or GL20/GL_COLOR_BUFFER_BIT GL20/GL_DEPTH_BUFFER_BIT)))))

(defmacro color
  "Returns a [Color](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/Color.html).

    (color :white)
    (color 1 1 1 1)"
  [& args]
  (if (keyword? (first args))
    `(Color. ^Color ~(u/gdx-field :graphics :Color
                                  (u/key->upper (first args))))
    `(Color. ~@args)))

(defmacro color!
  "Calls a single method on a `color`."
  [object k & options]
  `(u/call! ^Color ~object ~k ~@options))

; interop

(defmacro app!
  "Calls a single method on [Gdx.app](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/Application.html).

    (app! :error \"MYTAG\" \"An error occurred, so I'm logging it!\")"
  [k & options]
  `(u/call! ^Application (Gdx/app) ~k ~@options))

(defmacro audio!
  "Calls a single method on [Gdx.audio](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/Audio.html).

    (audio! :new-audio-recorder 44100 false)"
  [k & options]
  `(u/call! ^Audio (Gdx/audio) ~k ~@options))

(defmacro files!
  "Calls a single method on [Gdx.files](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/Files.html).

    (files! :internal \"image.png\")"
  [k & options]
  `(u/call! ^Files (Gdx/files) ~k ~@options))

(defmacro gl
  "Returns a static field from [GL20](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/GL20.html).

    (gl :gl-triangles)"
  [k]
  (u/gdx-field :graphics :GL20 (u/key->upper k)))

(defmacro gl!
  "Calls a single method on [Gdx.gl20](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/GL20.html).

    (gl! :gl-create-program)"
  [k & options]
  `(u/call! ^GL20 (Gdx/gl20) ~k ~@options))

(defmacro graphics!
  "Calls a single method on [Gdx.graphics](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/Graphics.html).

    (graphics! :is-fullscreen)"
  [k & options]
  `(u/call! ^Graphics (Gdx/graphics) ~k ~@options))

(defmacro input!
  "Calls a single method on [Gdx.input](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/Input.html).

    (input! :is-touched)"
  [k & options]
  `(u/call! ^Input (Gdx/input) ~k ~@options))

(defmacro net!
  "Calls a single method on [Gdx.net](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/Net.html).

    (net! :open-uri \"https://nightcode.info/\")"
  [k & options]
  `(u/call! ^Net (Gdx/net) ~k ~@options))

; input/output

(defn game
  "Provides quick access to often-used functions.

    (game :width)"
  [k & [arg]]
  (case k
    :width (graphics! :get-width)
    :height (graphics! :get-height)
    :fps (graphics! :get-frames-per-second)
    :fullscreen? (graphics! :is-fullscreen)
    :touched? (input! :is-touched)
    :x (input! :get-x (or arg 0))
    :y (- (graphics! :get-height) (input! :get-y (or arg 0)))
    :point-x (game :x arg)
    :point-y (game :y arg)
    (u/throw-key-not-found k)))

(defmacro key-code
  "Returns a static field from [Input.Keys](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/Input.Keys.html).

    (key-code :a)
    (key-code :page-down)"
  [k]
  (u/gdx-field "Input$Keys" (u/key->upper k)))

(defmacro key-pressed?
  "Returns a boolean indicating if the key cooresponding to `k` is being pressed.

    (key-pressed? :a)
    (key-pressed? :page-down)"
  [k]
  `(input! :is-key-pressed (key-code ~k)))

(defmacro button-code
  "Returns a static field from [Input.Buttons](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/Input.Buttons.html).

    (button-code :left)"
  [k]
  (u/gdx-field "Input$Buttons" (u/key->upper k)))

(defmacro button-pressed?
  "Returns a boolean indicating if the button cooresponding to `k` is being pressed.

    (button-pressed? :left)"
  [k]
  `(input! :is-button-pressed (button-code ~k)))

(defn ^:private add-input!
  [^InputProcessor p]
  (when-let [^InputMultiplexer multi (input! :get-input-processor)]
    (.addProcessor multi p)))

(defn ^:private remove-input!
  [^InputProcessor p]
  (when-let [^InputMultiplexer multi (input! :get-input-processor)]
    (.removeProcessor multi p)))

(defn sound*
  [^String path]
  (or (u/load-asset path Sound)
      (audio! :new-sound (files! :internal path))))

(defmacro sound
  "Returns a [Sound](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/audio/Sound.html). Supports wav, mp3, and ogg.

    (sound \"playerhurt.wav\")
    (sound \"playerhurt.wav\" :play)"
  [path & options]
  `(let [^Sound object# (sound* ~path)]
     (u/calls! object# ~@options)))

(defmacro sound!
  "Calls a single method on a `sound`.

    (sound! object :play)
    (sound! object :dispose)"
  [object k & options]
  `(let [^Sound object# ~object]
     (u/call! object# ~k ~@options)))

(defn music*
  [^String path]
  (or (u/load-asset path Music)
      (audio! :new-music (files! :internal path))))

(defmacro music
  "Returns a [Music](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/audio/Music.html). Supports wav, mp3, and ogg.

    (music \"song.wav\")
    (music \"song.wav\" :play)"
  [path & options]
  `(let [^Music object# (music* ~path)]
     (u/calls! object# ~@options)))

(defmacro music!
  "Calls a single method on a `music`.

    (music! object :play)
    (music! object :dispose)"
  [object k & options]
  `(let [^Music object# ~object]
     (u/call! object# ~k ~@options)))
