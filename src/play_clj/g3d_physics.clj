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
            btRigidBody btRigidBody$btRigidBodyConstructionInfo
            btSequentialImpulseConstraintSolver]
           [com.badlogic.gdx.physics.bullet.linearmath btMotionState]))

(def ^:private init (delay (Bullet/init)))

; world

(defn ^:private discrete-dynamics
  []
  (let [config (btDefaultCollisionConfiguration.)
        dispatcher (btCollisionDispatcher. config)
        broad (btDbvtBroadphase.)
        solver (btSequentialImpulseConstraintSolver.)]
    (btDiscreteDynamicsWorld. dispatcher broad solver config)))

(defn bullet-3d*
  [type]
  @init
  (case type
    :discrete-dynamics (discrete-dynamics)
    (u/throw-key-not-found type)))

(defmacro bullet-3d
  "Returns a subclass of btCollisionWorld.

    (bullet-3d :discrete-dynamics)"
  [type & options]
  `(let [^btCollisionWorld object# (bullet-3d* ~type)]
     (u/calls! object# ~@options)))

(defmacro bullet-3d!
  "Calls a single method on a `bullet-3d`."
  [screen k & options]
  `(let [^btCollisionWorld object# (u/get-obj ~screen :world)]
     (u/call! object# ~k ~@options)))

; bodies

(defn basic-body*
  []
  (btCollisionObject.))

(defmacro basic-body
  "Returns a btCollisionObject."
  [& options]
  `(u/calls! ^btCollisionObject (basic-body*) ~@options))

(defmacro basic-body!
  "Calls a single method on a `basic-body`."
  [object k & options]
  `(u/call! ^btCollisionObject (u/get-obj ~object :body) ~k ~@options))

(defn rigid-body*
  [info]
  (btRigidBody. info))

(defmacro rigid-body
  "Returns a btRigidBody."
  [info & options]
  `(u/calls! ^btRigidBody (rigid-body* ~info) ~@options))

(defmacro rigid-body!
  "Calls a single method on a `rigid-body`."
  [object k & options]
  `(u/call! ^btRigidBody (u/get-obj ~object :body) ~k ~@options))

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
  (let [^btCollisionObject object (u/get-obj entity :body)]
    (-> object .getWorldTransform (. val) (aget Matrix4/M03))))

(defn ^:private body-y
  [entity]
  (let [^btCollisionObject object (u/get-obj entity :body)]
    (-> object .getWorldTransform (. val) (aget Matrix4/M13))))

(defn ^:private body-z
  [entity]
  (let [^btCollisionObject object (u/get-obj entity :body)]
    (-> object .getWorldTransform (. val) (aget Matrix4/M23))))

(defmethod c/body-position!
  btCollisionObject
  [entity x y z]
  (let [^btCollisionObject object (u/get-obj entity :body)]
    (.setWorldTransform object
      (doto (m/matrix-4*)
        (m/matrix-4! :set-translation x y z)))))

(defmethod c/body-x!
  btCollisionObject
  [entity x]
  (c/body-position! entity x (body-y entity) (body-z entity)))

(defmethod c/body-y!
  btCollisionObject
  [entity y]
  (c/body-position! entity (body-x entity) y (body-z entity)))

(defmethod c/body-z!
  btCollisionObject
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
  btCollisionWorld
  [screen body]
  (cond
    (isa? (type body) btRigidBody)
    (bullet-3d! screen :add-rigid-body body)
    :else
    (bullet-3d! screen :add-collision-object body))
  body)

(defn ^:private get-bodies
  [screen]
  (let [arr (bullet-3d! screen :get-collision-object-array)]
    (for [i (range (.size arr))]
      (.at arr i))))

(defmethod c/update-physics!
  btCollisionWorld
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
          (.setWorldTransform body (. object transform))))))
  ; remove bodies that no longer exist
  (when entities
    (doseq [body (get-bodies screen)]
      (when-not (some #(= body (:body %)) entities)
        (cond
          (isa? (type body) btRigidBody)
          (bullet-3d! screen :remove-rigid-body body)
          :else
          (bullet-3d! screen :remove-collision-object body))))))

(defmethod c/step!
  btCollisionWorld
  [{:keys [^btCollisionWorld world delta-time max-sub-steps time-step]
     :or {max-sub-steps 5 time-step (/ 1 60)}
     :as screen}
   & [entities]]
  (.stepSimulation world delta-time max-sub-steps time-step)
  (when entities
    (map (fn [e]
           (if (u/get-obj e :body)
             (assoc e
                    :x (body-x e)
                    :y (body-y e)
                    :z (body-z e))
             e))
         entities)))
