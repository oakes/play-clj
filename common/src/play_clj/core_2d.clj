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
  [val]
  (if (string? val)
    (-> val Texture. TextureRegion.)
    (TextureRegion. val)))

(defn split-image
  ([val size]
    (split-image val size size))
  ([val width height]
    (-> val image (.split width height))))

(defn flip-image
  [val x? y?]
  (doto (image val) (.flip x? y?)))

(defmacro animation
  [duration images & args]
  `(Animation. ~duration
               (utils/into-gdx-array ~images)
               (utils/static-field :graphics :g2d :Animation
                                   ~(or (first args) :normal))))

(defn get-animation-frame
  ([screen ^Animation animation]
    (get-animation-frame screen animation true))
  ([screen ^Animation animation is-looping?]
    (.getKeyFrame animation (:total-time screen) is-looping?)))
