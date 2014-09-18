(ns play-clj.g2d-physics
  (:require [play-clj.core :as c]
            [play-clj.math :as m]
            [play-clj.utils :as u])
  (:import [com.badlogic.gdx.math MathUtils Vector2]
           [com.badlogic.gdx.physics.box2d Body BodyDef ChainShape CircleShape
            Contact ContactListener EdgeShape Fixture FixtureDef Joint JointDef
            PolygonShape Transform]))

; world

(defn box-2d*
  ([]
    (box-2d* 0 0 true))
  ([gravity-x gravity-y]
    (box-2d* gravity-x gravity-y true))
  ([gravity-x gravity-y sleep?]
    ; use reflection to instantiate in order to avoid the static initializer
    (some-> (try (Class/forName "com.badlogic.gdx.physics.box2d.World")
              (catch Exception _))
            .getDeclaredConstructors
            first
            (.newInstance
              (object-array [(m/vector-2 gravity-x gravity-y) sleep?])))))

(defmacro box-2d
  "Returns a [World](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/World.html).

    (box-2d 0 0)"
  [gravity-x gravity-y & options]
  `(let [object# (box-2d* ~gravity-x ~gravity-y)]
     (u/calls! object# ~@options)))

(defmacro box-2d!
  "Calls a single method on a `box-2d`."
  [screen k & options]
  `(let [object# (u/get-obj ~screen :world)]
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
     (u/fields! object# :type ~(body-type k) ~@options)))

(defmacro body!
  "Calls a single method on a body."
  [entity k & options]
  `(let [^Body object# (u/get-obj ~entity :body)]
     (u/call! object# ~k ~@options)))

(defn add-body!
  "Creates a body from `b-def`, adds it to the `screen` and returns it.

    (add-body! screen (body-def :dynamic))"
  [screen b-def]
  (box-2d! screen :create-body b-def))

(defn ^:private body-x
  [entity]
  (. (body! entity :get-position) x))

(defn ^:private body-y
  [entity]
  (. (body! entity :get-position) y))

(defn ^:private body-angle
  [entity]
  (* (body! entity :get-angle) MathUtils/radiansToDegrees))

(defn ^:private body-origin-x
  [entity]
  (. ^Vector2 (body! entity :get-local-center) x))

(defn ^:private body-origin-y
  [entity]
  (. ^Vector2 (body! entity :get-local-center) y))

(defn body-position!
  "Changes the position of the body in `entity`. The angle is in degrees."
  [entity x y angle]
  (body! entity :set-transform x y (* angle MathUtils/degreesToRadians)))

(defn body-x!
  "Changes the `x` of the body in `entity`."
  [entity x]
  (body-position! entity x (body-y entity) (body-angle entity)))

(defn body-y!
  "Changes the `y` of the body in `entity`."
  [entity y]
  (body-position! entity (body-x entity) y (body-angle entity)))

(defn body-angle!
  "Changes the `angle` (degrees) of the body in `entity`."
  [entity angle]
  (body-position! entity (body-x entity) (body-y entity) angle))

(defn ^:private find-body
  [body entities]
  (some #(if (= body (u/get-obj % :body)) %) entities))

(defn first-entity
  "Returns the first entity in a contact. May only be used in contact functions
such as :on-begin-contact."
  [screen entities]
  (let [^Contact contact (u/get-obj screen :contact)]
    (-> contact .getFixtureA .getBody (find-body entities))))

(defn second-entity
  "Returns the second entity in a contact. May only be used in contact functions
such as :on-begin-contact."
  [screen entities]
  (let [^Contact contact (u/get-obj screen :contact)]
    (-> contact .getFixtureB .getBody (find-body entities))))

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

(defn add-joint!
  "Adds the `joint` to the `screen` for physics simulations and returns it."
  [screen j-def]
  (box-2d! screen :create-joint j-def))

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
  []
  (CircleShape.))

(defmacro circle-shape
  "Returns a [CircleShape](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/CircleShape.html)."
  [& options]
  `(u/calls! ^CircleShape (circle-shape*) ~@options))

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

(defmethod c/contact-listener
  "com.badlogic.gdx.physics.box2d.World"
  [screen
   {:keys [on-begin-contact on-end-contact on-post-solve on-pre-solve]}
   execute-fn!]
  (reify ContactListener
    (beginContact [this c]
      (execute-fn! on-begin-contact :contact c))
    (endContact [this c]
      (execute-fn! on-end-contact :contact c))
    (postSolve [this c i]
      (execute-fn! on-post-solve :contact c :impulse i))
    (preSolve [this c m]
      (execute-fn! on-pre-solve :contact c :old-manifold m))))

(defmethod c/update-physics!
  "com.badlogic.gdx.physics.box2d.World"
  [{:keys [world contact-listener]} & [entities]]
  (.setContactListener world contact-listener)
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

(defn step!
  "Runs the physics simulations for a single frame and optionally returns the
`entities` with their positions updated."
  ([{:keys [world time-step velocity-iterations position-iterations]
     :or {time-step (/ 1 60) velocity-iterations 10 position-iterations 10}
     :as screen}]
    (.step world time-step velocity-iterations position-iterations))
  ([screen entities]
    (step! screen)
    (map (fn [e]
           (if (:body e)
             (assoc e
                    :x (body-x e)
                    :y (body-y e)
                    :angle (body-angle e)
                    :origin-x (body-origin-x e)
                    :origin-y (body-origin-y e))
             e))
         entities)))
