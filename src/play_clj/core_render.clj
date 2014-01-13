(in-ns 'play-clj.core)

; tiled maps

(defn tiled-map
  [s]
  (if (string? s)
    (.load (TmxMapLoader.) s)
    s))

(defmacro tiled-map!
  [{:keys [^BatchTiledMapRenderer renderer]} k & options]
  `(u/call! ^TiledMap (.getMap ~renderer) ~k ~@options))

(defn tiled-map-layers
  [{:keys [^BatchTiledMapRenderer renderer]}]
  (assert renderer)
  (let [layers (-> renderer .getMap .getLayers)]
    (for [^long i (range (.getCount layers))]
      (.get layers i))))

(defn tiled-map-layer
  [screen layer]
  (if (isa? (type layer) MapLayer)
    layer
    (->> (tiled-map-layers screen)
         (drop-while #(not= layer (.getName ^MapLayer %)))
         first)))

(defmacro tiled-map-layer!
  [layer k & options]
  `(u/call! ^TiledMapTileLayer (cast TiledMapTileLayer ~layer) ~k ~@options))

(defn tiled-map-cell
  [screen layer x y]
  (.getCell ^TiledMapTileLayer (tiled-map-layer screen layer) x y))

(defmacro tiled-map-cell!
  [cell k & options]
  `(u/call! ^TiledMapTileLayer$Cell ~cell ~k ~@options))

(defn ^:private refresh-renderer!
  [{:keys [renderer ui-listeners]} entities]
  (when (isa? (type renderer) Stage)
    (doseq [^Actor a (.getActors ^Stage renderer)]
      (.remove a))
    (doseq [{:keys [object]} entities]
      (when (isa? (type object) Actor)
        (.addActor ^Stage renderer object)
        (doseq [listener ui-listeners]
          (.addListener ^Actor object listener))))
    (remove-input! renderer)
    (add-input! renderer)))

; renderers

(defn orthogonal-tiled-map
  [path unit]
  (OrthogonalTiledMapRenderer. ^TiledMap (tiled-map path) ^double unit))

(defmacro orthogonal-tiled-map!
  [screen k & options]
  `(u/call! ^OrthogonalTiledMapRenderer (:renderer ~screen) ~k ~@options))

(defn isometric-tiled-map
  [path unit]
  (IsometricTiledMapRenderer. ^TiledMap (tiled-map path) ^double unit))

(defmacro isometric-tiled-map!
  [screen k & options]
  `(u/call! ^IsometricTiledMapRenderer (:renderer ~screen) ~k ~@options))

(defn isometric-staggered-tiled-map
  [path unit]
  (IsometricStaggeredTiledMapRenderer. ^TiledMap (tiled-map path) ^double unit))

(defmacro isometric-staggered-tiled-map!
  [screen k & options]
  `(u/call! ^IsometricStaggeredTiledMapRenderer (:renderer ~screen)
                ~k ~@options))

(defn hexagonal-tiled-map
  [path unit]
  (HexagonalTiledMapRenderer. ^TiledMap (tiled-map path) ^double unit))

(defmacro hexagonal-tiled-map!
  [screen k & options]
  `(u/call! ^HexagonalTiledMapRenderer (:renderer ~screen) ~k ~@options))

(defn stage
  []
  (Stage.))

(defmacro stage!
  [screen k & options]
  `(u/call! ^Stage (:renderer ~screen) ~k ~@options))

(defmulti render! #(-> % :renderer type) :default nil)

(defmethod render! nil [screen])

(defmethod render! BatchTiledMapRenderer
  [{:keys [^BatchTiledMapRenderer renderer ^Camera camera]}]
  (when camera (.setView renderer camera))
  (.render renderer))

(defmethod render! Stage
  [{:keys [^Stage renderer ^Camera camera]}]
  (when camera
    (.setCamera renderer camera)
    (.setViewport renderer (. camera viewportWidth) (. camera viewportHeight)))
  (.draw renderer))

; cameras

(defn orthographic-camera
  []
  (OrthographicCamera.))

(defmacro orthographic-camera!
  [screen k & options]
  `(u/call! ^OrthographicCamera (:camera ~screen) ~k ~@options))

(defn perspective-camera
  []
  (PerspectiveCamera.))

(defmacro perspective-camera!
  [screen k & options]
  `(u/call! ^PerspectiveCamera (:camera ~screen) ~k ~@options))

(defn size!
  [{:keys [^OrthographicCamera camera]} width height]
  (assert camera)
  (.setToOrtho camera false width height))

(defn height!
  [screen new-height]
  (size! screen (* new-height (/ (game :width) (game :height))) new-height))

(defn width!
  [screen new-width]
  (size! screen new-width (* new-width (/ (game :height) (game :width)))))

(defn move-x!
  [{:keys [^Camera camera]} x]
  (assert camera)
  (when x (set! (. (. camera position) x) x))
  (.update camera))

(defn move-y!
  [{:keys [^Camera camera]} y]
  (assert camera)
  (when y (set! (. (. camera position) y) y))
  (.update camera))

(defn move!
  [screen x y]
  (move-x! screen x)
  (move-y! screen y))
