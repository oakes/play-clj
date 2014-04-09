(ns play-clj.entities
  (:import [com.badlogic.gdx Gdx Graphics]
           [com.badlogic.gdx.graphics Camera]
           [com.badlogic.gdx.graphics.g2d NinePatch ParticleEffect SpriteBatch
            TextureRegion]
           [com.badlogic.gdx.graphics.g3d Environment ModelBatch ModelInstance]
           [com.badlogic.gdx.graphics.glutils ShapeRenderer]
           [com.badlogic.gdx.math Matrix4]
           [com.badlogic.gdx.scenes.scene2d Actor]))

(defprotocol Entity
  (draw-entity! [this screen batch] "Draws the entity"))

(extend-protocol Entity
  clojure.lang.PersistentArrayMap
  (draw-entity! [this screen batch])
  clojure.lang.PersistentHashMap
  (draw-entity! [this screen batch]))

(defrecord TextureEntity [object] Entity
  (draw-entity! [{:keys [^TextureRegion object x y width height]} _ batch]
    (let [x (float (or x 0))
          y (float (or y 0))
          width (float (or width (.getRegionWidth object)))
          height (float (or height (.getRegionHeight object)))]
      (.draw ^SpriteBatch batch object x y width height))))

(defrecord NinePatchEntity [object] Entity
  (draw-entity! [{:keys [^NinePatch object x y width height]} _ batch]
    (let [x (float (or x 0))
          y (float (or y 0))
          width (float (or width (.getTotalWidth object)))
          height (float (or height (.getTotalHeight object)))]
      (.draw object ^SpriteBatch batch x y width height))))

(defrecord ParticleEffectEntity [object] Entity
  (draw-entity! [{:keys [^ParticleEffect object x y delta-time]} _ batch]
    (let [x (float (or x 0))
          y (float (or y 0))
          ^Graphics g (Gdx/graphics)
          delta-time (float (or delta-time (.getDeltaTime g)))]
      (.setPosition object x y)
      (.draw object ^SpriteBatch batch delta-time))))

(defrecord ActorEntity [object] Entity
  (draw-entity! [{:keys [^Actor object] :as entity} _ batch]
    (doseq [[k v] entity]
      (case k
        :x (.setX object v)
        :y (.setY object v)
        :width (.setWidth object v)
        :height (.setHeight object v)
        nil))
    (.draw object ^SpriteBatch batch 1)))

(defrecord ModelEntity [object] Entity
  (draw-entity! [{:keys [^ModelInstance object]}
                 {:keys [^ModelBatch renderer ^Environment attributes]}
                 _]
    (.render renderer object attributes)))

(defrecord ShapeEntity [object] Entity
  (draw-entity! [{:keys [^ShapeRenderer object type draw! x y z]}
                 {:keys [^Camera camera]}
                 _]
    (let [^Matrix4 m (.getTransformMatrix object)
          x (float (or x 0))
          y (float (or y 0))
          z (float (or z 0))]
      (.setTranslation m x y z))
    (when camera
      (.setProjectionMatrix object (. camera combined)))
    (.begin object type)
    (draw!)
    (.end object)))

(defrecord BundleEntity [entities] Entity
  (draw-entity! [{:keys [entities] :as entity} screen batch]
    (doseq [e entities]
      (draw-entity! (merge e entity) screen batch))))
