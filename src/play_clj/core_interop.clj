(in-ns 'play-clj.core)

; 2d

(defmacro image!
  [entity k & options]
  `(utils/call! ^TextureRegion (:object ~entity) ~k ~@options))

; render

(defmacro tiled-map-renderer!
  [{:keys [renderer]} k & options]
  `(utils/call! ^BatchTiledMapRenderer ~renderer ~k ~@options))

(defmacro stage!
  [{:keys [renderer]} k & options]
  `(utils/call! ^Stage ~renderer ~k ~@options))

(defmacro orthographic-camera!
  [{:keys [camera]} k & options]
  `(utils/call! ^OrthographicCamera ~camera ~k ~@options))

(defmacro perspective-camera!
  [{:keys [camera]} k & options]
  `(utils/call! ^PerspectiveCamera ~camera ~k ~@options))

; ui

