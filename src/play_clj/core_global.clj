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

(defmacro input-key
  [key]
  `~(symbol
      (str utils/gdx-package ".Input$Keys/" (utils/key->static-field key))))

(defmacro is-pressed?
  [key]
  `(.isKeyPressed (Gdx/input) (input-key ~key)))

(defn input
  [& {:keys [key-down key-typed key-up mouse-moved
             scrolled touch-down touch-dragged touch-up]
      :or {key-down dummy key-typed dummy key-up dummy mouse-moved dummy
           scrolled dummy touch-down dummy touch-dragged dummy touch-up dummy}}]
  (reify InputProcessor
    (keyDown [this keycode] (key-down keycode))
    (keyTyped [this character] (key-typed character))
    (keyUp [this keycode] (key-up keycode))
    (mouseMoved [this screen-x screen-y] (mouse-moved screen-x screen-y))
    (scrolled [this amount] (scrolled amount))
    (touchDown [this screen-x screen-y pointer button]
      (touch-down screen-x screen-y pointer button))
    (touchDragged [this screen-x screen-y pointer]
      (touch-dragged screen-x screen-y pointer))
    (touchUp [this screen-x screen-y pointer button]
      (touch-up screen-x screen-y pointer button))))
