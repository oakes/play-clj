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
  [arg]
  (u/create-entity
    (cond
      (string? arg)
      (-> ^String arg Texture. TextureRegion.)
      (map? arg)
      (TextureRegion. ^TextureRegion (:object arg))
      :else
      arg)))

(defmacro texture
  [arg & options]
  `(let [entity# (texture* ~arg)]
     (u/calls! ^TextureRegion (:object entity#) ~@options)
     entity#))

(defmacro texture!
  [entity k & options]
  `(u/call! ^TextureRegion (:object ~entity) ~k ~@options))

(defmacro play-mode
  [key]
  `(u/static-field-upper :graphics :g2d :Animation ~key))

(defn animation*
  [duration textures]
  (Animation. duration
              (u/gdx-array (map :object textures))
              (play-mode :normal)))

(defmacro animation
  [duration textures & options]
  `(u/calls! ^Animation (animation* ~duration ~textures) ~@options))

(defmacro animation!
  [object k & options]
  `(u/call! ^Animation ~object ~k ~@options))

(defn animation->texture
  ([{:keys [total-time]} ^Animation animation]
    (texture* (.getKeyFrame animation total-time true)))
  ([{:keys [total-time]} ^Animation animation is-looping?]
    (texture* (.getKeyFrame animation total-time is-looping?))))
