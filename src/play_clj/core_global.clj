(in-ns 'play-clj.core)

; graphics

(defn clear!
  ([]
    (clear! 0 0 0 0))
  ([r g b a]
    (doto (Gdx/gl)
      (.glClearColor (float r) (float g) (float b) (float a))
      (.glClear GL20/GL_COLOR_BUFFER_BIT))))

(defmacro color
  [& args]
  `~(if (keyword? (first args))
      `(Color. ^Color (u/static-field-upper :graphics :Color ~(first args)))
      `(Color. ~@args)))

; interop

(defmacro app!
  [k & options]
  `(u/call! ^Application (Gdx/app) ~k ~@options))

(defmacro audio!
  [k & options]
  `(u/call! ^Audio (Gdx/audio) ~k ~@options))

(defmacro files!
  [k & options]
  `(u/call! ^Files (Gdx/files) ~k ~@options))

(defmacro gl!
  [k & options]
  `(u/call! ^GL20 (Gdx/gl20) ~k ~@options))

(defmacro graphics!
  [k & options]
  `(u/call! ^Graphics (Gdx/graphics) ~k ~@options))

(defmacro input!
  [k & options]
  `(u/call! ^Input (Gdx/input) ~k ~@options))

(defmacro net!
  [k & options]
  `(u/call! ^Net (Gdx/net) ~k ~@options))

; input/output

(defn game
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
  [k]
  `~(symbol (str u/main-package ".Input$Keys/" (u/key->upper k))))

(defmacro is-pressed?
  [k]
  `(input! :is-key-pressed (key-code ~k)))

(defn ^:private add-input!
  [^InputProcessor p]
  (let [^InputMultiplexer multi (input! :get-input-processor)]
    (.addProcessor multi p)))

(defn ^:private remove-input!
  [^InputProcessor p]
  (let [^InputMultiplexer multi (input! :get-input-processor)]
    (.removeProcessor multi p)))
