(in-ns 'play-clj.core)

; tiled maps

(defn tiled-map*
  "The function version of `tiled-map`"
  [s]
  (if (string? s)
    (.load (TmxMapLoader.) s)
    s))

(defmacro tiled-map
  "Returns a [TiledMap](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/maps/tiled/TiledMap.html)

    (tiled-map \"level1.tmx\")
"
  [s & options]
  `(u/calls! ^TiledMap (tiled-map* ~s) ~@options))

(defmacro tiled-map!
  "Calls a single method on a `tiled-map`

    (tiled-map! screen :get-layers)
"
  [screen k & options]
  `(let [^BatchTiledMapRenderer object# (u/get-obj ~screen :renderer)]
     (u/call! ^TiledMap (.getMap object#) ~k ~@options)))

(defn tiled-map-layers
  "Returns a list with [MapLayer](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/maps/MapLayer.html)
objects cooresponding to each layer in the tiled map in `screen`

    (tiled-map-layers screen)
"
  [screen]
  (let [^BatchTiledMapRenderer renderer (u/get-obj screen :renderer)
        ^MapLayers layers (-> renderer .getMap .getLayers)]
    (for [^long i (range (.getCount layers))]
      (.get layers i))))

(defn tiled-map-layer*
  "The function version of `tiled-map-layer`"
  [screen layer]
  (if (isa? (type layer) MapLayer)
    layer
    (->> (tiled-map-layers screen)
         (drop-while #(not= layer (.getName ^MapLayer %)))
         first)))

(defmacro tiled-map-layer
  "Returns a [TiledMapTileLayer](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/maps/tiled/TiledMapTileLayer.html)
from the tiled map in `screen` that matches `layer`

    (tiled-map-layer screen \"water\")
"
  [screen layer & options]
  `(let [^TiledMapTileLayer object# (tiled-map-layer* ~screen ~layer)]
     (u/calls! object# ~@options)))

(defmacro tiled-map-layer!
  "Calls a single method on a `tiled-map-layer`

    (tiled-map-layer! (tiled-map-layer screen \"water\")
                      :set-cell 0 0 nil)
"
  [object k & options]
  `(u/call! ^TiledMapTileLayer (cast TiledMapTileLayer ~object) ~k ~@options))

(defn tiled-map-cell*
  "The function version of `tiled-map-cell`"
  [screen layer x y]
  (.getCell ^TiledMapTileLayer (tiled-map-layer screen layer) x y))

(defmacro tiled-map-cell
  "Returns a [TiledMapTileLayer.Cell](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/maps/tiled/TiledMapTileLayer.Cell.html)
from the tiled map in `screen` from the given `layer` and position `x` and `y`

    (tiled-map-cell screen \"water\" 0 0)
"
  [screen layer x y & options]
  `(let [^TiledMapTileLayer$Cell object# (tiled-map-cell* ~screen ~layer ~x ~y)]
     (u/calls! object# ~@options)))

(defmacro tiled-map-cell!
  "Calls a single method on a `tiled-map-cell`

    (tiled-map-cell! (tiled-map-cell screen \"water\" 0 0)
                     :set-rotation 90)
"
  [object k & options]
  `(u/call! ^TiledMapTileLayer$Cell ~object ~k ~@options))

; renderers

(defn orthogonal-tiled-map*
  "The function version of `orthogonal-tiled-map`"
  [path unit]
  (OrthogonalTiledMapRenderer. ^TiledMap (tiled-map* path) ^double unit))

(defmacro orthogonal-tiled-map
  "Returns an [OrthogonalTiledMapRenderer](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/maps/tiled/renderers/OrthogonalTiledMapRenderer.html)
with the tiled map file at `path` and `unit` scale

    (orthogonal-tiled-map \"level1.tmx\" (/ 1 8))
"
  [path unit & options]
  `(u/calls! ^OrthogonalTiledMapRenderer (orthogonal-tiled-map* ~path ~unit)
             ~@options))

(defmacro orthogonal-tiled-map!
  "Calls a single method on an `orthogonal-tiled-map`"
  [screen k & options]
  `(let [^OrthogonalTiledMapRenderer object# (u/get-obj ~screen :renderer)]
     (u/call! object# ~k ~@options)))

(defn isometric-tiled-map*
  "The function version of `isometric-tiled-map`"
  [path unit]
  (IsometricTiledMapRenderer. ^TiledMap (tiled-map* path) ^double unit))

(defmacro isometric-tiled-map
  "Returns an [IsometricTiledMapRenderer](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/maps/tiled/renderers/IsometricTiledMapRenderer.html)
with the tiled map file at `path` and `unit` scale

    (isometric-tiled-map \"level1.tmx\" (/ 1 8))
"
  [path unit & options]
  `(u/calls! ^IsometricTiledMapRenderer (isometric-tiled-map* ~path ~unit)
             ~@options))

(defmacro isometric-tiled-map!
  "Calls a single method on an `isometric-tiled-map`"
  [screen k & options]
  `(let [^IsometricTiledMapRenderer object# (u/get-obj ~screen :renderer)]
     (u/call! object# ~k ~@options)))

(defn isometric-staggered-tiled-map*
  "The function version of `isometric-staggered-tiled-map`"
  [path unit]
  (IsometricStaggeredTiledMapRenderer. ^TiledMap (tiled-map* path)
                                       ^double unit))

(defmacro isometric-staggered-tiled-map
  "Returns an [IsometricStaggeredTiledMapRenderer](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/maps/tiled/renderers/IsometricStaggeredTiledMapRenderer.html)
with the tiled map file at `path` and `unit` scale

    (isometric-staggered-tiled-map \"level1.tmx\" (/ 1 8))
"
  [path unit & options]
  `(u/calls! ^IsometricStaggeredTiledMapRenderer
             (isometric-staggered-tiled-map* ~path ~unit)
             ~@options))

(defmacro isometric-staggered-tiled-map!
  "Calls a single method on an `isometric-staggered-tiled-map`"
  [screen k & options]
  `(let [^IsometricStaggeredTiledMapRenderer object#
         (u/get-obj ~screen :renderer)]
     (u/call! object# ~k ~@options)))

(defn hexagonal-tiled-map*
  "The function version of `hexagonal-tiled-map`"
  [path unit]
  (HexagonalTiledMapRenderer. ^TiledMap (tiled-map* path) ^double unit))

(defmacro hexagonal-tiled-map
  "Returns a [HexagonalTiledMapRenderer](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/maps/tiled/renderers/HexagonalTiledMapRenderer.html)
with the tiled map file at `path` and `unit` scale

    (hexagonal-tiled-map \"level1.tmx\" (/ 1 8))
"
  [path unit & options]
  `(u/calls! ^HexagonalTiledMapRenderer (hexagonal-tiled-map* ~path ~unit)
             ~@options))

(defmacro hexagonal-tiled-map!
  "Calls a single method on a `hexagonal-tiled-map`"
  [screen k & options]
  `(let [^HexagonalTiledMapRenderer object# (u/get-obj ~screen :renderer)]
     (u/call! object# ~k ~@options)))

(defn stage*
  "The function version of `stage`"
  []
  (Stage.))

(defmacro stage
  "Returns a [Stage](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/Stage.html)

    (stage)
"
  [& options]
  `(u/calls! ^Stage (stage*) ~@options))

(defmacro stage!
  "Calls a single method on a `stage`"
  [screen k & options]
  `(let [^Stage object# (u/get-obj ~screen :renderer)]
     (u/call! object# ~k ~@options)))

; batch

(defmulti batch
  "Internal use only"
  #(-> % :renderer class))

(defmethod batch BatchTiledMapRenderer
  [{:keys [^BatchTiledMapRenderer renderer]}]
  (.getSpriteBatch renderer))

(defmethod batch Stage
  [{:keys [^Stage renderer]}]
  (.getSpriteBatch renderer))

; rendering

(defmulti draw-entity!
  "Internal use only"
  (fn [_ entity] (:type entity)))

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
  [^SpriteBatch batch {:keys [^TextureRegion object x y width height]}]
  (assert object)
  (let [x (float (or x 0))
        y (float (or y 0))
        width (float (or width (.getRegionWidth object)))
        height (float (or height (.getRegionHeight object)))]
    (.draw batch object x y width height)))

(defn draw!
  "Draws the `entities` with the renderer from `screen`

    (draw! screen entities)
"
  [{:keys [renderer] :as screen} entities]
  (assert renderer)
  (let [^SpriteBatch batch (batch screen)]
    (.begin batch)
    (doseq [entity entities]
      (draw-entity! batch entity))
    (.end batch))
  entities)

(defn ^:private render-map!
  "Internal use only"
  [{:keys [^BatchTiledMapRenderer renderer ^Camera camera]}]
  (when camera (.setView renderer camera))
  (.render renderer))

(defn ^:private render-stage!
  "Internal use only"
  [{:keys [^Stage renderer ^Camera camera]}]
  (when camera
    (.setCamera renderer camera)
    (.setViewport renderer (. camera viewportWidth) (. camera viewportHeight)))
  (doto renderer .act .draw))

(defn render!
  "Calls the renderer from `screen` and optionally draws and returns the
`entities`

    (render! screen entities)
"
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
  "The function version of `orthographic`"
  []
  (OrthographicCamera.))

(defmacro orthographic
  "Returns an [OrthographicCamera](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/OrthographicCamera.html)

    (orthographic)
"
  [& options]
  `(let [^OrthographicCamera object# (orthographic*)]
     (u/calls! object# ~@options)))

(defmacro orthographic!
  "Calls a single method on an `orthographic`"
  [screen k & options]
  `(let [^OrthographicCamera object# (u/get-obj ~screen :camera)]
     (u/call! object# ~k ~@options)))

(defn perspective*
  "The function version of `perspective`"
  []
  (PerspectiveCamera.))

(defmacro perspective
  "Returns an [PerspectiveCamera](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/PerspectiveCamera.html)

    (perspective)
"
  [& options]
  `(let [^PerspectiveCamera object# (perspective*)]
     (u/calls! object# ~@options)))

(defmacro perspective!
  "Calls a single method on a `perspective`"
  [screen k & options]
  `(let [^PerspectiveCamera object# (u/get-obj ~screen :camera)]
     (u/call! object# ~k ~@options)))

(defn size!
  "Sets the size of the camera in `screen`

    (size! screen 480 360)
"
  [screen width height]
  (let [^OrthographicCamera camera (u/get-obj screen :camera)]
    (assert camera)
    (.setToOrtho camera false width height)))

(defn width!
  "Sets the width of the camera in `screen`, adjusting the height so the ratio
remains in tact

    (width! screen 480)
"
  [screen new-width]
  (size! screen new-width (* new-width (/ (game :height) (game :width)))))

(defn height!
  "Sets the height of the camera in `screen`, adjusting the width so the ratio
remains in tact

    (height! screen 360)
"
  [screen new-height]
  (size! screen (* new-height (/ (game :width) (game :height))) new-height))

(defn x!
  "Sets only the x position of the camera in `screen`"
  [screen x]
  (let [^Camera camera (u/get-obj screen :camera)]
    (assert camera)
    (set! (. (. camera position) x) x)
    (.update camera)))

(defn y!
  "Sets only the y position of the camera in `screen`"
  [screen y]
  (let [^Camera camera (u/get-obj screen :camera)]
    (assert camera)
    (set! (. (. camera position) y) y)
    (.update camera)))

(defn z!
  "Sets only the z position of the camera in `screen`"
  [screen z]
  (let [^Camera camera (u/get-obj screen :camera)]
    (assert camera)
    (set! (. (. camera position) z) z)
    (.update camera)))

(defn position!
  "Sets the position of the camera in `screen`"
  ([screen x y]
    (position! screen x y nil))
  ([screen x y z]
    (when x (x! screen x))
    (when y (y! screen y))
    (when z (z! screen z))))
