(ns play-clj.render)

(in-ns 'play-clj.core)

; tiled map renderers

(defmulti create-tiled-map-renderer :type :default :orthogonal)

(defmethod create-tiled-map-renderer :orthogonal [options]
  (OrthogonalTiledMapRenderer. (:map options) (:unit-scale options)))

(defmethod create-tiled-map-renderer :isometric [options]
  (IsometricTiledMapRenderer. (:map options) (:unit-scale options)))

(defmethod create-tiled-map-renderer :isometric-staggered [options]
  (IsometricStaggeredTiledMapRenderer. (:map options) (:unit-scale options)))

(defmethod create-tiled-map-renderer :hexagonal [options]
  (HexagonalTiledMapRenderer. (:map options) (:unit-scale options)))

(defn tiled-map
  [& {:keys [file tile-size type] :as options}]
  (assert (string? file))
  (assert (number? tile-size))
  (fn []
    (let [tiled-map (.load (TmxMapLoader.) file)
          unit-scale (float (/ 1 tile-size))
          options (assoc options
                         :map tiled-map
                         :unit-scale unit-scale)]
      (create-tiled-map-renderer options))))

(defn render-tiled-map!
  [{:keys [^BatchTiledMapRenderer renderer ^OrthographicCamera camera]}]
  (.setView renderer camera)
  (.render renderer))

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

(defmulti sprite-batch (fn [screen] (class (:renderer screen))) :default nil)

(defmethod sprite-batch nil [screen]
  (SpriteBatch.))

(defmethod sprite-batch BatchTiledMapRenderer [screen]
  (.getSpriteBatch (:renderer screen)))

(defn draw-entities!
  [screen entities]
  (let [batch (sprite-batch screen)]
    (.begin batch)
    (doseq [{:keys [image x y width height]} entities]
      (when (and image x y width height)
        (.draw batch image x y width height)))
    (.end batch)))
