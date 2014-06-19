(ns play-clj.g3d
  (:require [play-clj.entities]
            [play-clj.utils :as u])
  (:import [com.badlogic.gdx.graphics.g3d Environment Material Model ModelBatch
            ModelInstance]
           [com.badlogic.gdx.graphics.g3d.attributes BlendingAttribute
            ColorAttribute CubemapAttribute DepthTestAttribute FloatAttribute
            IntAttribute TextureAttribute]
           [com.badlogic.gdx.graphics.g3d.model.data ModelData]
           [com.badlogic.gdx.graphics.g3d.utils AnimationController
            ModelBuilder]
           [play_clj.entities ModelEntity]))

; animation-controller

(defn animation-controller*
  [entity]
  (AnimationController. (u/get-obj entity :object)))

(defmacro animation-controller
  "Returns an [AnimationController](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g3d/utils/AnimationController.html).

    (animation-controller model-entity)"
  [entity & options]
  `(let [^AnimationController object# (animation-controller* ~entity)]
     (u/calls! object# ~@options)))

(defmacro animation-controller!
  "Calls a single method on an `animation-controller`."
  [object k & options]
  `(let [^AnimationController object# ~object]
     (u/call! object# ~k ~@options)))

; environment

(defn environment*
  []
  (Environment.))

(defmacro environment
  "Returns an [Environment](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g3d/Environment.html).

    (environment)"
  [& options]
  `(let [^Environment object# (environment*)]
     (u/calls! object# ~@options)))

(defmacro environment!
  "Calls a single method on an `environment`."
  [screen k & options]
  `(let [^Environment object# (u/get-obj ~screen :attributes)]
     (u/call! object# ~k ~@options)))

; model-batch

(defn model-batch*
  []
  (ModelBatch.))

(defmacro model-batch
  "Returns a [ModelBatch](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g3d/ModelBatch.html).

    (model-batch)"
  [& options]
  `(let [^ModelBatch object# (model-batch*)]
     (u/calls! object# ~@options)))

(defmacro model-batch!
  "Calls a single method on a `model-batch`."
  [screen k & options]
  `(let [^ModelBatch object# (u/get-obj ~screen :renderer)]
     (u/call! object# ~k ~@options)))

; model

(defn model*
  [^String path]
  (or (u/load-asset path Model)
      (throw (Exception. "Asset manager not found. See set-asset-manager!"))))

(defmacro model
  "Returns an entity based on [ModelInstance](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g3d/ModelInstance.html).
In addition to the listed options, you may also pass an internal path to a model
object created by an external application.

    ; load a model from a file
    (model \"knight.g3dj\")
    ; create a model from an existing model
    (model (model \"knight.g3dj\"))
    ; set the position of a model
    (assoc (model \"knight.g3dj\")
           :x 0 :y 0 :z 0)"
  [& args]
  `(ModelEntity.
     (let [arg1# ~(first args)]
       (cond
         (string? arg1#)
         (ModelInstance. (model* arg1#))
         (isa? (type arg1#) ModelEntity)
         (ModelInstance. (. ^ModelInstance (:object arg1#) model) ~@(rest args))
         (isa? (type arg1#) ModelData)
         (ModelInstance. ^Model (Model. arg1# ~@(rest args)))
         :else
         (ModelInstance. arg1# ~@(rest args))))))

(defmacro model!
  "Calls a single method on a `model`."
  [entity k & options]
  `(let [^ModelInstance object# (u/get-obj ~entity :object)]
     (u/call! object# ~k ~@options)))

(defn model?
  "Returns true if `entity` is a `model`."
  [entity]
  (isa? (type (u/get-obj entity :object)) ModelInstance))

; model-builder

(defn model-builder*
  []
  (ModelBuilder.))

(defmacro model-builder
  "Returns a [ModelBuilder](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g3d/utils/ModelBuilder.html).

    (model-builder)"
  [& options]
  `(let [^ModelBuilder object# (model-builder*)]
     (u/calls! object# ~@options)))

(defmacro model-builder!
  "Calls a single method on a `model-builder`."
  [object k & options]
  `(u/call! ^ModelBuilder ~object ~k ~@options))

; material

(defn material*
  []
  (Material.))

(defmacro material
  "Returns a [Material](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g3d/Material.html).

    (material)"
  [& options]
  `(let [^Material object# (material*)]
     (u/calls! object# ~@options)))

(defmacro material!
  "Calls a single method on a `material`."
  [object k & options]
  `(u/call! ^Material ~object ~k ~@options))

; attribute

(defn ^:private attribute-init
  [k]
  (u/gdx :graphics :g3d :attributes (str (u/key->pascal k) "Attribute.")))

(defmacro attribute
  "Returns a subclass of [Attribute](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g3d/Attribute.html).

    (attribute :color (attribute-type :color :diffuse) (color :blue))"
  [type & args]
  `(~(attribute-init type) ~@args))

(defmacro attribute-type
  "Returns a static field in a subclass of [Attribute](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g3d/Attribute.html).

    (attribute-type :color :diffuse)"
  [type k]
  (u/gdx-field :graphics :g3d :attributes
               (str (u/key->pascal type) "Attribute")
               (u/key->pascal k)))

(defmacro attribute!
  "Calls a static method in a subclass of [Attribute](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g3d/Attribute.html).

    (attribute! :color :create-diffuse (color :blue))"
  [type k & options]
  `(~(u/gdx-field :graphics :g3d :attributes
                  (str (u/key->pascal type) "Attribute")
                  (u/key->camel k))
     ~@options))
