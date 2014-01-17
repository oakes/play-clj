(in-ns 'play-clj.core)

(defn ^:private update-stage!
  [{:keys [^Stage renderer ui-listeners]} entities]
  (doseq [^Actor a (.getActors renderer)]
    (.remove a))
  (doseq [{:keys [object]} entities]
    (when (isa? (type object) Actor)
      (.addActor renderer object)
      (doseq [listener ui-listeners]
        (.addListener ^Actor object listener))))
  (remove-input! renderer)
  (add-input! renderer))

(defn ^:private update-screen!
  ([{:keys [world g2dp-listener]}]
    (when (isa? (type world) World)
      (.setContactListener ^World world g2dp-listener)))
  ([{:keys [renderer] :as screen} entities]
    (cond
      (isa? (type renderer) Stage)
      (update-stage! screen entities))))

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
  [{:keys [^BatchTiledMapRenderer renderer]} k & options]
  `(u/call! ^TiledMap (.getMap ~renderer) ~k ~@options))

(defn tiled-map-layers
  [{:keys [^BatchTiledMapRenderer renderer]}]
  (assert renderer)
  (let [layers (-> renderer .getMap .getLayers)]
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
  `(u/calls! ^TiledMapTileLayer (tiled-map-layer* ~screen ~layer) ~@options))

(defmacro tiled-map-layer!
  [object k & options]
  `(u/call! ^TiledMapTileLayer (cast TiledMapTileLayer ~object) ~k ~@options))

(defn tiled-map-cell*
  [screen layer x y]
  (.getCell ^TiledMapTileLayer (tiled-map-layer screen layer) x y))

(defmacro tiled-map-cell
  [screen layer x y & options]
  `(u/calls! ^TiledMapTileLayer$Cell (tiled-map-cell* ~screen ~layer ~x ~y)
             ~@options))

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
  `(u/call! ^OrthogonalTiledMapRenderer (or (:renderer ~screen) ~screen)
            ~k ~@options))

(defn isometric-tiled-map*
  [path unit]
  (IsometricTiledMapRenderer. ^TiledMap (tiled-map* path) ^double unit))

(defmacro isometric-tiled-map
  [path unit & options]
  `(u/calls! ^IsometricTiledMapRenderer (isometric-tiled-map* ~path ~unit)
             ~@options))

(defmacro isometric-tiled-map!
  [screen k & options]
  `(u/call! ^IsometricTiledMapRenderer (or (:renderer ~screen) ~screen)
            ~k ~@options))

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
  `(u/call! ^IsometricStaggeredTiledMapRenderer (or (:renderer ~screen) ~screen)
                ~k ~@options))

(defn hexagonal-tiled-map*
  [path unit]
  (HexagonalTiledMapRenderer. ^TiledMap (tiled-map* path) ^double unit))

(defmacro hexagonal-tiled-map
  [path unit & options]
  `(u/calls! ^HexagonalTiledMapRenderer (hexagonal-tiled-map* ~path ~unit)
             ~@options))

(defmacro hexagonal-tiled-map!
  [screen k & options]
  `(u/call! ^HexagonalTiledMapRenderer (or (:renderer ~screen) ~screen)
            ~k ~@options))

(defn stage*
  []
  (Stage.))

(defmacro stage
  [& options]
  `(u/calls! ^Stage (stage*) ~@options))

(defmacro stage!
  [screen k & options]
  `(u/call! ^Stage (or (:renderer ~screen) ~screen) ~k ~@options))

; batch

(defmulti batch #(-> % :renderer class))

(defmethod batch BatchTiledMapRenderer
  [{:keys [^BatchTiledMapRenderer renderer]}]
  (.getSpriteBatch renderer))

(defmethod batch Stage
  [{:keys [^Stage renderer]}]
  (.getSpriteBatch renderer))

(defmulti batch-begin! type)

(defmethod batch-begin! SpriteBatch
  [^SpriteBatch batch]
  (.begin batch))

(defmulti batch-end! type)

(defmethod batch-end! SpriteBatch
  [^SpriteBatch batch]
  (.end batch))

; rendering

(defmulti draw-entity! #(-> % second :type))

(defmethod draw-entity! :actor
  [[^SpriteBatch batch {:keys [^Actor object] :as entity}]]
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
  [[^SpriteBatch batch {:keys [^TextureRegion object x y width height]
                        :or {x 0 y 0 width 0 height 0}}]]
  (assert object)
  (.draw batch object (float x) (float y) (float width) (float height)))

(defn draw! [{:keys [renderer] :as screen} entities]
  (assert renderer)
  (let [^SpriteBatch batch (batch screen)]
    (batch-begin! batch)
    (doseq [entity entities]
      (draw-entity! [batch entity]))
    (batch-end! batch))
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
  (.draw renderer))

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

; physics

(defn step!
  ([{:keys [world time-step velocity-iterations position-iterations]
     :or {time-step (/ 1 60) velocity-iterations 10 position-iterations 10}}]
    (assert world)
    (cond
      (isa? (type world) World)
      (.step ^World world time-step velocity-iterations position-iterations)))
  ([screen entities]
    (step! screen)
    entities))

; cameras

(defn orthographic*
  []
  (OrthographicCamera.))

(defmacro orthographic
  [& options]
  `(u/calls! ^OrthographicCamera (orthographic*) ~@options))

(defmacro orthographic!
  [screen k & options]
  `(u/call! ^OrthographicCamera (or (:camera ~screen) ~screen) ~k ~@options))

(defn perspective
  []
  (PerspectiveCamera.))

(defmacro perspective
  [& options]
  `(u/calls! ^PerspectiveCamera (perspective*) ~@options))

(defmacro perspective!
  [screen k & options]
  `(u/call! ^PerspectiveCamera (or (:camera ~screen) ~screen) ~k ~@options))

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

(defn x!
  [{:keys [^Camera camera]} x]
  (assert camera)
  (set! (. (. camera position) x) x)
  (.update camera))

(defn y!
  [{:keys [^Camera camera]} y]
  (assert camera)
  (set! (. (. camera position) y) y)
  (.update camera))

(defn z!
  [{:keys [^Camera camera]} z]
  (assert camera)
  (set! (. (. camera position) z) z)
  (.update camera))

(defn position!
  ([screen x y]
    (position! screen x y nil))
  ([screen x y z]
    (when x (x! screen x))
    (when y (y! screen y))
    (when z (z! screen z))))
