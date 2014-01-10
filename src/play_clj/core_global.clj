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
    :width (.getWidth ^Graphics (Gdx/graphics))
    :height (.getHeight ^Graphics (Gdx/graphics))
    :fps (.getFramesPerSecond ^Graphics (Gdx/graphics))
    :is-fullscreen? (.isFullscreen ^Graphics (Gdx/graphics))
    :is-touched? (.isTouched ^Input (Gdx/input))
    :x (.getX ^Input (Gdx/input))
    :y (.getY ^Input (Gdx/input))
    nil))

(defmacro get-keycode
  [key]
  `~(symbol
      (str utils/gdx-package ".Input$Keys/" (utils/key->static-field key))))

(defmacro is-pressed?
  [key]
  `(.isKeyPressed ^Input (Gdx/input) (get-keycode ~key)))

(defn input
  [& {:keys [key-down key-typed key-up mouse-moved
             scrolled touch-down touch-dragged touch-up]
      :or {key-down dummy key-typed dummy key-up dummy mouse-moved dummy
           scrolled dummy touch-down dummy touch-dragged dummy touch-up dummy}}]
  (let [return-fn (fn [return-val]
                    (some #(not (nil? %)) [return-val false]))]
    (reify InputProcessor
      (keyDown [this keycode]
        (return-fn (key-down keycode)))
      (keyTyped [this character]
        (return-fn (key-typed character)))
      (keyUp [this keycode]
        (return-fn (key-up keycode)))
      (mouseMoved [this screen-x screen-y]
        (return-fn (mouse-moved screen-x screen-y)))
      (scrolled [this amount]
        (return-fn (scrolled amount)))
      (touchDown [this screen-x screen-y pointer button]
        (return-fn (touch-down screen-x screen-y pointer button)))
      (touchDragged [this screen-x screen-y pointer]
        (return-fn (touch-dragged screen-x screen-y pointer)))
      (touchUp [this screen-x screen-y pointer button]
        (return-fn (touch-up screen-x screen-y pointer button))))))

(defmacro input-multi
  [& args]
  `(InputMultiplexer. ~@args))

(defn set-input!
  [^InputProcessor p]
  (.setInputProcessor ^Input (Gdx/input) p))

(defn add-input!
  [^InputProcessor p]
  (.addProcessor ^InputMultiplexer (.getInputProcessor (Gdx/input)) p))

(defn remove-input!
  [^InputProcessor p]
  (.removeProcessor ^InputMultiplexer (.getInputProcessor (Gdx/input)) p))

(defn clear-inputs!
  []
  (.clear ^InputMultiplexer (.getInputProcessor (Gdx/input))))
