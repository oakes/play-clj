(ns play-clj.g3d-physics
  (:require [play-clj.core :as c]
            [play-clj.math :as m]
            [play-clj.utils :as u])
  (:import [com.badlogic.gdx.math Matrix4]
           [com.badlogic.gdx.physics.bullet Bullet]
           [com.badlogic.gdx.physics.bullet.collision btBoxShape
            btCollisionDispatcher btCylinderShape btCapsuleShape btConeShape
            btCollisionObject btDefaultCollisionConfiguration btDbvtBroadphase
            btSphereShape]
           [com.badlogic.gdx.physics.bullet.dynamics btDiscreteDynamicsWorld
            btDynamicsWorld btRigidBody btRigidBody$btRigidBodyConstructionInfo
            btSequentialImpulseConstraintSolver]
           [com.badlogic.gdx.physics.bullet.linearmath btMotionState]
           [com.badlogic.gdx.physics.bullet.softbody btSoftBody
            btSoftBodyRigidBodyCollisionConfiguration btSoftBodyWorldInfo
            btSoftRigidDynamicsWorld]
           [play_clj.g3d_physics ContactListener3D]))

(def init-bullet (delay (Bullet/init)))

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
           :constraint-solver solver)))

(defn ^:private soft-rigid-dynamics
  []
  (let [config (btSoftBodyRigidBodyCollisionConfiguration.)
        dispatcher (btCollisionDispatcher. config)
        broad (btDbvtBroadphase.)
        solver (btSequentialImpulseConstraintSolver.)]
    (assoc (World3D. (btSoftRigidDynamicsWorld. dispatcher broad solver config))
           :config config
           :dispatcher dispatcher
           :broadphase broad
           :constraint-solver solver)))

(defn bullet-3d*
  [type]
  @init-bullet
  (case type
    :rigid (discrete-dynamics)
    :soft-rigid (soft-rigid-dynamics)
    (u/throw-key-not-found type)))

(defmacro bullet-3d
  "Returns a world based on btDynamicsWorld.

    (bullet-3d :rigid) ; can only handle rigid bodies
    (bullet-3d :soft-rigid) ; can handle soft and rigid bodies"
  [type & options]
  `(let [world# (bullet-3d* ~type)
         ^btDynamicsWorld object# (:object world#)]
     (u/calls! object# ~@options)
     world#))

(defmacro bullet-3d!
  "Calls a single method on a `bullet-3d`."
  [screen k & options]
  `(let [^btDynamicsWorld object# (:object (u/get-obj ~screen :world))]
     (u/call! object# ~k ~@options)))

; bodies

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

(defn soft-body*
  [info]
  (assoc (Body3D. (btSoftBody. info))
         :info info))

(defmacro soft-body
  "Returns a body based on btSoftBody."
  [info & options]
  `(let [body# (soft-body* ~info)
         ^btSoftBody object# (:object body#)]
     (u/calls! object# ~@options)
     body#))

(defmacro soft-body!
  "Calls a single method on a `soft-body`."
  [object k & options]
  `(let [^btSoftBody object# (:object (u/get-obj ~object :body))]
     (u/call! object# ~k ~@options)))

(defn soft-body-info
  "Returns a btSoftBodyWorldInfo."
  []
  (btSoftBodyWorldInfo.))

(defn add-body!
  "Adds the `body` to the `screen` and returns it.

    (add-body! screen (rigid-body info))"
  [screen body]
  (cond
    (isa? (type (:object body)) btRigidBody)
    (bullet-3d! screen :add-rigid-body (:object body))
    (isa? (type (:object body)) btSoftBody)
    (bullet-3d! screen :add-soft-body (:object body)))
  body)

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

(defn body-position!
  "Changes the position of the body in `entity`."
  [entity x y z]
  (let [^btCollisionObject object (:object (u/get-obj entity :body))]
    (.setWorldTransform object
      (doto (m/matrix-4*)
        (m/matrix-4! :set-translation x y z)))))

(defn body-x!
  "Changes the `x` of the body in `entity`."
  [entity x]
  (body-position! entity x (body-y entity) (body-z entity)))

(defn body-y!
  "Changes the `y` of the body in `entity`."
  [entity y]
  (body-position! entity (body-x entity) y (body-z entity)))

(defn body-z!
  "Changes the `z` of the body in `entity`."
  [entity z]
  (body-position! entity (body-x entity) (body-y entity) z))

(defn ^:private find-body
  [body entities]
  (some #(if (= body (:object (u/get-obj % :body))) %) entities))

(defn first-entity
  "Returns the first entity in a contact. May only be used in contact functions
such as :on-begin-contact."
  [screen entities]
  (-> (u/get-obj screen :first-body)
      (find-body entities)))

(defn second-entity
  "Returns the second entity in a contact. May only be used in contact functions
such as :on-begin-contact."
  [screen entities]
  (-> (u/get-obj screen :second-body)
      (find-body entities)))

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

(defn capsule-shape*
  [radius height]
  (btCapsuleShape. radius height))

(defmacro capsule-shape
  "Returns a btCapsuleShape."
  [radius height & options]
  `(u/calls! ^btCapsuleShape (capsule-shape* ~radius ~height) ~@options))

(defmacro capsule-shape!
  "Calls a single method on a `capsule-shape`."
  [object k & options]
  `(u/call! ^btCapsuleShape ~object ~k ~@options))

(defn cone-shape*
  [radius height]
  (btConeShape. radius height))

(defmacro cone-shape
  "Returns a btConeShape."
  [radius height & options]
  `(u/calls! ^btConeShape (cone-shape* ~radius ~height) ~@options))

(defmacro cone-shape!
  "Calls a single method on a `cone-shape`."
  [object k & options]
  `(u/call! ^btConeShape ~object ~k ~@options))

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

(defmethod c/contact-listener
  "play_clj.g3d_physics.World3D"
  [screen
   {:keys [on-begin-contact on-end-contact]}
   execute-fn!]
  (ContactListener3D.
    (fn [a b]
      (execute-fn! on-begin-contact :first-body a :second-body b))
    (fn [a b]
      (execute-fn! on-end-contact :first-body a :second-body b))))

(defn ^:private get-bodies
  [screen]
  (let [arr (bullet-3d! screen :get-collision-object-array)]
    (for [i (range (.size arr))]
      (.at arr i))))

(defmethod c/update-physics!
  "play_clj.g3d_physics.World3D"
  [screen & [entities]]
  ; initialize bodies if necessary
  (doseq [e entities]
    (let [object (u/get-obj e :object)
          body (u/get-obj e :body)]
      (when (and object (isa? (type (:object body)) btRigidBody))
        (when-not (rigid-body! e :get-motion-state)
          (->> (proxy [btMotionState] []
                 (getWorldTransform [world-t])
                 (setWorldTransform [world-t]
                   (m/matrix-4! (. object transform) :set world-t)))
               (rigid-body! e :set-motion-state))))))
  ; remove bodies that no longer exist
  (when entities
    (doseq [^btCollisionObject body (get-bodies screen)]
      (when-not (some #(= body (-> % :body :object)) entities)
        (cond
          (isa? (type body) btRigidBody)
          (bullet-3d! screen :remove-rigid-body body)
          (isa? (type body) btSoftBody)
          (bullet-3d! screen :remove-soft-body body))
        (.dispose body)))))

(defn step!
  "Runs the physics simulations for a single frame and optionally returns the
`entities` with their positions updated."
  ([{:keys [delta-time max-sub-steps time-step]
     :or {max-sub-steps 5 time-step (/ 1 60)}
     :as screen}]
    (bullet-3d! screen :step-simulation delta-time max-sub-steps time-step))
  ([screen entities]
    (step! screen)
    (map (fn [e]
           (if (:body e)
             (assoc e
                    :x (body-x e)
                    :y (body-y e)
                    :z (body-z e))
             e))
         entities)))
