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

(defn input*
  [{:keys [key-down key-typed key-up mouse-moved
           scrolled touch-down touch-dragged touch-up]
    :or {key-down dummy key-typed dummy key-up dummy mouse-moved dummy
         scrolled dummy touch-down dummy touch-dragged dummy touch-up dummy}}
   execute-fn!]
  (reify InputProcessor
    (keyDown [this keycode]
      (execute-fn! key-down {:keycode keycode})
      false)
    (keyTyped [this character]
      (execute-fn! key-typed {:character character})
      false)
    (keyUp [this keycode]
      (execute-fn! key-up {:keycode keycode})
      false)
    (mouseMoved [this screen-x screen-y]
      (execute-fn! mouse-moved {:screen-x screen-x :screen-y screen-y})
      false)
    (scrolled [this amount]
      (execute-fn! scrolled {:amount amount})
      false)
    (touchDown [this screen-x screen-y pointer button]
      (execute-fn! touch-down {:screen-x screen-x :screen-y screen-y
                               :pointer pointer :button button})
      false)
    (touchDragged [this screen-x screen-y pointer]
      (execute-fn! touch-dragged
                   {:screen-x screen-x :screen-y screen-y :pointer pointer})
      false)
    (touchUp [this screen-x screen-y pointer button]
      (execute-fn! touch-up {:screen-x screen-x :screen-y screen-y
                             :pointer pointer :button button})
      false)))

(defmacro input
  [& args]
  `(input* ~args (fn [func# options#] (func# options#))))

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
