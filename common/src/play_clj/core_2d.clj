(in-ns 'play-clj.core)

; drawing

(defmulti sprite-batch #(-> % :renderer class) :default nil)

(defmethod sprite-batch nil [screen]
  (SpriteBatch.))

(defmethod sprite-batch BatchTiledMapRenderer [{:keys [renderer]}]
  (assert renderer)
  (.getSpriteBatch renderer))

(defn draw! [screen entities]
  (let [batch (sprite-batch screen)]
    (.begin batch)
    (doseq [{:keys [image x y width height]} entities]
      (when (and image x y width height)
        (.draw batch image (float x) (float y) (float width) (float height))))
    (.end batch))
  entities)

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
               (utils/gdx-into-array ~images)
               (utils/gdx-static-field :graphics :g2d :Animation
                                       ~(or (first args) :normal))))

(defn get-key-frame
  ([screen ^Animation animation]
    (.getKeyFrame animation (:total-time screen) true))
  ([screen ^Animation animation is-looping?]
    (.getKeyFrame animation (:total-time screen) is-looping?)))
