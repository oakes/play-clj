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

(defmacro check-box!
  [entity k & options]
  `(utils/call! ^Checkbox (:object ~entity) ~k ~@options))

(defmacro image-button!
  [entity k & options]
  `(utils/call! ^ImageButton (:object ~entity) ~k ~@options))

(defmacro image-text-button!
  [entity k & options]
  `(utils/call! ^ImageTextButton (:object ~entity) ~k ~@options))

(defmacro label!
  [entity k & options]
  `(utils/call! ^Label (:object ~entity) ~k ~@options))

(defmacro text-button!
  [entity k & options]
  `(utils/call! ^TextButton (:object ~entity) ~k ~@options))

(defmacro text-field!
  [entity k & options]
  `(utils/call! ^TextField (:object ~entity) ~k ~@options))

(defmacro dialog!
  [entity k & options]
  `(utils/call! ^Dialog (:object ~entity) ~k ~@options))
