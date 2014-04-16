(ns play-clj.g2d-physics
  (:require [play-clj.core :as c]
            [play-clj.math :as m]
            [play-clj.utils :as u])
  (:import [com.badlogic.gdx.physics.box2d Body BodyDef ChainShape CircleShape
            Contact ContactListener EdgeShape Fixture FixtureDef Joint JointDef
            PolygonShape Transform World]))

; world

(defn box-2d*
  ([]
    (box-2d* 0 0 true))
  ([gravity-x gravity-y]
    (box-2d* gravity-x gravity-y true))
  ([gravity-x gravity-y sleep?]
    (World. (m/vector-2 gravity-x gravity-y) sleep?)))

(defmacro box-2d
  "Returns a [World](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/World.html).

    (box-2d 0 0)"
  [gravity-x gravity-y & options]
  `(let [^World object# (box-2d* ~gravity-x ~gravity-y)]
     (u/calls! object# ~@options)))

(defmacro box-2d!
  "Calls a single method on a `box-2d`."
  [screen k & options]
  `(let [^World object# (u/get-obj ~screen :world)]
     (u/call! object# ~k ~@options)))

; bodies

(defn ^:private body-type
  [k]
  (u/gdx-class :physics :box2d :BodyDef
               (str "BodyType/" (u/key->pascal k) "Body")))

(defmacro body-def
  "Returns a [BodyDef](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/BodyDef.html).

    (body-def :dynamic)"
  [k & options]
  `(let [^BodyDef object# (BodyDef.)]
     (set! (. object# type) ~(body-type k))
     (u/fields! object# ~@options)))

(defmacro body!
  "Calls a single method on a body."
  [entity k & options]
  `(let [^Body object# (u/get-obj ~entity :body)]
     (u/call! object# ~k ~@options)))

(defn ^:private body-x
  [entity]
  (. (body! entity :get-position) x))

(defn ^:private body-y
  [entity]
  (. (body! entity :get-position) y))

(defn ^:private body-angle
  [entity]
  (.getRotation ^Transform (body! entity :get-transform)))

(defmethod c/body-position!
  Body
  [entity x y angle]
  (body! entity :set-transform x y angle))

(defmethod c/body-x!
  Body
  [entity x]
  (c/body-position! entity x (body-y entity) (body-angle entity)))

(defmethod c/body-y!
  Body
  [entity y]
  (c/body-position! entity (body-x entity) y (body-angle entity)))

(defmethod c/body-angle!
  Body
  [entity angle]
  (c/body-position! entity (body-x entity) (body-y entity) angle))

; joints

(defn ^:private joint-init
  [k]
  (u/gdx :physics :box2d :joints (str (u/key->pascal k) "JointDef.")))

(defmacro joint-def
  "Returns a subclass of [JointDef](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/JointDef.html).

    (joint-def :rope)"
  [k & options]
  `(let [object# (~(joint-init k))]
     (u/fields! object# ~@options)))

(defmacro joint!
  "Calls a single method on a joint."
  [object k & options]
  `(u/call! ^Joint ~object ~k ~@options))

; fixtures

(defmacro fixture-def
  "Returns a [FixtureDef](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/FixtureDef.html)."
  [& options]
  `(let [^FixtureDef object# (FixtureDef.)]
     (u/fields! object# ~@options)
     object#))

(defmacro fixture!
  "Calls a single method on a fixture."
  [object k & options]
  `(u/call! ^Fixture ~object ~k ~@options))

; shapes

(defn chain-shape*
  []
  (ChainShape.))

(defmacro chain-shape
  "Returns a [ChainShape](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/ChainShape.html)."
  [& options]
  `(u/calls! ^ChainShape (chain-shape*) ~@options))

(defmacro chain-shape!
  "Calls a single method on a `chain-shape`."
  [object k & options]
  `(u/call! ^ChainShape ~object ~k ~@options))

(defn circle-shape*
  ([]
    (CircleShape.))
  ([radius]
    (doto ^CircleShape (circle-shape*)
      (.setRadius radius)
      (.setPosition (m/vector-2 radius radius)))))

(defmacro circle-shape
  "Returns a [CircleShape](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/CircleShape.html)."
  [radius & options]
  `(u/calls! ^CircleShape (circle-shape* ~radius) ~@options))

(defmacro circle-shape!
  "Calls a single method on a `circle-shape`."
  [object k & options]
  `(u/call! ^CircleShape ~object ~k ~@options))

(defn edge-shape*
  []
  (EdgeShape.))

(defmacro edge-shape
  "Returns an [EdgeShape](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/EdgeShape.html)."
  [& options]
  `(u/calls! ^EdgeShape (edge-shape*) ~@options))

(defmacro edge-shape!
  "Calls a single method on an `edge-shape`."
  [object k & options]
  `(u/call! ^EdgeShape ~object ~k ~@options))

(defn polygon-shape*
  []
  (PolygonShape.))

(defmacro polygon-shape
  "Returns a [PolygonShape](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/PolygonShape.html)."
  [& options]
  `(u/calls! ^PolygonShape (polygon-shape*) ~@options))

(defmacro polygon-shape!
  "Calls a single method on a `polygon-shape`."
  [object k & options]
  `(u/call! ^PolygonShape ~object ~k ~@options))

; misc

(defmacro contact!
  "Calls a single method on a [Contact](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/Contact.html)."
  [screen k & options]
  `(u/call! ^Contact (u/get-obj ~screen :contact) ~k ~@options))

(defn find-body
  "Returns the first entity in `entities` whose body matches `body`."
  [body entities]
  (some #(if (= body (:body %)) %) entities))

(defn first-body
  "Returns the first body in a contact."
  [screen]
  (let [^Contact contact (u/get-obj screen :contact)]
    (assert contact)
    (-> contact .getFixtureA .getBody)))

(defn second-body
  "Returns the second body in a contact."
  [screen]
  (let [^Contact contact (u/get-obj screen :contact)]
    (assert contact)
    (-> contact .getFixtureB .getBody)))

(defmethod c/add-body!
  World
  [screen b-def]
  (box-2d! screen :create-body b-def))

(defmethod c/add-joint!
  World
  [screen j-def]
  (box-2d! screen :create-joint j-def))

(defmethod c/physics-listeners
  World
  [screen
   {:keys [on-begin-contact on-end-contact on-post-solve on-pre-solve]}
   execute-fn!]
  {:contact (reify ContactListener
              (beginContact [this c]
                (execute-fn! on-begin-contact :contact c))
              (endContact [this c]
                (execute-fn! on-end-contact :contact c))
              (postSolve [this c i]
                (execute-fn! on-post-solve :contact c :impulse i))
              (preSolve [this c m]
                (execute-fn! on-pre-solve :contact c :old-manifold m)))})

(defmethod c/update-physics!
  World
  [{:keys [^World world physics-listeners]} & [entities]]
  (.setContactListener world (:contact physics-listeners))
  (when (and entities (not (.isLocked world)))
    (let [arr (u/gdx-array [])]
      ; remove bodies that no longer exist
      (.getBodies world arr)
      (doseq [body arr]
        (when-not (some #(= body (:body %)) entities)
          (.destroyBody world body)))
      ; remove joints whose bodies no longer exist
      (.getJoints world arr)
      (doseq [^Joint joint arr]
        (when (and (not (some #(= (.getBodyA joint) (:body %)) entities))
                   (not (some #(= (.getBodyB joint) (:body %)) entities)))
          (.destroyJoint world joint))))))

(defmethod c/step!
  World
  [{:keys [^World world time-step velocity-iterations position-iterations]
     :or {time-step (/ 1 60) velocity-iterations 10 position-iterations 10}
     :as screen}
   & [entities]]
  (.step world time-step velocity-iterations position-iterations)
  (when entities
    (map (fn [e]
           (if (u/get-obj e :body)
             (assoc e
                    :x (body-x e)
                    :y (body-y e))
             e))
         entities)))
