(in-ns 'play-clj.core)

; rendering

(defn render!
  [{:keys [renderer ^Camera camera]}]
  (assert renderer)
  (cond
    (isa? (type renderer) BatchTiledMapRenderer)
    (doto ^BatchTiledMapRenderer renderer
      (.setView camera)
      .render)
    (isa? (type renderer) Stage)
    (.draw ^Stage renderer)))

(defn tiled-map-layer
  [{:keys [^BatchTiledMapRenderer renderer]} layer]
  (assert renderer)
  (if (isa? (type layer) MapLayer)
    layer
    (-> renderer .getMap .getLayers (.get ^String layer))))

(defn tiled-map-layers
  [{:keys [^BatchTiledMapRenderer renderer]}]
  (assert renderer)
  (let [layers (-> renderer .getMap .getLayers)]
    (for [^long i (range (.getCount layers))]
      (.get layers i))))

(defn tiled-map-layer-name
  [screen layer]
  (.getName ^MapLayer (tiled-map-layer screen layer)))

(defn tiled-map-layer-names
  [screen]
  (for [layer (tiled-map-layers screen)]
    (tiled-map-layer-name screen layer)))

(defn tiled-map-cell
  [screen layer x y]
  (.getCell ^TiledMapTileLayer (tiled-map-layer screen layer) x y))

; renderers

(defn tiled-map
  [s]
  (if (string? s)
    (.load (TmxMapLoader.) s)
    s))

(defn orthogonal-tiled-map
  [path ppt]
  (OrthogonalTiledMapRenderer. ^TiledMap (tiled-map path) ^double (/ 1 ppt)))

(defn isometric-tiled-map
  [path ppt]
  (IsometricTiledMapRenderer. ^TiledMap (tiled-map path) ^double (/ 1 ppt)))

(defn isometric-staggered-tiled-map
  [path ppt]
  (IsometricStaggeredTiledMapRenderer. ^TiledMap (tiled-map path)
                                       ^double (/ 1 ppt)))

(defn hexagonal-tiled-map
  [path ppt]
  (HexagonalTiledMapRenderer. ^TiledMap (tiled-map path) ^double (/ 1 ppt)))

(defn stage
  []
  (Stage.))

; cameras

(defn orthographic-camera
  []
  (OrthographicCamera.))

(defn perspective-camera
  []
  (PerspectiveCamera.))

(defn resize-camera!
  [{:keys [^OrthographicCamera camera]} width height]
  (assert camera)
  (.setToOrtho camera false width height))

(defn move-camera!
  [{:keys [^Camera camera]} x y]
  (assert camera)
  (when x (set! (. (. camera position) x) x))
  (when y (set! (. (. camera position) y) y))
  (.update camera))
