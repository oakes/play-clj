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
  [^SpriteBatch batch {:keys [^Actor object] :as entity}]
  (assert object)
  (doseq [[k v] entity]
    (case k
      :x (.setX object v)
      :y (.setY object v)
      :width (.setWidth object v)
      :height (.setHeight object v)
      nil))
  (.draw object batch 1))

(defn draw-image!
  [^SpriteBatch batch {:keys [^TextureRegion object x y width height]}]
  (assert (and object x y width height))
  (.draw batch object (float x) (float y) (float width) (float height)))

(defn draw-entity!
  [^SpriteBatch batch entity]
  (if (not (map? entity))
    (draw-entity! batch (create-entity entity))
    (case (:type entity)
      :actor
      (draw-actor! batch entity)
      :image
      (draw-image! batch entity)
      nil)))

(defn draw! [{:keys [renderer] :as screen} entities]
  (assert renderer)
  (let [^SpriteBatch batch (sprite-batch screen)]
    (.begin batch)
    (doseq [entity entities]
      (draw-entity! batch entity))
    (.end batch))
  entities)

; textures

(defn image*
  [img]
  (cond
    (string? img)
    (-> ^String img Texture. TextureRegion.)
    (map? img)
    (TextureRegion. ^TextureRegion (:object img))
    :else
    img))

(defmacro image
  [img & options]
  `(create-entity (utils/calls! ^TextureRegion (image* ~img) ~@options)))

(defmacro animation
  [duration images & args]
  `(Animation. ~duration
               (utils/gdx-into-array (map :object ~images))
               (utils/gdx-static-field :graphics :g2d :Animation
                                       ~(or (first args) :normal))))

(defn animation-image
  ([screen ^Animation animation]
    (create-entity (.getKeyFrame animation (:total-time screen) true)))
  ([screen ^Animation animation is-looping?]
    (create-entity (.getKeyFrame animation (:total-time screen) is-looping?))))
