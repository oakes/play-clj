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
    (keyDown [this k]
      (execute-fn! key-down {:keycode k})
      false)
    (keyTyped [this c]
      (execute-fn! key-typed {:character c})
      false)
    (keyUp [this k]
      (execute-fn! key-up {:keycode k})
      false)
    (mouseMoved [this sx sy]
      (execute-fn! mouse-moved {:screen-x sx :screen-y sy})
      false)
    (scrolled [this a]
      (execute-fn! scrolled {:amount a})
      false)
    (touchDown [this sx sy p b]
      (execute-fn! touch-down {:screen-x sx :screen-y sy :pointer p :button b})
      false)
    (touchDragged [this sx sy p]
      (execute-fn! touch-dragged {:screen-x sx :screen-y sy :pointer p})
      false)
    (touchUp [this sx sy p b]
      (execute-fn! touch-up {:screen-x sx :screen-y sy :pointer p :button b})
      false)))

(defmacro input
  [& args]
  `(input* ~args (fn [func# options#] (func# options#))))

(defn gesture*
  [{:keys [fling long-press pan pan-stop pinch tap zoom]
    :or {fling dummy long-press dummy pan dummy pan-stop dummy
         pinch dummy tap dummy zoom dummy}}
   execute-fn!]
  (let [listener
        (reify GestureDetector$GestureListener
          (fling [this vx vy b]
            (execute-fn! fling {:velocity-x vx :velocity-y vy :button b})
            false)
          (longPress [this x y]
            (execute-fn! long-press {:x x :y y})
            false)
          (pan [this x y dx dy]
            (execute-fn! pan {:x x :y y :delta-x dx :delta-y dy})
            false)
          (panStop [this x y p b]
            (execute-fn! pan-stop {:x x :y y :pointer p :button b})
            false)
          (pinch [this ip1 ip2 p1 p2]
            (execute-fn! pinch {:initial-pointer-1 ip1 :initial-pointer-2 ip2
                                :pointer1 p1 :pointer2 p2})
            false)
          (tap [this x y c b]
            (execute-fn! tap {:x x :y y :count c :button b})
            false)
          (touchDown [this x y p b]
            false)
          (zoom [this id d]
            (execute-fn! zoom {:initial-distance id :distance d})
            false))]
    (proxy [GestureDetector] [listener])))

(defmacro gesture
  [& args]
  `(gesture* ~args (fn [func# options#] (func# options#))))

(defn add-input!
  [^InputProcessor p]
  (.addProcessor ^InputMultiplexer (.getInputProcessor (Gdx/input)) p))

(defn remove-input!
  [^InputProcessor p]
  (.removeProcessor ^InputMultiplexer (.getInputProcessor (Gdx/input)) p))
