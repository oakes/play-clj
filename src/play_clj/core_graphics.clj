(in-ns 'play-clj.core)

; tiled maps

(defn tiled-map*
  [s]
  (if (string? s)
    (.load (TmxMapLoader.) s)
    s))

(defmacro tiled-map
  [s & options]
  `(u/calls! ^TiledMap (tiled-map* ~s) ~@options))

(defmacro tiled-map!
  [screen k & options]
  `(let [^BatchTiledMapRenderer object# (u/get-obj ~screen :renderer)]
     (u/call! ^TiledMap (.getMap object#) ~k ~@options)))

(defn tiled-map-layers
  [screen]
  (let [^BatchTiledMapRenderer renderer (u/get-obj screen :renderer)
        ^MapLayers layers (-> renderer .getMap .getLayers)]
    (for [^long i (range (.getCount layers))]
      (.get layers i))))

(defn tiled-map-layer*
  [screen layer]
  (if (isa? (type layer) MapLayer)
    layer
    (->> (tiled-map-layers screen)
         (drop-while #(not= layer (.getName ^MapLayer %)))
         first)))

(defmacro tiled-map-layer
  [screen layer & options]
  `(let [^TiledMapTileLayer object# (tiled-map-layer* ~screen ~layer)]
     (u/calls! object# ~@options)))

(defmacro tiled-map-layer!
  [object k & options]
  `(u/call! ^TiledMapTileLayer (cast TiledMapTileLayer ~object) ~k ~@options))

(defn tiled-map-cell*
  [screen layer x y]
  (.getCell ^TiledMapTileLayer (tiled-map-layer screen layer) x y))

(defmacro tiled-map-cell
  [screen layer x y & options]
  `(let [^TiledMapTileLayer$Cell object# (tiled-map-cell* ~screen ~layer ~x ~y)]
     (u/calls! object# ~@options)))

(defmacro tiled-map-cell!
  [object k & options]
  `(u/call! ^TiledMapTileLayer$Cell ~object ~k ~@options))

; renderers

(defn orthogonal-tiled-map*
  [path unit]
  (OrthogonalTiledMapRenderer. ^TiledMap (tiled-map* path) ^double unit))

(defmacro orthogonal-tiled-map
  [path unit & options]
  `(u/calls! ^OrthogonalTiledMapRenderer (orthogonal-tiled-map* ~path ~unit)
             ~@options))

(defmacro orthogonal-tiled-map!
  [screen k & options]
  `(let [^OrthogonalTiledMapRenderer object# (u/get-obj ~screen :renderer)]
     (u/call! object# ~k ~@options)))

(defn isometric-tiled-map*
  [path unit]
  (IsometricTiledMapRenderer. ^TiledMap (tiled-map* path) ^double unit))

(defmacro isometric-tiled-map
  [path unit & options]
  `(u/calls! ^IsometricTiledMapRenderer (isometric-tiled-map* ~path ~unit)
             ~@options))

(defmacro isometric-tiled-map!
  [screen k & options]
  `(let [^IsometricTiledMapRenderer object# (u/get-obj ~screen :renderer)]
     (u/call! object# ~k ~@options)))

(defn isometric-staggered-tiled-map*
  [path unit]
  (IsometricStaggeredTiledMapRenderer. ^TiledMap (tiled-map* path)
                                       ^double unit))

(defmacro isometric-staggered-tiled-map
  [path unit & options]
  `(u/calls! ^IsometricStaggeredTiledMapRenderer
             (isometric-staggered-tiled-map* ~path ~unit)
             ~@options))

(defmacro isometric-staggered-tiled-map!
  [screen k & options]
  `(let [^IsometricStaggeredTiledMapRenderer object#
         (u/get-obj ~screen :renderer)]
     (u/call! object# ~k ~@options)))

(defn hexagonal-tiled-map*
  [path unit]
  (HexagonalTiledMapRenderer. ^TiledMap (tiled-map* path) ^double unit))

(defmacro hexagonal-tiled-map
  [path unit & options]
  `(u/calls! ^HexagonalTiledMapRenderer (hexagonal-tiled-map* ~path ~unit)
             ~@options))

(defmacro hexagonal-tiled-map!
  [screen k & options]
  `(let [^HexagonalTiledMapRenderer object# (u/get-obj ~screen :renderer)]
     (u/call! object# ~k ~@options)))

(defn stage*
  []
  (Stage.))

(defmacro stage
  [& options]
  `(u/calls! ^Stage (stage*) ~@options))

(defmacro stage!
  [screen k & options]
  `(let [^Stage object# (u/get-obj ~screen :renderer)]
     (u/call! object# ~k ~@options)))

; batch

(defmulti batch #(-> % :renderer class))

(defmethod batch BatchTiledMapRenderer
  [{:keys [^BatchTiledMapRenderer renderer]}]
  (.getSpriteBatch renderer))

(defmethod batch Stage
  [{:keys [^Stage renderer]}]
  (.getSpriteBatch renderer))

; rendering

(defmulti draw-entity! (fn [_ entity] (:type entity)))

(defmethod draw-entity! nil [_ _])

(defmethod draw-entity! :actor
  [^SpriteBatch batch {:keys [^Actor object] :as entity}]
  (assert object)
  (doseq [[k v] entity]
    (case k
      :x (.setX object v)
      :y (.setY object v)
      :width (.setWidth object v)
      :height (.setHeight object v)
      nil))
  (.draw object batch 1))

(defmethod draw-entity! :texture
  [^SpriteBatch batch {:keys [^TextureRegion object x y width height]
                       :or {x 0 y 0}}]
  (assert object)
  (.draw batch object (float x) (float y) (float width) (float height)))

(defn draw! [{:keys [renderer] :as screen} entities]
  (assert renderer)
  (let [^SpriteBatch batch (batch screen)]
    (.begin batch)
    (doseq [entity entities]
      (draw-entity! batch entity))
    (.end batch))
  entities)

(defn ^:private render-map!
  [{:keys [^BatchTiledMapRenderer renderer ^Camera camera]}]
  (when camera (.setView renderer camera))
  (.render renderer))

(defn ^:private render-stage!
  [{:keys [^Stage renderer ^Camera camera]}]
  (when camera
    (.setCamera renderer camera)
    (.setViewport renderer (. camera viewportWidth) (. camera viewportHeight)))
  (doto renderer .act .draw))

(defn render!
  ([{:keys [renderer] :as screen}]
    (cond
      (isa? (type renderer) BatchTiledMapRenderer)
      (render-map! screen)
      (isa? (type renderer) Stage)
      (render-stage! screen)))
  ([screen entities]
    (render! screen)
    (draw! screen entities)))

; cameras

(defn orthographic*
  []
  (OrthographicCamera.))

(defmacro orthographic
  [& options]
  `(let [^OrthographicCamera object# (orthographic*)]
     (u/calls! object# ~@options)))

(defmacro orthographic!
  [screen k & options]
  `(let [^OrthographicCamera object# (u/get-obj ~screen :camera)]
     (u/call! object# ~k ~@options)))

(defn perspective
  []
  (PerspectiveCamera.))

(defmacro perspective
  [& options]
  `(let [^PerspectiveCamera object# (perspective*)]
     (u/calls! object# ~@options)))

(defmacro perspective!
  [screen k & options]
  `(let [^PerspectiveCamera object# (u/get-obj ~screen :camera)]
     (u/call! object# ~k ~@options)))

(defn size!
  [screen width height]
  (let [^OrthographicCamera camera (u/get-obj screen :camera)]
    (assert camera)
    (.setToOrtho camera false width height)))

(defn height!
  [screen new-height]
  (size! screen (* new-height (/ (game :width) (game :height))) new-height))

(defn width!
  [screen new-width]
  (size! screen new-width (* new-width (/ (game :height) (game :width)))))

(defn x!
  [screen x]
  (let [^Camera camera (u/get-obj screen :camera)]
    (assert camera)
    (set! (. (. camera position) x) x)
    (.update camera)))

(defn y!
  [screen y]
  (let [^Camera camera (u/get-obj screen :camera)]
    (assert camera)
    (set! (. (. camera position) y) y)
    (.update camera)))

(defn z!
  [screen z]
  (let [^Camera camera (u/get-obj screen :camera)]
    (assert camera)
    (set! (. (. camera position) z) z)
    (.update camera)))

(defn position!
  ([screen x y]
    (position! screen x y nil))
  ([screen x y z]
    (when x (x! screen x))
    (when y (y! screen y))
    (when z (z! screen z))))
