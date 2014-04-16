(ns play-clj.g3d-physics
  (:require [play-clj.core :as c]
            [play-clj.math :as m]
            [play-clj.utils :as u])
  (:import [com.badlogic.gdx.math Matrix4]
           [com.badlogic.gdx.physics.bullet Bullet]
           [com.badlogic.gdx.physics.bullet.collision btBoxShape
            btCollisionDispatcher btCylinderShape btCollisionObject
            btCollisionWorld btDefaultCollisionConfiguration btDbvtBroadphase
            btSphereShape]
           [com.badlogic.gdx.physics.bullet.dynamics btDiscreteDynamicsWorld
            btDynamicsWorld btRigidBody btRigidBody$btRigidBodyConstructionInfo
            btSequentialImpulseConstraintSolver]
           [com.badlogic.gdx.physics.bullet.linearmath btMotionState]))

(def ^:private init (delay (Bullet/init)))

(defrecord World3D [object])
(defrecord Body3D [object])

; world

(defn ^:private discrete-dynamics
  []
  (let [config (btDefaultCollisionConfiguration.)
        dispatcher (btCollisionDispatcher. config)
        broad (btDbvtBroadphase.)
        solver (btSequentialImpulseConstraintSolver.)]
    (assoc (World3D. (btDiscreteDynamicsWorld. dispatcher broad solver config))
           :config config
           :dispatcher dispatcher
           :broadphase broad
           :solver solver)))

(defn bullet-3d*
  [type]
  @init
  (case type
    :discrete-dynamics (discrete-dynamics)
    (u/throw-key-not-found type)))

(defmacro bullet-3d
  "Returns a world based on btCollisionWorld.

    (bullet-3d :discrete-dynamics)"
  [type & options]
  `(let [world# (bullet-3d* ~type)
         ^btCollisionWorld object# (:object world#)]
     (u/calls! object# ~@options)
     world#))

(defmacro bullet-3d!
  "Calls a single method on a `bullet-3d`."
  [screen k & options]
  `(let [^btCollisionWorld object# (:object (u/get-obj ~screen :world))]
     (u/call! object# ~k ~@options)))

; bodies

(defn basic-body*
  []
  (Body3D. (btCollisionObject.)))

(defmacro basic-body
  "Returns a body based on btCollisionObject."
  [& options]
  `(let [body# (basic-body*)
         ^btCollisionObject object# (:object body#)]
     (u/calls! object# ~@options)
     body#))

(defmacro basic-body!
  "Calls a single method on a `basic-body`."
  [object k & options]
  `(let [^btCollisionObject object# (:object (u/get-obj ~object :body))]
     (u/call! object# ~k ~@options)))

(defn rigid-body*
  [info]
  (assoc (Body3D. (btRigidBody. info))
         :info info))

(defmacro rigid-body
  "Returns a body based on btRigidBody."
  [info & options]
  `(let [body# (rigid-body* ~info)
         ^btRigidBody object# (:object body#)]
     (u/calls! object# ~@options)
     body#))

(defmacro rigid-body!
  "Calls a single method on a `rigid-body`."
  [object k & options]
  `(let [^btRigidBody object# (:object (u/get-obj ~object :body))]
     (u/call! object# ~k ~@options)))

(defn rigid-body-info
  "Returns a btRigidBodyConstructionInfo."
  [mass motion-state collision-shape local-inertia]
  (btRigidBody$btRigidBodyConstructionInfo.
    mass motion-state collision-shape local-inertia))

(defmacro rigid-body-info!
  "Calls a single method on a `rigid-body-info`."
  [object k & options]
  `(u/call! ^btRigidBody$btRigidBodyConstructionInfo ~object ~k ~@options))

(defn ^:private body-x
  [entity]
  (let [^btCollisionObject object (:object (u/get-obj entity :body))]
    (-> object .getWorldTransform (. val) (aget Matrix4/M03))))

(defn ^:private body-y
  [entity]
  (let [^btCollisionObject object (:object (u/get-obj entity :body))]
    (-> object .getWorldTransform (. val) (aget Matrix4/M13))))

(defn ^:private body-z
  [entity]
  (let [^btCollisionObject object (:object (u/get-obj entity :body))]
    (-> object .getWorldTransform (. val) (aget Matrix4/M23))))

(defmethod c/body-position!
  Body3D
  [entity x y z]
  (let [^btCollisionObject object (:object (u/get-obj entity :body))]
    (.setWorldTransform object
      (doto (m/matrix-4*)
        (m/matrix-4! :set-translation x y z)))))

(defmethod c/body-x!
  Body3D
  [entity x]
  (c/body-position! entity x (body-y entity) (body-z entity)))

(defmethod c/body-y!
  Body3D
  [entity y]
  (c/body-position! entity (body-x entity) y (body-z entity)))

(defmethod c/body-z!
  Body3D
  [entity z]
  (c/body-position! entity (body-x entity) (body-y entity) z))

; shapes

(defn box-shape*
  [box-half-extents]
  (btBoxShape. box-half-extents))

(defmacro box-shape
  "Returns a btSphereShape."
  [box-half-extents & options]
  `(u/calls! ^btBoxShape (box-shape* ~box-half-extents) ~@options))

(defmacro box-shape!
  "Calls a single method on a `box-shape`."
  [object k & options]
  `(u/call! ^btBoxShape ~object ~k ~@options))

(defn cylinder-shape*
  [half-extents]
  (btCylinderShape. half-extents))

(defmacro cylinder-shape
  "Returns a btCylinderShape."
  [half-extents & options]
  `(u/calls! ^btCylinderShape (cylinder-shape* ~half-extents) ~@options))

(defmacro cylinder-shape!
  "Calls a single method on a `cylinder-shape`."
  [object k & options]
  `(u/call! ^btCylinderShape ~object ~k ~@options))

(defn sphere-shape*
  [radius]
  (btSphereShape. radius))

(defmacro sphere-shape
  "Returns a btSphereShape."
  [radius & options]
  `(u/calls! ^btSphereShape (sphere-shape* ~radius) ~@options))

(defmacro sphere-shape!
  "Calls a single method on a `sphere-shape`."
  [object k & options]
  `(u/call! ^btSphereShape ~object ~k ~@options))

; misc

(defmethod c/add-body!
  World3D
  [screen body]
  (cond
    (isa? (type (:object body)) btRigidBody)
    (bullet-3d! screen :add-rigid-body (:object body))
    :else
    (bullet-3d! screen :add-collision-object (:object body)))
  body)

(defn ^:private get-bodies
  [screen]
  (let [arr (bullet-3d! screen :get-collision-object-array)]
    (for [i (range (.size arr))]
      (.at arr i))))

(defmethod c/update-physics!
  World3D
  [screen & [entities]]
  ; initialize bodies
  (doseq [e entities]
    (let [object (u/get-obj e :object)
          body (u/get-obj e :body)]
      (when (and object body)
        (cond
          (isa? (type body) btRigidBody)
          (when-not (rigid-body! body :get-motion-state)
            (rigid-body! body
                         :set-motion-state
                         (proxy [btMotionState] []
                           (getWorldTransform [world-t])
                           (setWorldTransform [world-t]
                             (m/matrix-4! (. object transform) :set world-t)))))
          :else
          (basic-body! e :set-world-transform (. object transform))))))
  ; remove bodies that no longer exist
  (when entities
    (doseq [body (get-bodies screen)]
      (when-not (some #(= body (-> % :body :object)) entities)
        (cond
          (isa? (type body) btRigidBody)
          (bullet-3d! screen :remove-rigid-body body)
          :else
          (bullet-3d! screen :remove-collision-object body))))))

(defmethod c/step!
  World3D
  [{:keys [delta-time max-sub-steps time-step]
     :or {max-sub-steps 5 time-step (/ 1 60)}
     :as screen}
   & [entities]]
  (when (isa? (type (:object (u/get-obj screen :world))) btDynamicsWorld)
    (bullet-3d! screen :step-simulation delta-time max-sub-steps time-step)
    (when entities
      (map (fn [e]
             (if (u/get-obj e :body)
               (assoc e
                      :x (body-x e)
                      :y (body-y e)
                      :z (body-z e))
               e))
           entities))))
