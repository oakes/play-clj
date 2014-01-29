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
  "Calls a single method on a `model-batch`"
  [screen k & options]
  `(let [^ModelBatch object# (u/get-obj ~screen :renderer)]
     (u/call! object# ~k ~@options)))

; model

(defmacro model
  "Returns an entity based on [ModelInstance](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g3d/ModelInstance.html)"
  [& args]
  `(u/create-entity (ModelInstance. ~@args)))

(defmacro model!
  "Calls a single method on a `model`"
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
  "Calls a single method on a `model-builder`"
  [object k & options]
  `(u/call! ^ModelBuilder ~object ~k ~@options))

; material

(defn material*
  "The function version of `material`"
  []
  (Material.))

(defmacro material
  "Returns a [Material](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g3d/Material.html)

    (material)"
  [& options]
  `(let [^Material object# (material*)]
     (u/calls! object# ~@options)))

(defmacro material!
  "Calls a single method on a `material`"
  [object k & options]
  `(u/call! ^Material ~object ~k ~@options))

; attribute

(defn ^:private attribute-init
  "Internal use only"
  [k]
  (u/gdx :graphics :g3d :attributes (str (u/key->pascal k) "Attribute.")))

(defmacro attribute
  "Returns a subclass of [Attribute](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g3d/Attribute.html)

    (attribute :color (attribute-type :color :diffuse) (color :blue))"
  [type & args]
  `(~(attribute-init type) ~@args))

(defmacro attribute-type
  "Returns a static field in a subclass of [Attribute](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g3d/Attribute.html)

    (attribute-type :color :diffuse)"
  [type k]
  `~(u/gdx-field :graphics :g3d :attributes
                 (str (u/key->pascal type) "Attribute")
                 (u/key->pascal k)))

(defmacro attribute!
  "Calls a static method in a subclass of [Attribute](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g3d/Attribute.html)

    (attribute! :color :create-diffuse (color :blue))"
  [type k & options]
  `(~(u/gdx-field :graphics :g3d :attributes
                  (str (u/key->pascal type) "Attribute")
                  (u/key->camel k))
     ~@options))
