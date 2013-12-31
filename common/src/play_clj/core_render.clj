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

(defn render-tiled-map
  [{:keys [^BatchTiledMapRenderer renderer ^Camera camera]}]
  (fn []
    (doto renderer
      (.setView camera)
      .render)
    nil))

(defn tiled-map-layer
  [{:keys [^BatchTiledMapRenderer renderer]} layer]
  (-> renderer .getMap .getLayers (.get layer)))

(defn tiled-map-cell
  [{:keys [^BatchTiledMapRenderer renderer] :as screen} layer x y]
  (-> (if (or (string? layer) (number? layer))
        (tiled-map-layer screen layer)
        layer)
      (.getCell x y)))

(defmulti create-renderer :type :default :orthogonal-tiled-map)

(defmethod create-renderer :orthogonal-tiled-map [opts]
  (OrthogonalTiledMapRenderer. (load-tiled-map opts) (unit-scale opts)))

(defmethod create-renderer :isometric-tiled-map [opts]
  (IsometricTiledMapRenderer. (load-tiled-map opts) (unit-scale opts)))

(defmethod create-renderer :isometric-staggered-tiled-map [opts]
  (IsometricStaggeredTiledMapRenderer. (load-tiled-map opts) (unit-scale opts)))

(defmethod create-renderer :hexagonal-tiled-map [opts]
  (HexagonalTiledMapRenderer. (load-tiled-map opts) (unit-scale opts)))

; cameras

(defmulti create-camera identity :default :orthographic)

(defmethod create-camera :orthographic [_]
  (OrthographicCamera.))

(defmethod create-camera :perspective [_]
  (PerspectiveCamera.))

(defn resize-camera
  [{:keys [^Camera camera]} width height]
  (fn []
    (.setToOrtho camera false width height)
    nil))

(defn move-camera
  [{:keys [^Camera camera]} x y]
  (fn []
    (when x (set! (. (. camera position) x) x))
    (when y (set! (. (. camera position) y) y))
    (.update camera)
    nil))
