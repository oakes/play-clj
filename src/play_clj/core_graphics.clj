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
(normally, you don't need to use this directly, because the *-tiled-map
macros that create the renderers will call this themselves)"
  [s & options]
  `(u/calls! ^TiledMap (tiled-map* ~s) ~@options))

(defmacro tiled-map!
  "Calls a single method on a `tiled-map`

    (tiled-map! screen :get-layers)"
  [screen k & options]
  `(let [^BatchTiledMapRenderer object# (u/get-obj ~screen :renderer)]
     (u/call! ^TiledMap (.getMap object#) ~k ~@options)))

(defn tiled-map-layers
  "Returns a list with [MapLayer](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/maps/MapLayer.html)
objects cooresponding to each layer in the tiled map in `screen`

    (tiled-map-layers screen)"
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

    (tiled-map-layer screen \"water\")"
  [screen layer & options]
  `(let [^TiledMapTileLayer object# (tiled-map-layer* ~screen ~layer)]
     (u/calls! object# ~@options)))

(defmacro tiled-map-layer!
  "Calls a single method on a `tiled-map-layer`

    (tiled-map-layer! (tiled-map-layer screen \"water\")
                      :set-cell 0 0 nil)"
  [object k & options]
  `(u/call! ^TiledMapTileLayer (cast TiledMapTileLayer ~object) ~k ~@options))

(defn tiled-map-layer-names
  "Returns a list with strings cooresponding to the name of each layer in the
tiled map in `screen`"
  [screen]
  (for [layer (tiled-map-layers screen)]
    (tiled-map-layer! layer :get-name)))

(defn tiled-map-cell*
  "The function version of `tiled-map-cell`"
  [screen layer x y]
  (.getCell ^TiledMapTileLayer (tiled-map-layer screen layer) x y))

(defmacro tiled-map-cell
  "Returns a [TiledMapTileLayer.Cell](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/maps/tiled/TiledMapTileLayer.Cell.html)
from the tiled map in `screen` from the given `layer` and position `x` and `y`

    (tiled-map-cell screen \"water\" 0 0)"
  [screen layer x y & options]
  `(let [^TiledMapTileLayer$Cell object# (tiled-map-cell* ~screen ~layer ~x ~y)]
     (u/calls! object# ~@options)))

(defmacro tiled-map-cell!
  "Calls a single method on a `tiled-map-cell`

    (tiled-map-cell! (tiled-map-cell screen \"water\" 0 0)
                     :set-rotation 90)"
  [object k & options]
  `(u/call! ^TiledMapTileLayer$Cell ~object ~k ~@options))

(defn ^:private tiled-map-prop
  "Internal use only"
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
to isometric map coordinates

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
map to screen coordinates

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
  "The function version of `orthogonal-tiled-map`"
  [path unit]
  (OrthogonalTiledMapRenderer. ^TiledMap (tiled-map* path) ^double unit))

(defmacro orthogonal-tiled-map
  "Returns an [OrthogonalTiledMapRenderer](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/maps/tiled/renderers/OrthogonalTiledMapRenderer.html)
with the tiled map file at `path` and `unit` scale

    (orthogonal-tiled-map \"level1.tmx\" (/ 1 8))"
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

    (isometric-tiled-map \"level1.tmx\" (/ 1 8))"
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

    (isometric-staggered-tiled-map \"level1.tmx\" (/ 1 8))"
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

    (hexagonal-tiled-map \"level1.tmx\" (/ 1 8))"
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

    (stage)"
  [& options]
  `(u/calls! ^Stage (stage*) ~@options))

(defmacro stage!
  "Calls a single method on a `stage`"
  [screen k & options]
  `(let [^Stage object# (u/get-obj ~screen :renderer)]
     (u/call! object# ~k ~@options)))

; draw

(defmulti draw!
  "Internal use only"
  (fn [screen _] (-> screen :renderer class)))

(defmethod draw! BatchTiledMapRenderer
  [{:keys [^BatchTiledMapRenderer renderer]} entities]
  (let [^SpriteBatch batch (.getSpriteBatch renderer)]
    (.begin batch)
    (doseq [entity entities]
      (u/draw-entity! entity batch))
    (.end batch))
  entities)

(defmethod draw! Stage
  [{:keys [^Stage renderer]} entities]
  (let [^SpriteBatch batch (.getSpriteBatch renderer)]
    (.begin batch)
    (doseq [entity entities]
      (u/draw-entity! entity batch))
    (.end batch))
  entities)

(defmethod draw! ModelBatch
  [{:keys [^ModelBatch renderer ^Camera camera] :as screen} entities]
  (.begin renderer camera)
  (doseq [entity entities]
    (u/draw-entity! entity screen))
  (.end renderer)
  entities)

; render

(defn render-map!
  "Calls the tiled-map renderer from `screen`, optionally allowing you to
specify which layers to render with or without

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
  "Calls the stage renderer from `screen`"
  [{:keys [^Stage renderer ^Camera camera]}]
  (when camera
    (.setCamera renderer camera)
    (.setViewport renderer (. camera viewportWidth) (. camera viewportHeight)))
  (doto renderer .act .draw)
  nil)

(defn render!
  "Calls the renderer from `screen` and optionally draws and returns the
`entities`

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
  "Internal use only"
  [^TiledMapTileLayer layer]
  (TiledMapTileLayer. (.getWidth layer)
                      (.getHeight layer)
                      (int (.getTileWidth layer))
                      (int (.getTileHeight layer))))

(defn ^:private isometric?
  "Internal use only"
  [{:keys [renderer] :as screen}]
  (or (isa? (type renderer) IsometricTiledMapRenderer)
      (isa? (type renderer) IsometricStaggeredTiledMapRenderer)))

(defn ^:private split-layer
  "Internal use only"
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

(defn render-sorted!
  "Draws the supplied tiled-map layers and entities, sorted by latitude

    (render-sorted! screen [\"walls\"] entities)"
  [{:keys [^BatchTiledMapRenderer renderer
           ^Camera camera
           update-fn!]
    :as screen}
   layer-names entities]
  (doseq [ln layer-names]
    (when-not (get-in screen [:layers ln])
      (update-fn! assoc-in [[:layers ln] (split-layer screen ln)])))
  (when camera (.setView renderer camera))
  (let [^SpriteBatch batch (.getSpriteBatch renderer)]
    (.begin batch)
    (doseq [entity (->> (map #(get-in screen [:layers %]) layer-names)
                        (apply concat entities)
                        (sort-by :y #(compare %2 %1)))]
      (if-let [layer (:layer entity)]
        (.renderTileLayer renderer layer)
        (u/draw-entity! entity batch)))
    (.end batch))
  entities)
