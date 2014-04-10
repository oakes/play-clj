(in-ns 'play-clj.core)

(defmacro pixmap
  "Returns a [Pixmap](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/Pixmap.html).

    (pixmap \"image.png\")"
  [& args]
  `~(if (string? (first args))
      `(Pixmap. (files! :internal ~(first args)))
      `(Pixmap. ~@args)))

(defmacro pixmap!
  "Calls a single method on a `pixmap`.

    (pixmap! object :dispose)"
  [object k & options]
  `(let [^Pixmap object# ~object]
     (u/call! object# ~k ~@options)))

(defmacro shape-type
  "Returns a static field from [ShapeRenderer.ShapeType](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/glutils/ShapeRenderer.ShapeType.html).

    (shape-type :filled)"
  [k]
  `~(u/gdx-field :graphics :glutils "ShapeRenderer$ShapeType" (u/key->pascal k)))

(defn shape*
  ([]
    (ShapeEntity. (ShapeRenderer.)))
  ([max-vertices]
    (ShapeEntity. (ShapeRenderer. max-vertices))))

(defmacro shape
  "Returns an entity based on [ShapeRenderer](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/glutils/ShapeRenderer.html).

    ; create a red rectangle
    (shape :filled
           :set-color (color :red)
           :rect 0 0 10 30)
    ; create an empty shape, then set it to a green rectangle
    (shape (shape :filled)
           :set-color (color :green)
           :rect 0 0 10 30)"
  [type & options]
  (when (seq (clojure.set/intersection #{:begin :end} (set options)))
    (-> "No need to call :begin or :end, because it's done for you."
        Throwable.
        throw))
  `(let [entity# ~(if (keyword? type)
                    `(assoc (shape*) :type (shape-type ~type))
                    type)
         ^ShapeRenderer object# (u/get-obj entity# :object)]
     (assoc entity# :draw! (fn [] (u/calls! object# ~@options)))))

(defmacro shape!
  "Calls a single method on a `shape`."
  [entity k & options]
  `(let [^ShapeRenderer object# (u/get-obj ~entity :object)]
     (u/call! object# ~k ~@options)))

(defn shape?
  "Returns true if `entity` is a `shape`."
  [entity]
  (isa? (type (u/get-obj entity :object)) ShapeRenderer))

; tiled maps

(defn tiled-map*
  ([]
    (TiledMap.))
  ([s]
    (if (string? s)
      (.load (TmxMapLoader.) s)
      s)))

(defmacro tiled-map
  "Returns a [TiledMap](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/maps/tiled/TiledMap.html).
Normally, you don't need to use this directly."
  [s & options]
  `(u/calls! ^TiledMap (tiled-map* ~s) ~@options))

(defmacro tiled-map!
  "Calls a single method on a `tiled-map`.

    (tiled-map! screen :get-layers)"
  [screen k & options]
  `(let [^BatchTiledMapRenderer object# (u/get-obj ~screen :renderer)]
     (u/call! ^TiledMap (.getMap object#) ~k ~@options)))

(defn tiled-map-layers
  "Returns a list with [MapLayer](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/maps/MapLayer.html)
objects cooresponding to each layer in the tiled map in `screen`.

    (tiled-map-layers screen)"
  [screen]
  (let [^BatchTiledMapRenderer renderer (u/get-obj screen :renderer)
        ^MapLayers layers (-> renderer .getMap .getLayers)]
    (for [^long i (range (.getCount layers))]
      (.get layers i))))

(defn tiled-map-layer*
  ([width height tile-width tile-height]
    (TiledMapTileLayer. width height tile-width tile-height))
  ([screen layer]
    (if (isa? (type layer) MapLayer)
      layer
      (->> (tiled-map-layers screen)
           (drop-while #(not= layer (.getName ^MapLayer %)))
           first))))

(defmacro tiled-map-layer
  "Returns a [TiledMapTileLayer](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/maps/tiled/TiledMapTileLayer.html)
from the tiled map in `screen` that matches `layer`.

    (tiled-map-layer screen \"water\")"
  [screen layer & options]
  `(let [^TiledMapTileLayer object# (tiled-map-layer* ~screen ~layer)]
     (u/calls! object# ~@options)))

(defmacro tiled-map-layer!
  "Calls a single method on a `tiled-map-layer`.

    (tiled-map-layer! (tiled-map-layer screen \"water\")
                      :set-cell 0 0 nil)"
  [object k & options]
  `(u/call! ^TiledMapTileLayer (cast TiledMapTileLayer ~object) ~k ~@options))

(defn tiled-map-layer-names
  "Returns a list with strings cooresponding to the name of each layer in the
tiled map in `screen`."
  [screen]
  (for [^MapLayer layer (tiled-map-layers screen)]
    (.getName layer)))

(defn tiled-map-cell*
  ([]
    (TiledMapTileLayer$Cell.))
  ([screen layer x y]
    (.getCell ^TiledMapTileLayer (tiled-map-layer screen layer) x y)))

(defmacro tiled-map-cell
  "Returns a [TiledMapTileLayer.Cell](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/maps/tiled/TiledMapTileLayer.Cell.html)
from the tiled map in `screen` from the given `layer` and position `x` and `y`.

    (tiled-map-cell screen \"water\" 0 0)"
  [screen layer x y & options]
  `(let [^TiledMapTileLayer$Cell object# (tiled-map-cell* ~screen ~layer ~x ~y)]
     (u/calls! object# ~@options)))

(defmacro tiled-map-cell!
  "Calls a single method on a `tiled-map-cell`.

    (tiled-map-cell! (tiled-map-cell screen \"water\" 0 0)
                     :set-rotation 90)"
  [object k & options]
  `(u/call! ^TiledMapTileLayer$Cell ~object ~k ~@options))

(defn map-layer*
  ([]
    (MapLayer.))
  ([screen layer]
    (tiled-map-layer* screen layer)))

(defmacro map-layer
  "Returns a [MapLayer](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/maps/MapLayer.html)
from the tiled map in `screen` that matches `layer`. This is necessary for
non-tile layers, like object and image layers.

    (map-layer screen \"objects\")"
  [screen layer & options]
  `(let [^MapLayer object# (map-layer* ~screen ~layer)]
     (u/calls! object# ~@options)))

(defmacro map-layer!
  "Calls a single method on a `map-layer`.

    (map-layer! (map-layer screen \"objects\")
                :set-visible false)"
  [object k & options]
  `(u/call! ^MapLayer ~object ~k ~@options))

(defn ^:private tiled-map-prop
  [screen]
  (let [^BatchTiledMapRenderer renderer (u/get-obj screen :renderer)
        ^MapProperties prop (-> renderer .getMap .getProperties)]
    {:unit-scale (.getUnitScale renderer)
     :tile-width (.get prop "tilewidth")
     :tile-height (.get prop "tileheight")
     :width (.get prop "width")
     :height (.get prop "height")}))

(defn screen->isometric
  "Returns a copy of the provided map with x/y values converted from screen
to isometric map coordinates.

    (screen->isometric screen {:x 64 :y 32})"
  [screen {:keys [x y] :as entity}]
  (let [{:keys [unit-scale tile-width tile-height]} (tiled-map-prop screen)
        half-tile-width (/ (* tile-width unit-scale) 2)
        half-tile-height (/ (* tile-height unit-scale) 2)]
    (assoc entity
           :x (/ (- (/ x half-tile-width)
                    (/ y half-tile-height))
                 2)
           :y (/ (+ (/ y half-tile-height)
                    (/ x half-tile-width))
                 2))))

(defn isometric->screen
  "Returns a copy of the provided map with x/y values converted from isometric
map to screen coordinates.

    (isometric->screen screen {:x 2 :y 1})"
  [screen {:keys [x y] :as entity}]
  (let [{:keys [unit-scale tile-width tile-height]} (tiled-map-prop screen)
        half-tile-width (/ (* tile-width unit-scale) 2)
        half-tile-height (/ (* tile-height unit-scale) 2)]
    (assoc entity
           :x (+ (* x half-tile-width)
                 (* y half-tile-width))
           :y (+ (* -1 x half-tile-height)
                 (* y half-tile-height)))))

; renderers

(defn orthogonal-tiled-map*
  [path unit]
  (OrthogonalTiledMapRenderer. ^TiledMap (tiled-map* path) ^double unit))

(defmacro orthogonal-tiled-map
  "Returns an [OrthogonalTiledMapRenderer](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/maps/tiled/renderers/OrthogonalTiledMapRenderer.html)
with the tiled map file at `path` and `unit` scale.

    (orthogonal-tiled-map \"level1.tmx\" (/ 1 8))"
  [path unit & options]
  `(u/calls! ^OrthogonalTiledMapRenderer (orthogonal-tiled-map* ~path ~unit)
             ~@options))

(defmacro orthogonal-tiled-map!
  "Calls a single method on an `orthogonal-tiled-map`."
  [screen k & options]
  `(let [^OrthogonalTiledMapRenderer object# (u/get-obj ~screen :renderer)]
     (u/call! object# ~k ~@options)))

(defn isometric-tiled-map*
  [path unit]
  (IsometricTiledMapRenderer. ^TiledMap (tiled-map* path) ^double unit))

(defmacro isometric-tiled-map
  "Returns an [IsometricTiledMapRenderer](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/maps/tiled/renderers/IsometricTiledMapRenderer.html)
with the tiled map file at `path` and `unit` scale.

    (isometric-tiled-map \"level1.tmx\" (/ 1 8))"
  [path unit & options]
  `(u/calls! ^IsometricTiledMapRenderer (isometric-tiled-map* ~path ~unit)
             ~@options))

(defmacro isometric-tiled-map!
  "Calls a single method on an `isometric-tiled-map`."
  [screen k & options]
  `(let [^IsometricTiledMapRenderer object# (u/get-obj ~screen :renderer)]
     (u/call! object# ~k ~@options)))

(defn isometric-staggered-tiled-map*
  [path unit]
  (IsometricStaggeredTiledMapRenderer. ^TiledMap (tiled-map* path)
                                       ^double unit))

(defmacro isometric-staggered-tiled-map
  "Returns an [IsometricStaggeredTiledMapRenderer](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/maps/tiled/renderers/IsometricStaggeredTiledMapRenderer.html)
with the tiled map file at `path` and `unit` scale.

    (isometric-staggered-tiled-map \"level1.tmx\" (/ 1 8))"
  [path unit & options]
  `(u/calls! ^IsometricStaggeredTiledMapRenderer
             (isometric-staggered-tiled-map* ~path ~unit)
             ~@options))

(defmacro isometric-staggered-tiled-map!
  "Calls a single method on an `isometric-staggered-tiled-map`."
  [screen k & options]
  `(let [^IsometricStaggeredTiledMapRenderer object#
         (u/get-obj ~screen :renderer)]
     (u/call! object# ~k ~@options)))

(defn hexagonal-tiled-map*
  [path unit]
  (HexagonalTiledMapRenderer. ^TiledMap (tiled-map* path) ^double unit))

(defmacro hexagonal-tiled-map
  "Returns a [HexagonalTiledMapRenderer](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/maps/tiled/renderers/HexagonalTiledMapRenderer.html)
with the tiled map file at `path` and `unit` scale.

    (hexagonal-tiled-map \"level1.tmx\" (/ 1 8))"
  [path unit & options]
  `(u/calls! ^HexagonalTiledMapRenderer (hexagonal-tiled-map* ~path ~unit)
             ~@options))

(defmacro hexagonal-tiled-map!
  "Calls a single method on a `hexagonal-tiled-map`."
  [screen k & options]
  `(let [^HexagonalTiledMapRenderer object# (u/get-obj ~screen :renderer)]
     (u/call! object# ~k ~@options)))

(defn stage*
  []
  (Stage.))

(defmacro stage
  "Returns a [Stage](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/Stage.html).

    (stage)"
  [& options]
  `(u/calls! ^Stage (stage*) ~@options))

(defmacro stage!
  "Calls a single method on a `stage`."
  [screen k & options]
  `(let [^Stage object# (u/get-obj ~screen :renderer)]
     (u/call! object# ~k ~@options)))

; draw

(defmulti draw!
  (fn [screen _] (-> screen :renderer class)))

(defmethod draw! BatchTiledMapRenderer
  [{:keys [^BatchTiledMapRenderer renderer] :as screen} entities]
  (let [^SpriteBatch batch (.getSpriteBatch renderer)]
    (.begin batch)
    (doseq [entity entities]
      (e/draw-entity! entity screen batch))
    (.end batch))
  entities)

(defmethod draw! Stage
  [{:keys [^Stage renderer] :as screen} entities]
  (let [^SpriteBatch batch (.getSpriteBatch renderer)]
    (.begin batch)
    (doseq [entity entities]
      (e/draw-entity! entity screen batch))
    (.end batch))
  entities)

(defmethod draw! ModelBatch
  [{:keys [^ModelBatch renderer ^Camera camera] :as screen} entities]
  (.begin renderer camera)
  (doseq [entity entities]
    (e/draw-entity! entity screen nil))
  (.end renderer)
  entities)

; render

(defn render-map!
  "Calls the tiled-map renderer from `screen`, optionally allowing you to
specify which layers to render with or without.

    (render-map! screen :with \"water\" \"grass\")
    (render-map! screen :without \"desert\" \"rocks\")"
  [{:keys [^BatchTiledMapRenderer renderer ^Camera camera] :as screen}
   & [k & layer-names]]
  (when camera (.setView renderer camera))
  (if k
    (let [all-layer-names (tiled-map-layer-names screen)]
      ; make sure the layer names exist
      (doseq [n layer-names]
        (when-not (contains? (set all-layer-names) n)
          (throw (Exception. (format "Layer \"%s\" does not exist." n)))))
      ; render with or without the supplied layers
      (->> (case k
             :with (set layer-names)
             :without (clojure.set/difference (set all-layer-names)
                                              (set layer-names))
             (u/throw-key-not-found k))
           (map #(.indexOf ^java.util.List all-layer-names %))
           int-array
           (.render renderer)))
    (.render renderer))
  nil)

(defn render-stage!
  "Calls the stage renderer from `screen`."
  [{:keys [^Stage renderer] :as screen}]
  (doto renderer .act .draw)
  nil)

(defn render!
  "Calls the renderer from `screen` and optionally draws and returns the
`entities`.

    (render! screen entities)"
  ([{:keys [renderer] :as screen}]
    (cond
      (isa? (type renderer) BatchTiledMapRenderer)
      (render-map! screen)
      (isa? (type renderer) Stage)
      (render-stage! screen)))
  ([screen entities]
    (render! screen)
    (draw! screen entities)))

(defn ^:private create-layer
  [^TiledMapTileLayer layer]
  (TiledMapTileLayer. (.getWidth layer)
                      (.getHeight layer)
                      (int (.getTileWidth layer))
                      (int (.getTileHeight layer))))

(defn ^:private isometric?
  [{:keys [renderer] :as screen}]
  (or (isa? (type renderer) IsometricTiledMapRenderer)
      (isa? (type renderer) IsometricStaggeredTiledMapRenderer)))

(defn ^:private split-layer
  [screen layer-name]
  (let [^TiledMapTileLayer l (tiled-map-layer screen layer-name)]
    (reduce (fn [layers {:keys [x y] :as map-tile}]
              (let [screen-tile (if (isometric? screen)
                                  (isometric->screen screen map-tile)
                                  map-tile)
                    new-layer (or (->> layers
                                       (filter #(= (:y screen-tile) (:y %)))
                                       first)
                                  (assoc screen-tile :layer (create-layer l)))]
                (->> (tiled-map-layer! l :get-cell x y)
                     (tiled-map-layer! (:layer new-layer) :set-cell x y))
                (if (contains? (set layers) new-layer)
                  layers
                  (conj layers new-layer))))
            []
            (for [x (range (- (.getWidth l) 1) -1 -1)
                  y (range (- (.getHeight l) 1) -1 -1)]
              {:x x :y y}))))

(defn ^:private sort-by-y
  [entities]
  (sort-by :y #(compare %2 %1) entities))

(defn render-sorted!
  "Draws the supplied tiled-map layers and entities. If no sort function is
supplied, they will be sorted by :y (latitude).

    (render-sorted! screen [\"walls\"] entities)
    (render-sorted! screen #(sort-by :x %) [\"walls\"] entities)"
  ([screen layer-names entities]
    (render-sorted! sort-by-y screen layer-names entities))
  ([{:keys [^BatchTiledMapRenderer renderer
            ^Camera camera
            update-fn!]
     :as screen}
    sort-fn layer-names entities]
    (doseq [ln layer-names]
      (when-not (get-in screen [:layers ln])
        (update-fn! assoc-in [[:layers ln] (split-layer screen ln)])))
    (when camera (.setView renderer camera))
    (let [^SpriteBatch batch (.getSpriteBatch renderer)]
      (.begin batch)
      (doseq [entity (->> (map #(get-in screen [:layers %]) layer-names)
                          (apply concat entities)
                          sort-fn)]
        (if-let [layer (:layer entity)]
          (.renderTileLayer renderer layer)
          (e/draw-entity! entity screen batch)))
      (.end batch))
    entities))
