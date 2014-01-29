(ns play-clj.g3d
  (:require [play-clj.utils :as u])
  (:import [com.badlogic.gdx.graphics.g3d Environment Material Model ModelBatch
            ModelInstance]
           [com.badlogic.gdx.graphics.g3d.attributes BlendingAttribute
            ColorAttribute CubemapAttribute DepthTestAttribute FloatAttribute
            IntAttribute TextureAttribute]
           [com.badlogic.gdx.graphics.g3d.utils ModelBuilder]))

; environment

(defn environment*
  "The function version of `environment`"
  []
  (Environment.))

(defmacro environment
  "Returns an [Environment](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g3d/Environment.html)

    (environment)"
  [& options]
  `(let [^Environment object# (environment*)]
     (u/calls! object# ~@options)))

(defmacro environment!
  "Calls a single method on an `environment`"
  [screen k & options]
  `(let [^Environment object# (u/get-obj ~screen :attributes)]
     (u/call! object# ~k ~@options)))

; model-batch

(defn model-batch*
  "The function version of `model-batch`"
  []
  (ModelBatch.))

(defmacro model-batch
  "Returns a [ModelBatch](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g3d/ModelBatch.html)

    (model-batch)"
  [& options]
  `(let [^ModelBatch object# (model-batch*)]
     (u/calls! object# ~@options)))

(defmacro model-batch!
  "Calls a single method on an `model-batch`"
  [screen k & options]
  `(let [^ModelBatch object# (u/get-obj ~screen :renderer)]
     (u/call! object# ~k ~@options)))

; model

(defn model*
  "The function version of `model`"
  ([a1]
    (u/create-entity (ModelInstance. (if (map? a1) (:object a1) a1))))
  ([a1 a2]
    (u/create-entity (ModelInstance. a1 a2)))
  ([a1 a2 a3]
    (u/create-entity (ModelInstance. a1 a2 a3)))
  ([a1 a2 a3 a4]
    (u/create-entity (ModelInstance. a1 a2 a3 a4)))
  ([a1 a2 a3 a4 a5]
    (u/create-entity (ModelInstance. a1 a2 a3 a4 a5)))
  ([a1 a2 a3 a4 a5 a6]
    (u/create-entity (ModelInstance. a1 a2 a3 a4 a5 a6))))

(defmacro model
  "Returns an entity based on [ModelInstance](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g3d/ModelInstance.html)"
  [& options]
  `(let [^ModelInstance object# (model*)]
     (u/calls! object# ~@options)))

(defmacro model!
  "Calls a single method on an `model`"
  [entity k & options]
  `(let [^ModelInstance object# (u/get-obj ~entity :object)]
     (u/call! object# ~k ~@options)))

; model-builder

(defn model-builder*
  "The function version of `model-builder`"
  []
  (ModelBuilder.))

(defmacro model-builder
  "Returns a [ModelBuilder](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g3d/utils/ModelBuilder.html)

    (model-builder)"
  [& options]
  `(let [^ModelBuilder object# (model-builder*)]
     (u/calls! object# ~@options)))

(defmacro model-builder!
  "Calls a single method on an `model-builder`"
  [object k & options]
  `(let [^ModelBuilder object# object]
     (u/call! object# ~k ~@options)))

; material

(defmacro material
  "Returns a [Material](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g3d/Material.html)

    (material)"
  [& args]
  `(Material. ~@args))

(defmacro material!
  "Calls a single method on an `material`"
  [object k & options]
  `(let [^Material object# object]
     (u/call! object# ~k ~@options)))

; attribute

(defmacro ^:private attribute-type
  "Internal use only"
  [k]
  `(symbol (str u/main-package ".graphics.g3d."
                (u/key->pascal ~k) "Attribute")))

(defmacro attribute
  "Returns a subclass of [Attribute](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g3d/Attribute.html)

    (attribute :color)"
  [type & args]
  `(~(attribute-type type) ~@args))

(defmacro attribute!
  "Calls a single method on [Attribute](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g3d/Attribute.html)

    (attribute! :color)"
  [type k & options]
  `((u/static-camel :graphics
                    :g2d
                    (str (u/key->pascal type) "Attribute")
                    k)
     ~@options))
