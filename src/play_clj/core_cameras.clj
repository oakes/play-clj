(in-ns 'play-clj.core)

(defn orthographic*
  []
  (OrthographicCamera.))

(defmacro orthographic
  "Returns an [OrthographicCamera](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/OrthographicCamera.html).

    (orthographic)"
  [& options]
  `(let [^OrthographicCamera object# (orthographic*)]
     (u/calls! object# ~@options)))

(defmacro orthographic!
  "Calls a single method on an `orthographic`."
  [screen k & options]
  `(let [^OrthographicCamera object# (u/get-obj ~screen :camera)]
     (u/call! object# ~k ~@options)))

(defn perspective*
  ([]
    (PerspectiveCamera.))
  ([field-of-view viewport-width viewport-height]
    (PerspectiveCamera. field-of-view viewport-width viewport-height)))

(defmacro perspective
  "Returns a [PerspectiveCamera](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/PerspectiveCamera.html).

    (perspective)"
  [fov vw vh & options]
  `(let [^PerspectiveCamera object# (perspective* ~fov ~vw ~vh)]
     (u/calls! object# ~@options)))

(defmacro perspective!
  "Calls a single method on a `perspective`."
  [screen k & options]
  `(let [^PerspectiveCamera object# (u/get-obj ~screen :camera)]
     (u/call! object# ~k ~@options)))

(defn size!
  "Sets the size of the camera in `screen` and recenters it.

    (size! screen 480 360)"
  [screen width height]
  (let [^Camera camera (u/get-obj screen :camera)]
    (set! (. camera viewportWidth) width)
    (set! (. camera viewportHeight) height)
    (set! (. (. camera position) x) (/ width 2))
    (set! (. (. camera position) y) (/ height 2))
    (.update camera)))

(defn width
  "Returns the width of the camera in `screen`.

    (width screen)"
  [screen]
  (let [^Camera camera (u/get-obj screen :camera)]
    (. camera viewportWidth)))

(defn width!
  "Sets the width of the camera in `screen`, adjusting the height so the ratio
remains in tact.

    (width! screen 480)"
  [screen new-width]
  (size! screen new-width (* new-width (/ (game :height) (game :width)))))

(defn height
  "Returns the height of the camera in `screen`.

    (height screen)"
  [screen]
  (let [^Camera camera (u/get-obj screen :camera)]
    (. camera viewportHeight)))

(defn height!
  "Sets the height of the camera in `screen`, adjusting the width so the ratio
remains in tact.

    (height! screen 360)"
  [screen new-height]
  (size! screen (* new-height (/ (game :width) (game :height))) new-height))

(defn x
  "Returns the x position of the camera in `screen`."
  [screen]
  (let [^Camera camera (u/get-obj screen :camera)]
    (. (. camera position) x)))

(defn x!
  "Sets only the x position of the camera in `screen`."
  [screen x-val]
  (let [^Camera camera (u/get-obj screen :camera)]
    (set! (. (. camera position) x) x-val)
    (.update camera)))

(defn y
  "Returns the y position of the camera in `screen`."
  [screen]
  (let [^Camera camera (u/get-obj screen :camera)]
    (. (. camera position) y)))

(defn y!
  "Sets only the y position of the camera in `screen`."
  [screen y-val]
  (let [^Camera camera (u/get-obj screen :camera)]
    (set! (. (. camera position) y) y-val)
    (.update camera)))

(defn z
  "Returns the z position of the camera in `screen`."
  [screen]
  (let [^Camera camera (u/get-obj screen :camera)]
    (. (. camera position) z)))

(defn z!
  "Sets only the z position of the camera in `screen`."
  [screen z-val]
  (let [^Camera camera (u/get-obj screen :camera)]
    (set! (. (. camera position) z) z-val)
    (.update camera)))

(defn position
  "Returns the position of the camera in `screen`."
  [screen]
  (let [^Camera camera (u/get-obj screen :camera)]
    (. camera position)))

(defn position!
  "Sets the position of the camera in `screen`."
  ([screen pos]
    (let [^Camera camera (u/get-obj screen :camera)]
      (set! (. camera position) pos)))
  ([screen x-val y-val]
    (position! screen x-val y-val nil))
  ([screen x-val y-val z-val]
    (let [^Camera camera (u/get-obj screen :camera)]
      (when x-val (set! (. (. camera position) x) x-val))
      (when y-val (set! (. (. camera position) y) y-val))
      (when z-val (set! (. (. camera position) z) z-val))
      (.update camera))))

(defn direction
  "Returns the direction of the camera in `screen`."
  [screen]
  (let [^Camera camera (u/get-obj screen :camera)]
    (. camera direction)))

(defn direction!
  "Sets the direction of the camera in `screen`."
  [screen x y z]
  (let [^Camera camera (u/get-obj screen :camera)]
    (.lookAt camera x y z)
    (.update camera)))

(defn near
  "Returns the near clipping plane distance of the camera in `screen`."
  [screen]
  (let [^Camera camera (u/get-obj screen :camera)]
    (. camera near)))

(defn near!
  "Sets the near clipping plane distance of the camera in `screen`."
  [screen n]
  (let [^Camera camera (u/get-obj screen :camera)]
    (set! (. camera near) n)
    (.update camera)))

(defn far
  "Returns the far clipping plane distance of the camera in `screen`."
  [screen]
  (let [^Camera camera (u/get-obj screen :camera)]
    (. camera far)))

(defn far!
  "Sets the far clipping plane distance of the camera in `screen`."
  [screen n]
  (let [^Camera camera (u/get-obj screen :camera)]
    (set! (. camera far) n)
    (.update camera)))
