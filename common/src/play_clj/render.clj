(ns play-clj.render)

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

(defn render-tiled-map!
  [{:keys [^BatchTiledMapRenderer renderer ^OrthographicCamera camera]}]
  (.setView renderer camera)
  (.render renderer))

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

(defn resize-camera!
  [{:keys [^Camera camera]} width height]
  (.setToOrtho camera false width height))

; draw entities

(defmulti sprite-batch #(-> % :renderer class) :default nil)

(defmethod sprite-batch nil [screen]
  (SpriteBatch.))

(defmethod sprite-batch BatchTiledMapRenderer [screen]
  (.getSpriteBatch (:renderer screen)))

(defn draw-entities!
  ([screen]
    (draw-entities! screen (:entities screen)))
  ([screen entities]
    (let [batch (sprite-batch screen)]
      (.begin batch)
      (doseq [{:keys [image x y width height]} entities]
        (when (and image x y width height)
          (.draw batch image (float x) (float y) (float width) (float height))))
      (.end batch)
      batch)))
