(in-ns 'play-clj.core)

; drawing

(defmulti sprite-batch #(-> % :renderer class) :default nil)

(defmethod sprite-batch nil
  [screen]
  (SpriteBatch.))

(defmethod sprite-batch BatchTiledMapRenderer
  [{:keys [^BatchTiledMapRenderer renderer]}]
  (.getSpriteBatch renderer))

(defmethod sprite-batch Stage
  [{:keys [^Stage renderer]}]
  (.getSpriteBatch renderer))

(defn draw-actor!
  [^SpriteBatch batch {:keys [^Actor actor] :as entity}]
  (doseq [[k v] entity]
    (case k
      :x (.setX actor v)
      :y (.setY actor v)
      :width (.setWidth actor v)
      :height (.setHeight actor v)
      nil))
  (.draw ^Actor actor batch 1))

(defn draw-image!
  [^SpriteBatch batch {:keys [^TextureRegion image x y width height]}]
  (.draw batch image (float x) (float y) (float width) (float height)))

(defn draw-entity!
  [^SpriteBatch batch entity]
  (cond
    (:actor entity)
    (draw-actor! batch entity)
    (:image entity)
    (draw-image! batch entity)
    (isa? (type entity) Actor)
    (draw-actor! batch {:actor entity})))

(defn draw! [{:keys [renderer] :as screen} entities]
  (assert renderer)
  (let [^SpriteBatch batch (sprite-batch screen)]
    (.begin batch)
    (doseq [entity entities]
      (draw-entity! batch entity))
    (.end batch))
  entities)

; textures

(defn image
  [val & {:keys [] :as options}]
  (let [^TextureRegion
        img (if (string? val)
              (-> ^String val Texture. TextureRegion.)
              (TextureRegion. ^TextureRegion val))]
    (doseq [[k v] options]
      (case k
        :x (.setRegionX img v)
        :y (.setRegionY img v)
        :width (.setRegionWidth img v)
        :height (.setRegionHeight img v)
        :region (.setRegion img
                  ^long (nth v 0) ^long (nth v 1)
                  ^long (nth v 2) ^long (nth v 3))
        nil))
    img))

(defn image-width
  [^TextureRegion img]
  (.getRegionWidth img))

(defn image-height
  [^TextureRegion img]
  (.getRegionHeight img))

(defn split-image
  ([val size]
    (split-image val size size))
  ([val width height]
    (-> val ^TextureRegion image (.split width height))))

(defn flip-image
  [val x? y?]
  (doto ^TextureRegion (image val) (.flip x? y?)))

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
