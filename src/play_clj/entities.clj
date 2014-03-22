(ns play-clj.entities
  (:import [com.badlogic.gdx Gdx Graphics]
           [com.badlogic.gdx.graphics.g2d NinePatch ParticleEffect SpriteBatch
            TextureRegion]
           [com.badlogic.gdx.graphics.g3d Environment ModelBatch ModelInstance]
           [com.badlogic.gdx.scenes.scene2d Actor]))

(defprotocol Entity
  "Internal use only"
  (draw-entity! [this batch] "Draws the entity"))

(extend-protocol Entity
  clojure.lang.PersistentArrayMap
  (draw-entity! [this batch])
  clojure.lang.PersistentHashMap
  (draw-entity! [this batch]))

(defrecord TextureEntity [object] Entity
  (draw-entity! [{:keys [^TextureRegion object x y width height]} batch]
    (let [x (float (or x 0))
          y (float (or y 0))
          width (float (or width (.getRegionWidth object)))
          height (float (or height (.getRegionHeight object)))]
      (.draw ^SpriteBatch batch object x y width height))))

(defrecord NinePatchEntity [object] Entity
  (draw-entity! [{:keys [^NinePatch object x y width height]} batch]
    (let [x (float (or x 0))
          y (float (or y 0))
          width (float (or width (.getTotalWidth object)))
          height (float (or height (.getTotalHeight object)))]
      (.draw object ^SpriteBatch batch x y width height))))

(defrecord ParticleEffectEntity [object] Entity
  (draw-entity! [{:keys [^ParticleEffect object x y delta-time]} batch]
    (let [x (float (or x 0))
          y (float (or y 0))
          ^Graphics g (Gdx/graphics)
          delta-time (float (or delta-time (.getDeltaTime g)))]
      (.setPosition object x y)
      (.draw object ^SpriteBatch batch delta-time))))

(defrecord ActorEntity [object] Entity
  (draw-entity! [{:keys [^Actor object] :as entity} batch]
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
                 {:keys [^ModelBatch renderer ^Environment attributes]}]
    (.render renderer object attributes)))
