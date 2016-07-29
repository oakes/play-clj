(in-ns 'play-clj.core)

(defmacro pixmap-format
  "Returns a static field from [Pixmap.Format](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/Pixmap.Format.html).

    (pixmap-format :alpha)"
  [k]
  (u/gdx-field :graphics "Pixmap$Format" (u/key->pascal k)))

(defn pixmap*
  ([^String path]
   (or (u/load-asset path Pixmap)
       (Pixmap. (files! :internal path))))
  ([width height fmt]
   (Pixmap. width height fmt)))

(defmacro pixmap
  "Returns a [Pixmap](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/Pixmap.html).

    (pixmap \"image.png\")"
  [path & options]
  `(let [^Pixmap object# (pixmap* ~path)]
     (u/calls! object# ~@options)))

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
  (u/gdx-field :graphics :glutils "ShapeRenderer$ShapeType" (u/key->pascal k)))

(defn shape*
  ([]
   (ShapeEntity. (ShapeRenderer.)))
  ([max-vertices]
   (ShapeEntity. (ShapeRenderer. max-vertices))))

(defmacro shape
  "Returns an entity based on [ShapeRenderer](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/glutils/ShapeRenderer.html).
You may pass in a type (see `shape-type`) or an existing `shape` entity that you
want to modify.

A `shape` can draw multiple sub-shapes internally, allowing you to create more
complicated shapes. If you use `assoc` to set the overall :x and :y of the
`shape`, each sub-shape's x and y position will be relative to it.

    ; create a green and red rectangle
    (shape :filled
           :set-color (color :green)
           :rect 0 0 10 30
           :set-color (color :red)
           :rect 10 0 10 30)
    ; create an empty shape, then set it to a green rectangle
    (shape (shape :filled)
           :set-color (color :green)
           :rect 0 0 10 30)
    ; create a green rectangle at 10,10 and rotate it 45 degrees
    (assoc (shape :filled
                  :set-color (color :green)
                  :rect 0 0 10 30)
           :x 10
           :y 10
           :angle 45)"
  [type & options]
  (assert (empty? (clojure.set/intersection #{:begin :end} (set options)))
          "No need to call :begin or :end, because it's done for you.")
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
  (instance? ShapeRenderer (u/get-obj entity :object)))

; tiled maps

(defn tiled-map*
  ([]
   (TiledMap.))
  ([^String path]
   (or (u/load-asset path TiledMap)
       (.load (TmxMapLoader.) path))))

(defmacro tiled-map
  "Returns a [TiledMap](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/maps/tiled/TiledMap.html).
Normally, you don't need to use this directly."
  [s & options]
  `(u/calls! ^TiledMap (tiled-map* ~s) ~@options))

(defmacro tiled-map!
  "Calls a single method on a `tiled-map`.

    (tiled-map! screen :get-layers)"
  [screen k & options]
  `(let [^BatchTiledMapRenderer renderer# (u/get-obj ~screen :renderer)]
     (u/call! ^TiledMap (.getMap renderer#) ~k ~@options)))

(defn tiled-map-layer*
  ([width height tile-width tile-height]
   (TiledMapTileLayer. width height tile-width tile-height))
  ([screen layer]
   (if (instance? MapLayer layer)
       layer
       (let [layers (-> ^BatchTiledMapRenderer (u/get-obj screen :renderer)
                        .getMap
                        .getLayers)]
         (if (number? layer)
           (.get layers (int layer))
           (.get layers (str layer)))))))

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

(defn tiled-map-cell*
  ([]
   (TiledMapTileLayer$Cell.))
  ([^TiledMapTileLayer layer x y]
   (.getCell layer x y)))

(defmacro tiled-map-cell
  "Returns a [TiledMapTileLayer.Cell](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/maps/tiled/TiledMapTileLayer.Cell.html)
from the `layer` at position `x` and `y`.

    (tiled-map-cell (tiled-map-layer screen \"water\") 0 0)"
  [layer x y & options]
  `(let [^TiledMapTileLayer$Cell object# (tiled-map-cell* ~layer ~x ~y)]
     (u/calls! object# ~@options)))

(defmacro tiled-map-cell!
  "Calls a single method on a `tiled-map-cell`.

    (-> (tiled-map-layer screen \"water\")
        (tiled-map-cell 0 0)
        (tiled-map-cell! :set-rotation 90))"
  [object k & options]
  `(u/call! ^TiledMapTileLayer$Cell ~object ~k ~@options))

(defn map-layers*
  ([]
   (MapLayers.))
  ([screen]
   (let [^BatchTiledMapRenderer renderer (u/get-obj screen :renderer)]
     (-> renderer .getMap .getLayers))))

(defmacro map-layers
  "Returns the [MapLayers](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/maps/MapLayers.html)
in the tiled map in `screen`.

    (map-layers screen)"
  [screen & options]
  `(let [^MapLayers object# (map-layers* ~screen)]
     (u/calls! object# ~@options)))

(defmacro map-layers!
  "Calls a single method on a `map-layers`.

    (map-layers! object :remove (map-layer screen \"objects\"))"
  [object k & options]
  `(u/call! ^MapLayers ~object ~k ~@options))

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

(defn map-layer-names
  "Returns a list with strings cooresponding to the name of each layer in the
tiled map in `screen`."
  [screen]
  (doall
    (for [^MapLayer layer (map-layers screen)]
      (.getName layer))))

(defn map-objects*
  ([]
   (MapObjects.))
  ([^MapLayer layer]
   (.getObjects layer)))

(defmacro map-objects
  "Returns the [MapObjects](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/maps/MapObjects.html)
in the `layer`.

    (map-objects layer)"
  [^MapLayer layer & options]
  `(let [^MapObjects object# (map-objects* ~layer)]
     (u/calls! object# ~@options)))

(defmacro map-objects!
  "Calls a single method on a `map-objects`.

    (map-objects! (map-objects layer) :remove (map-object layer 0))"
  [object k & options]
  `(u/call! ^MapObjects ~object ~k ~@options))

(defn ^:private map-object-init
  [k]
  (u/gdx :maps :objects (str (u/key->pascal k) "MapObject.")))

(defn map-object*
  []
  (MapObject.))

(defmacro map-object
  "Returns a subclass of [MapObject](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/maps/MapObject.html).

    (map-object :circle)"
  [type & options]
  `(let [^MapObject object# (~(map-object-init type))]
     (u/calls! object# ~@options)))

(defmacro map-object!
  "Calls a single method on a `map-object`.

    (map-object! (map-object :rectangle) :get-rectangle)"
  [object k & options]
  `(u/call! ^MapObject ~object ~k ~@options))

; coordinates

(defn screen->input
  "Returns a map with the provided x,y,z values converted from screen to input
coordinates.

    (screen->input screen {:x 10 :y 10 :z 0})
    (screen->input screen 10 10)
    (screen->input screen 10 10 0)"
  ([screen {:keys [x y z] :or {x 0 y 0 z 0} :as entity}]
   (try
     (let [^Camera camera (u/get-obj screen :camera)
           coords (m/vector-3 x y z)]
       (.project camera coords)
       (assoc entity
              :x (. coords x)
              :y (. coords y)
              :z (. coords z)))
      ; if there's no camera, just flip the y axis
     (catch Exception _
       (assoc entity :y (- (game :height) y)))))
  ([screen x y]
   (screen->input screen {:x x :y y}))
  ([screen x y z]
   (screen->input screen {:x x :y y :z z})))

(defn input->screen
  "Returns a map with the provided x,y,z values converted from input to screen
coordinates.

    (input->screen screen {:x 10 :y 10 :z 0})
    (input->screen screen 10 10)
    (input->screen screen 10 10 0)"
  ([screen {:keys [x y z] :or {x 0 y 0 z 0} :as entity}]
   (try
     (let [^Camera camera (u/get-obj screen :camera)
           coords (m/vector-3 x y z)]
       (.unproject camera coords)
       (assoc entity
              :x (. coords x)
              :y (. coords y)
              :z (. coords z)))
      ; if there's no camera, just flip the y axis
     (catch Exception _
       (assoc entity :y (- (game :height) y)))))
  ([screen x y]
   (input->screen screen {:x x :y y}))
  ([screen x y z]
   (input->screen screen {:x x :y y :z z})))

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
  "Returns a map with the provided x,y values converted from screen to isometric
map coordinates.

    (screen->isometric screen {:x 64 :y 32})
    (screen->isometric screen 64 32)"
  ([screen {:keys [x y] :or {x 0 y 0} :as entity}]
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
  ([screen x y]
   (screen->isometric screen {:x x :y y})))

(defn isometric->screen
  "Returns a map with the provided x,y values converted from isometric map to
screen coordinates.

    (isometric->screen screen {:x 2 :y 1})
    (isometric->screen screen 2 1)"
  ([screen {:keys [x y] :as entity}]
   (let [{:keys [unit-scale tile-width tile-height]} (tiled-map-prop screen)
         half-tile-width (/ (* tile-width unit-scale) 2)
         half-tile-height (/ (* tile-height unit-scale) 2)]
     (assoc entity
            :x (+ (* x half-tile-width)
                  (* y half-tile-width))
            :y (+ (* -1 x half-tile-height)
                  (* y half-tile-height)))))
  ([screen x y]
   (isometric->screen screen {:x x :y y})))

; renderers

(defn orthogonal-tiled-map*
  [path unit]
  (OrthogonalTiledMapRenderer. ^TiledMap (if (string? path)
                                           (tiled-map* path)
                                           path)
                               (float unit)))

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
  (IsometricTiledMapRenderer. ^TiledMap (if (string? path)
                                          (tiled-map* path)
                                          path)
                              (float unit)))

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
  (IsometricStaggeredTiledMapRenderer. ^TiledMap (if (string? path)
                                                   (tiled-map* path)
                                                   path)
                                       (float unit)))

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
  (HexagonalTiledMapRenderer. ^TiledMap (if (string? path)
                                          (tiled-map* path)
                                          path)
                              (float unit)))

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
  (let [^Batch batch (.getBatch renderer)]
    (.begin batch)
    (doseq [entity entities]
      (e/draw! entity screen batch))
    (.end batch))
  entities)

(defmethod draw! Stage
  [{:keys [^Stage renderer] :as screen} entities]
  (let [^Batch batch (.getBatch renderer)]
    (.begin batch)
    (doseq [{:keys [additive? opacity] :as entity} entities]
      (when additive?
        (.setBlendFunction ^Batch batch (gl :gl-src-alpha) (gl :gl-one)))
      (.setColor batch (color 1 1 1 (or opacity 1.0)))
      (e/draw! entity screen batch)
      (.setColor batch (color 1 1 1 1))
      (when additive?
        (.setBlendFunction ^Batch batch (gl :gl-src-alpha) (gl :gl-one-minus-src-alpha))))
    (.end batch))
  entities)

(defmethod draw! ModelBatch
  [{:keys [^ModelBatch renderer ^Camera camera] :as screen} entities]
  (.begin renderer camera)
  (doseq [entity entities]
    (e/draw! entity screen nil))
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
    (let [all-layer-names (map-layer-names screen)]
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
           (sort)
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
     (instance? BatchTiledMapRenderer renderer)
     (render-map! screen)
     (instance? Stage renderer)
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
  (or (instance? IsometricTiledMapRenderer renderer)
      (instance? IsometricStaggeredTiledMapRenderer renderer)))

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
  "Draws the specified layer tiles and entities sorted by their position on the
y axis. A custom sort function may be provided. This is primarily intended for
games with isometric tiled maps, where the layer tiles often need to be sorted
to overlap correctly with the entities.

    (render-sorted! screen [\"walls\"] entities)
    (render-sorted! screen #(sort-by :y %) [\"walls\"] entities)"
  ([screen layer-names entities]
   (render-sorted! screen sort-by-y layer-names entities))
  ([{:keys [^BatchTiledMapRenderer renderer
            ^Camera camera
            update-fn!]
     :as screen}
    sort-fn layer-names entities]
   (doseq [ln layer-names]
     (when-not (get-in screen [:layers ln])
       (update-fn! assoc-in [:layers ln] (split-layer screen ln))))
   (when camera (.setView renderer camera))
   (let [^Batch batch (.getBatch renderer)]
     (.begin batch)
     (doseq [entity (->> (map #(get-in screen [:layers %]) layer-names)
                         (apply concat entities)
                         sort-fn)]
       (if-let [layer (:layer entity)]
         (.renderTileLayer renderer layer)
         (e/draw! entity screen batch)))
     (.end batch))
   entities))
