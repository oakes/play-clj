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
      `(Color. ^Color (utils/gdx-static-field :graphics :Color ~(first args)))
      `(Color. ~@args)))

(defmacro bitmap-font
  [& options]
  `(BitmapFont. ~@options))

; input/output

(defn game
  [key]
  (case key
    :width (.getWidth (Gdx/graphics))
    :height (.getHeight (Gdx/graphics))
    :fps (.getFramesPerSecond (Gdx/graphics))
    :is-fullscreen? (.isFullscreen (Gdx/graphics))
    :is-touched? (.isTouched (Gdx/input))
    :x (.getX (Gdx/input))
    :y (.getY (Gdx/input))
    nil))

(defmacro is-pressed?
  [key]
  `(.isKeyPressed (Gdx/input)
     ~(symbol (str utils/gdx-package ".Input$Keys/"
                   (utils/key->static-field key)))))
