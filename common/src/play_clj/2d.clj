(in-ns 'play-clj.core)

; drawing

(defmulti sprite-batch #(-> % :renderer class) :default nil)

(defmethod sprite-batch nil [screen]
  (SpriteBatch.))

(defmethod sprite-batch BatchTiledMapRenderer [screen]
  (.getSpriteBatch (:renderer screen)))

(defn draw!
  ([screen]
    (draw! screen (:entities screen)))
  ([screen entities]
    (let [batch (sprite-batch screen)]
      (.begin batch)
      (doseq [{:keys [image x y width height]} entities]
        (when (and image x y width height)
          (.draw batch image (float x) (float y) (float width) (float height))))
      (.end batch)
      batch)))

; textures

(defn image
  [^String internal-path]
  (-> internal-path Texture. TextureRegion.))

(defn split-image
  ([^String internal-path size]
    (split-image internal-path size size))
  ([^String internal-path width height]
    (-> internal-path image (.split width height))))

(defmacro animation
  [& args]
  `(Animation. ~@args))

(defn get-animation-frame
  ([screen ^Animation animation]
    (get-animation-frame screen animation true))
  ([screen ^Animation animation is-looping?]
    (.getKeyFrame animation (:total-time screen) is-looping?)))
