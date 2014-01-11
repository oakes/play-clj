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
      `(Color. ^Color (u/gdx-static-field :graphics :Color ~(first args)))
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
  [key]
  (case key
    :width (graphics! :get-width)
    :height (graphics! :get-height)
    :fps (graphics! :get-frames-per-second)
    :is-fullscreen? (graphics! :is-fullscreen)
    :is-touched? (input! :is-touched)
    :x (input! :get-x)
    :y (input! :get-y)
    nil))

(defmacro key-code
  [key]
  `~(symbol (str u/gdx-package ".Input$Keys/" (u/key->static-field key))))

(defmacro is-pressed?
  [key]
  `(input! :is-key-pressed (key-code ~key)))

(defn ^:private input-processor
  [{:keys [on-key-down on-key-typed on-key-up on-mouse-moved
           on-scrolled on-touch-down on-touch-dragged on-touch-up]}
   execute-fn!]
  (reify InputProcessor
    (keyDown [this k]
      (execute-fn! on-key-down :keycode k)
      false)
    (keyTyped [this c]
      (execute-fn! on-key-typed :character c)
      false)
    (keyUp [this k]
      (execute-fn! on-key-up :keycode k)
      false)
    (mouseMoved [this sx sy]
      (execute-fn! on-mouse-moved :screen-x sx :screen-y sy)
      false)
    (scrolled [this a]
      (execute-fn! on-scrolled :amount a)
      false)
    (touchDown [this sx sy p b]
      (execute-fn! on-touch-down :screen-x sx :screen-y sy :pointer p :button b)
      false)
    (touchDragged [this sx sy p]
      (execute-fn! on-touch-dragged :screen-x sx :screen-y sy :pointer p)
      false)
    (touchUp [this sx sy p b]
      (execute-fn! on-touch-up :screen-x sx :screen-y sy :pointer p :button b)
      false)))

(defn ^:private gesture-listener
  [{:keys [on-fling on-long-press on-pan on-pan-stop on-pinch on-tap on-zoom]}
   execute-fn!]
  (reify GestureDetector$GestureListener
    (fling [this vx vy b]
      (execute-fn! on-fling :velocity-x vx :velocity-y vy :button b)
      false)
    (longPress [this x y]
      (execute-fn! on-long-press :x x :y y)
      false)
    (pan [this x y dx dy]
      (execute-fn! on-pan :x x :y y :delta-x dx :delta-y dy)
      false)
    (panStop [this x y p b]
      (execute-fn! on-pan-stop :x x :y y :pointer p :button b)
      false)
    (pinch [this ip1 ip2 p1 p2]
      (execute-fn! on-pinch
                   :initial-pointer-1 ip1 :initial-pointer-2 ip2
                   :pointer1 p1 :pointer2 p2)
      false)
    (tap [this x y c b]
      (execute-fn! on-tap :x x :y y :count c :button b)
      false)
    (touchDown [this x y p b]
      false)
    (zoom [this id d]
      (execute-fn! on-zoom :initial-distance id :distance d)
      false)))

(defn ^:private gesture-detector
  [options execute-fn!]
  (proxy [GestureDetector] [(gesture-listener options execute-fn!)]))

(defn ^:private add-input!
  [^InputProcessor p]
  (.addProcessor ^InputMultiplexer (input! :get-input-processor) p))
