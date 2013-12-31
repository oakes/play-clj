(in-ns 'play-clj.core)

; graphics

(defmethod execute-entity :clear [{:keys [r g b a] :as entity}]
  (doto (Gdx/gl)
    (.glClearColor (or r 0) (or g 0) (or b 0) (or a 0))
    (.glClear GL20/GL_COLOR_BUFFER_BIT))
  entity)

(defn game*
  [key]
  (case key
    :width `(.getWidth (Gdx/graphics))
    :height `(.getHeight (Gdx/graphics))
    :is-fullscreen? `(.isFullscreen (Gdx/graphics))
    :is-touched? `(.isTouched (Gdx/input))
    :x `(.getX (Gdx/input))
    :y `(.getY (Gdx/input))
    nil))

(defmacro game
  [key]
  `~(game* key))

; input

(defn resolve-key
  [key]
  (if (keyword? key)
    (case key
      :up Input$Keys/DPAD_UP
      :down Input$Keys/DPAD_DOWN
      :left Input$Keys/DPAD_LEFT
      :right Input$Keys/DPAD_RIGHT
      nil)
    key))

(defmacro is-pressed?
  [key]
  `(.isKeyPressed (Gdx/input) ~(resolve-key key)))
