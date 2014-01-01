(in-ns 'play-clj.core)

; renderers

(defn load-tiled-map
  [{:keys [file]}]
  (assert (string? file))
  (.load (TmxMapLoader.) file))

(defn unit-scale
  [{:keys [tile-size]}]
  (assert (number? tile-size))
  (float (/ 1 tile-size)))

(defn render!
  [{:keys [renderer ^Camera camera]}]
  (assert renderer)
  (cond
    (isa? (type renderer) BatchTiledMapRenderer)
    (doto renderer
      (.setView camera)
      .render)
    (isa? (type renderer) Stage)
    (.draw renderer)
    :else nil))

(defn tiled-map-layer
  [{:keys [^BatchTiledMapRenderer renderer]} layer]
  (assert renderer)
  (-> renderer .getMap .getLayers (.get layer)))

(defn tiled-map-cell
  [{:keys [^BatchTiledMapRenderer renderer] :as screen} layer x y]
  (assert renderer)
  (-> (if (or (string? layer) (number? layer))
        (tiled-map-layer screen layer)
        layer)
      (.getCell x y)))

(defmulti renderer :type :default nil)

(defmethod renderer nil [opts])

(defmethod renderer :orthogonal-tiled-map [opts]
  (OrthogonalTiledMapRenderer. (load-tiled-map opts) (unit-scale opts)))

(defmethod renderer :isometric-tiled-map [opts]
  (IsometricTiledMapRenderer. (load-tiled-map opts) (unit-scale opts)))

(defmethod renderer :isometric-staggered-tiled-map [opts]
  (IsometricStaggeredTiledMapRenderer. (load-tiled-map opts) (unit-scale opts)))

(defmethod renderer :hexagonal-tiled-map [opts]
  (HexagonalTiledMapRenderer. (load-tiled-map opts) (unit-scale opts)))

(defmethod renderer :stage [_]
  (Stage.))

; cameras

(defmulti camera identity :default :orthographic)

(defmethod camera :orthographic [_]
  (OrthographicCamera.))

(defmethod camera :perspective [_]
  (PerspectiveCamera.))

(defn resize-camera!
  [{:keys [^Camera camera]} width height]
  (assert camera)
  (.setToOrtho camera false width height))

(defn move-camera!
  [{:keys [^Camera camera]} x y]
  (assert camera)
  (when x (set! (. (. camera position) x) x))
  (when y (set! (. (. camera position) y) y))
  (.update camera))
