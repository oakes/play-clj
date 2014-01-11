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

(defn draw-texture!
  [^SpriteBatch batch {:keys [^TextureRegion object x y width height]}]
  (assert (and object x y width height))
  (.draw batch object (float x) (float y) (float width) (float height)))

(defn draw-entity!
  [^SpriteBatch batch entity]
  (if (not (map? entity))
    (draw-entity! batch (u/create-entity entity))
    (case (:type entity)
      :actor
      (draw-actor! batch entity)
      :texture
      (draw-texture! batch entity)
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

(defn texture*
  [img]
  (cond
    (string? img)
    (-> ^String img Texture. TextureRegion.)
    (map? img)
    (TextureRegion. ^TextureRegion (:object img))
    :else
    img))

(defmacro texture
  [img & options]
  `(u/create-entity (u/calls! ^TextureRegion (texture* ~img) ~@options)))

(defmacro animation
  [duration textures & args]
  `(Animation. ~duration
               (u/gdx-into-array (map :object ~textures))
               (u/gdx-static-field :graphics :g2d :Animation
                                   ~(or (first args) :normal))))

(defn animation-texture
  ([{:keys [total-time]} ^Animation animation]
    (u/create-entity (.getKeyFrame animation total-time true)))
  ([{:keys [total-time]} ^Animation animation is-looping?]
    (u/create-entity (.getKeyFrame animation total-time is-looping?))))

; interop

(defmacro texture!
  [entity k & options]
  `(u/call! ^TextureRegion (:object ~entity) ~k ~@options))
