(in-ns 'play-clj.core)

; graphics

(defn clear!
  "Clears the screen with a uniform color, defaulting to black

    (clear!)
    (clear! 0.5 0.5 1 1)"
  ([]
    (clear! 0 0 0 0))
  ([r g b a]
    (doto (Gdx/gl)
      (.glClearColor (float r) (float g) (float b) (float a))
      (.glClear (bit-or GL20/GL_COLOR_BUFFER_BIT GL20/GL_DEPTH_BUFFER_BIT)))))

(defmacro color
  "Returns a [Color](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/Color.html)

    (color :white)
    (color 1 1 1 1)"
  [& args]
  `~(if (keyword? (first args))
      `(Color. ^Color ~(u/gdx-field :graphics :Color
                                    (u/key->upper (first args))))
      `(Color. ~@args)))

; interop

(defmacro app!
  "Calls a single method on [Gdx.app](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/Application.html)

    (app! :error \"MYTAG\" \"An error occurred, so I'm logging it!\")"
  [k & options]
  `(u/call! ^Application (Gdx/app) ~k ~@options))

(defmacro audio!
  "Calls a single method on [Gdx.audio](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/Audio.html)

    (audio! :new-audio-recorder 44100 false)"
  [k & options]
  `(u/call! ^Audio (Gdx/audio) ~k ~@options))

(defmacro files!
  "Calls a single method on [Gdx.files](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/Files.html)

    (files! :internal \"image.png\")"
  [k & options]
  `(u/call! ^Files (Gdx/files) ~k ~@options))

(defmacro gl!
  "Calls a single method on [Gdx.gl20](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/GL20.html)

    (gl! :gl-create-program)"
  [k & options]
  `(u/call! ^GL20 (Gdx/gl20) ~k ~@options))

(defmacro graphics!
  "Calls a single method on [Gdx.graphics](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/Graphics.html)

    (graphics! :is-fullscreen)"
  [k & options]
  `(u/call! ^Graphics (Gdx/graphics) ~k ~@options))

(defmacro input!
  "Calls a single method on [Gdx.input](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/Input.html)

    (input! :is-touched)"
  [k & options]
  `(u/call! ^Input (Gdx/input) ~k ~@options))

(defmacro net!
  "Calls a single method on [Gdx.net](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/Net.html)

    (net! :open-uri \"https://nightcode.info/\")"
  [k & options]
  `(u/call! ^Net (Gdx/net) ~k ~@options))

; input/output

(defn game
  "Calls the global method cooresponding to `k` in a more Clojuresque way

    (game :width)
    (game :is-touched?)"
  [k]
  (case k
    :width (graphics! :get-width)
    :height (graphics! :get-height)
    :fps (graphics! :get-frames-per-second)
    :is-fullscreen? (graphics! :is-fullscreen)
    :is-touched? (input! :is-touched)
    :x (input! :get-x)
    :y (input! :get-y)
    (u/throw-key-not-found k)))

(defmacro key-code
  "Returns a static field from [Input.Keys](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/Input.Keys.html)

    (key-code :a)
    (key-code :page-down)"
  [k]
  `~(u/gdx-field "Input$Keys" (u/key->upper k)))

(defmacro is-pressed?
  "Returns a boolean indicating if the key cooresponding to `k` is being pressed

    (is-pressed? :a)
    (is-pressed? :page-down)"
  [k]
  `(input! :is-key-pressed (key-code ~k)))

(defn ^:private add-input!
  "Internal use only"
  [^InputProcessor p]
  (let [^InputMultiplexer multi (input! :get-input-processor)]
    (.addProcessor multi p)))

(defn ^:private remove-input!
  "Internal use only"
  [^InputProcessor p]
  (let [^InputMultiplexer multi (input! :get-input-processor)]
    (.removeProcessor multi p)))

(defn sound*
  "The function version of `sound`"
  [path]
  (audio! :new-sound (if (string? path)
                       (files! :internal path)
                       path)))

(defmacro sound
  "Returns a [Sound](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/audio/Sound.html)

    (sound \"playerhurt.wav\")
    (sound \"playerhurt.wav\" :play)"
  [path & options]
  `(u/calls! ^Sound (sound* ~path) ~@options))

(defmacro sound!
  "Calls a single method on a `sound`

    (sound! object :play)
    (sound! object :dispose)"
  [object k & options]
  `(let [^Sound object# ~object]
     (u/call! object# ~k ~@options)))

(defn ^:private loader-init
  "Internal use only"
  [k]
  (cond
    (contains? #{:atlas-tmx-map :tmx-map} k)
    (u/gdx :maps :tiled (str (u/key->pascal k) "Loader."))
    (contains? #{:g3d-model :obj} k)
    (u/gdx :graphics :g3d :loader (str (u/key->pascal k) "Loader."))
    :else
    (u/gdx :assets :loaders (str (u/key->pascal k) "Loader."))))

(defmacro loader
  "Returns a subclass of [AsynchronousAssetLoader](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/assets/loaders/AsynchronousAssetLoader.html)

    (loader :atlas-tmx-map \"map.atlas\")
    (loader :tmx-map \"map.tmx\")
    (loader :obj \"model.obj\")
    (loader :g3d-model \"cube.g3dj\")
    (loader :bitmap-font \"arial.fnt\")
    (loader :music \"song.ogg\")
    (loader :pixmap \"background.png\")
    (loader :skin \"uiskin.json\")
    (loader :sound \"hit.ogg\")
    (loader :texture \"monster.png\")"
  [type resolver & options]
  `(let [object# (~(loader-init type) ~resolver)]
     (u/calls! object# ~@options)))

(defmacro loader!
  "Calls a single method in a subclass of [AsynchronousAssetLoader](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/assets/loaders/AsynchronousAssetLoader.html)

    (loader! object :load \"map.tmx\")"
  [object & options]
  `(u/call! ~object ~@options))

(defn asset-manager*
  "The function version of `asset-manager`"
  ([]
    (AssetManager.))
  ([resolver]
    (AssetManager. resolver)))

(defmacro asset-manager
  "Returns an [AssetManager](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/assets/AssetManager.html)

    (asset-manager)"
  [& options]
  `(let [^AssetManager object# (asset-manager*)]
     (u/calls! object# ~@options)))

(defmacro asset-manager!
  "Calls a single method in an `asset-manager`

    (asset-manager! object :clear)"
  [object k & options]
  `(let [^AssetManager object# ~object]
     (u/call! object# ~k ~@options)))
