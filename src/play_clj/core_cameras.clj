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

    (perspective 75 (game :width) (game :height))"
  [field-of-view viewport-width viewport-height & options]
  `(let [^PerspectiveCamera object#
         (perspective* ~field-of-view ~viewport-width ~viewport-height)]
     (u/calls! object# ~@options)))

(defmacro perspective!
  "Calls a single method on a `perspective`."
  [screen k & options]
  `(let [^PerspectiveCamera object# (u/get-obj ~screen :camera)]
     (u/call! object# ~k ~@options)))

(defn size!
  "Sets the size of the camera in `screen`.

    (size! screen 480 360)"
  [screen width height]
  (let [^Camera camera (u/get-obj screen :camera)]
    (set! (. camera viewportWidth) width)
    (set! (. camera viewportHeight) height)
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
  "Returns the x position of `object`. If `object` is a screen, the x position
of the camera will be returned."
  [object]
  (cond
    (isa? (type object) Vector2) (. ^Vector2 object x)
    (isa? (type object) Vector3) (. ^Vector3 object x)
    :else (let [^Camera camera (u/get-obj object :camera)]
            (. (. camera position) x))))

(defn x!
  "Sets only the x position of `object`. If `object` is a screen, the x position
of the camera will be set."
  [object x-val]
  (cond
    (isa? (type object) Vector2) (let [^Vector2 v object]
                                   (.set v x-val (. v y)))
    (isa? (type object) Vector3) (let [^Vector3 v object]
                                   (.set v x-val (. v y) (. v z)))
    :else (let [^Camera camera (u/get-obj object :camera)]
            (set! (. (. camera position) x) x-val)
            (.update camera))))

(defn y
  "Returns the y position of `object`. If `object` is a screen, the y position
of the camera will be returned."
  [object]
  (cond
    (isa? (type object) Vector2) (. ^Vector2 object y)
    (isa? (type object) Vector3) (. ^Vector3 object y)
    :else (let [^Camera camera (u/get-obj object :camera)]
            (. (. camera position) y))))

(defn y!
  "Sets only the y position of `object`. If `object` is a screen, the y position
of the camera will be set."
  [object y-val]
  (cond
    (isa? (type object) Vector2) (let [^Vector2 v object]
                                   (.set v (. v x) y-val))
    (isa? (type object) Vector3) (let [^Vector3 v object]
                                   (.set v (. v x) y-val (. v z)))
    :else (let [^Camera camera (u/get-obj object :camera)]
            (set! (. (. camera position) y) y-val)
            (.update camera))))

(defn z
  "Returns the z position of `object`. If `object` is a screen, the z position
of the camera will be returned."
  [object]
  (cond
    (isa? (type object) Vector3) (. ^Vector3 object z)
    :else (let [^Camera camera (u/get-obj object :camera)]
            (. (. camera position) z))))

(defn z!
  "Sets only the z position of `object`. If `object` is a screen, the z position
of the camera will be set."
  [object z-val]
  (cond
    (isa? (type object) Vector3) (let [^Vector3 v object]
                                   (.set v (. v x) (. v y) z-val))
    :else (let [^Camera camera (u/get-obj object :camera)]
            (set! (. (. camera position) z) z-val)
            (.update camera))))

(defn position
  "Returns the position of the camera in `screen`."
  [screen]
  (let [^Camera camera (u/get-obj screen :camera)]
    (. camera position)))

(defn position!
  "Sets the position of `object`. If `object` is a screen, the position of the
camera will be set."
  ([object vec-3]
    (let [^Camera camera (u/get-obj object :camera)]
      (set! (. camera position) vec-3)))
  ([object x-val y-val]
    (position! object x-val y-val nil))
  ([object x-val y-val z-val]
    (when x-val (x! object x-val))
    (when y-val (y! object y-val))
    (when z-val (z! object z-val))))

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
