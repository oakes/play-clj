(in-ns 'play-clj.core)

; drawing

(defmulti sprite-batch #(-> % :renderer class) :default nil)

(defmethod sprite-batch nil [_])

(defmethod sprite-batch BatchTiledMapRenderer
  [{:keys [^BatchTiledMapRenderer renderer]}]
  (.getSpriteBatch renderer))

(defmethod sprite-batch Stage
  [{:keys [^Stage renderer]}]
  (.getSpriteBatch renderer))

(defn draw-actor!
  [^SpriteBatch batch {:keys [^Actor actor] :as entity}]
  (.draw ^Actor actor batch 1))

(defn draw-image!
  [^SpriteBatch batch {:keys [^TextureRegion image x y width height]}]
  (.draw batch image (float x) (float y) (float width) (float height)))

(defn draw-entity!
  [^SpriteBatch batch entity]
  (cond
    (isa? (type entity) Actor)
    (draw-actor! batch {:actor entity})
    (:actor entity)
    (draw-actor! batch entity)
    (:image entity)
    (draw-image! batch entity)))

(defn draw! [{:keys [renderer] :as screen} entities]
  (assert renderer)
  (let [^SpriteBatch batch (sprite-batch screen)]
    (.begin batch)
    (doseq [entity entities]
      (draw-entity! batch entity))
    (.end batch))
  entities)

; textures

(defn create-image*
  [img]
  (cond
    (string? img)
    (-> ^String img Texture. TextureRegion.)
    (isa? img TextureRegion)
    (TextureRegion. ^TextureRegion img)
    :else
    img))

(defmacro create-image
  [img & options]
  `(utils/calls! ^TextureRegion (create-image* ~img) ~@options))

(defmacro image
  [img k & options]
  `(utils/call! ^TextureRegion ~img ~k ~@options))

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
