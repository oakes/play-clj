(in-ns 'play-clj.core)

; 2d

(defmacro image!
  [entity k & options]
  `(utils/call! ^TextureRegion (:object ~entity) ~k ~@options))

; render

(defmacro orthogonal-tiled-map!
  [screen k & options]
  `(utils/call! ^OrthogonalTiledMapRenderer (:renderer ~screen) ~k ~@options))

(defmacro isometric-tiled-map!
  [screen k & options]
  `(utils/call! ^IsometricTiledMapRenderer (:renderer ~screen) ~k ~@options))

(defmacro isometric-staggered-tiled-map!
  [screen k & options]
  `(utils/call! ^IsometricStaggeredTiledMapRenderer (:renderer ~screen)
                ~k ~@options))

(defmacro hexagonal-tiled-map!
  [screen k & options]
  `(utils/call! ^HexagonalTiledMapRenderer (:renderer ~screen) ~k ~@options))

(defmacro stage!
  [screen k & options]
  `(utils/call! ^Stage (:renderer ~screen) ~k ~@options))

(defmacro orthographic-camera!
  [screen k & options]
  `(utils/call! ^OrthographicCamera (:camera ~screen) ~k ~@options))

(defmacro perspective-camera!
  [screen k & options]
  `(utils/call! ^PerspectiveCamera (:camera ~screen) ~k ~@options))

; global

(defmacro app!
  [k & options]
  `(utils/call! ^Application (Gdx/app) ~k ~@options))

(defmacro audio!
  [k & options]
  `(utils/call! ^Audio (Gdx/audio) ~k ~@options))

(defmacro files!
  [k & options]
  `(utils/call! ^Files (Gdx/files) ~k ~@options))

(defmacro gl!
  [k & options]
  `(utils/call! ^GL20 (Gdx/gl20) ~k ~@options))

(defmacro graphics!
  [k & options]
  `(utils/call! ^Graphics (Gdx/graphics) ~k ~@options))

(defmacro input!
  [k & options]
  `(utils/call! ^Input (Gdx/input) ~k ~@options))

(defmacro net!
  [k & options]
  `(utils/call! ^Net (Gdx/net) ~k ~@options))

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

(defmacro slider!
  [entity k & options]
  `(utils/call! ^Slider (:object ~entity) ~k ~@options))

(defmacro text-button!
  [entity k & options]
  `(utils/call! ^TextButton (:object ~entity) ~k ~@options))

(defmacro text-field!
  [entity k & options]
  `(utils/call! ^TextField (:object ~entity) ~k ~@options))

(defmacro dialog!
  [entity k & options]
  `(utils/call! ^Dialog (:object ~entity) ~k ~@options))
