(ns play-clj.entities
  (:import [com.badlogic.gdx Gdx Graphics]
           [com.badlogic.gdx.graphics Camera Color GL20]
           [com.badlogic.gdx.graphics.g2d Batch NinePatch ParticleEffect Sprite
            TextureRegion]
           [com.badlogic.gdx.graphics.g3d Environment ModelBatch ModelInstance]
           [com.badlogic.gdx.graphics.glutils ShapeRenderer]
           [com.badlogic.gdx.math Matrix4]
           [com.badlogic.gdx.scenes.scene2d Actor]))

(defprotocol Entity
  (draw! [this screen batch] "Draws the entity"))

(extend-protocol Entity
  clojure.lang.PersistentArrayMap
  (draw! [this screen batch])
  clojure.lang.PersistentHashMap
  (draw! [this screen batch])
  nil
  (draw! [this screen batch]))

(defrecord TextureEntity [object] Entity
  (draw! [{:keys [^TextureRegion object x y width height
                  scale-x scale-y angle color]}
          _
          batch]
    (let [x (float (or x 0))
          y (float (or y 0))
          width (float (or width (.getRegionWidth object)))
          height (float (or height (.getRegionHeight object)))]
      (when-let [[r g b a] color]
        (.setColor ^Batch batch r g b a))
      (if (or scale-x scale-y angle)
        (let [scale-x (float (or scale-x 1))
              scale-y (float (or scale-y 1))
              angle (float (or angle 0))]
          (.draw ^Batch batch object x y 0 0 width height
            scale-x scale-y angle))
        (.draw ^Batch batch object x y width height))
      (when color
        (.setColor ^Batch batch Color/WHITE)))))

(defrecord SpriteEntity [object] Entity
  (draw! [{:keys [^Sprite object
                          alpha
                          x y width height scale-x scale-y origin-x origin-y
                          alpha angle color]
           :or {x (.getX object)
                y (.getY object)
                width (.getWidth object)
                height (.getHeight object)
                scale-x (.getScaleX object)
                scale-y (.getScaleY object)
                origin-x (.getOriginX object)
                origin-y (.getOriginY object)
                angle (.getRotation object)
                color (.getColor object)}}
          _
          batch]
    (.setBounds object
                (float x)
                (float y)
                (float width)
                (float height))
    (.setOrigin object
                (float origin-x)
                (float origin-y))
    (.setScale object
               (float scale-x)
               (float scale-y))
    (.setRotation object angle)
    (if (instance? Color color)
      (.setColor object color)
      (let [[r g b a] color]
        (.setColor object r g b a)))
    (if alpha
      (.draw object ^Batch batch alpha)
      (.draw object ^Batch batch))))

(defrecord NinePatchEntity [object] Entity
  (draw! [{:keys [^NinePatch object x y width height]} _ batch]
    (let [x (float (or x 0))
          y (float (or y 0))
          width (float (or width (.getTotalWidth object)))
          height (float (or height (.getTotalHeight object)))]
      (.draw object ^Batch batch x y width height))))

(defrecord ParticleEffectEntity [object] Entity
  (draw! [{:keys [^ParticleEffect object x y delta-time]} _ batch]
    (let [x (float (or x 0))
          y (float (or y 0))
          ^Graphics g (Gdx/graphics)
          delta-time (float (or delta-time (.getDeltaTime g)))]
      (.setPosition object x y)
      (.draw object ^Batch batch delta-time))))

(defrecord ActorEntity [object] Entity
  (draw! [{:keys [^Actor object x y width height
                  scale-x scale-y angle origin-x origin-y]} _ batch]
    (when (.getStage object)
      (some->> x (.setX object))
      (some->> y (.setY object))
      (some->> width (.setWidth object))
      (some->> height (.setHeight object))
      (when (or scale-x scale-y angle)
        (let [scale-x (float (or scale-x 1))
              scale-y (float (or scale-y 1))
              origin-x (float (or origin-x (/ (.getWidth object) 2)))
              origin-y (float (or origin-y (/ (.getHeight object) 2)))
              angle (float (or angle 0))]
          (.setScaleX object scale-x)
          (.setScaleY object scale-y)
          (.setOriginX object origin-x)
          (.setOriginY object origin-y)
          (.setRotation object angle)))
      (.draw object ^Batch batch 1))))

(defrecord ModelEntity [object] Entity
  (draw! [{:keys [^ModelInstance object x y z]}
          {:keys [^ModelBatch renderer ^Environment attributes]}
          _]
    (when (or x y z)
      (let [^Matrix4 m (. object transform)
            x (float (or x 0))
            y (float (or y 0))
            z (float (or z 0))]
        (.setTranslation m x y z)))
    (.render renderer object attributes)))

(defrecord ShapeEntity [object] Entity
  (draw! [{:keys [^ShapeRenderer object type draw!
                  x y scale-x scale-y angle]}
          {:keys [^Camera camera]}
          batch]
    (when batch
      (.end ^Batch batch))
    (when camera
      (.setProjectionMatrix object (. camera combined)))
    (.glEnable Gdx/gl GL20/GL_BLEND)
    (.glBlendFunc Gdx/gl GL20/GL_SRC_ALPHA GL20/GL_ONE_MINUS_SRC_ALPHA)
    (.begin object type)
    (when (or x y scale-x scale-y angle)
      (let [x (float (or x 0))
            y (float (or y 0))
            scale-x (float (or scale-x 1))
            scale-y (float (or scale-y 1))
            angle (float (or angle 0))]
        (.identity object)
        (.translate object x y 0)
        (.scale object scale-x scale-y 1)
        (.rotate object 0 0 1 angle)))
    (draw!)
    (.end object)
    (.glDisable Gdx/gl GL20/GL_BLEND)
    (when batch
      (.begin ^Batch batch))))

(defrecord BundleEntity [entities] Entity
  (draw! [{:keys [entities] :as entity} screen batch]
    (doseq [e entities]
      (draw! (merge e (apply dissoc entity (keys e))) screen batch))))
