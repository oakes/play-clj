(ns play-clj.g2d-physics
  (:require [play-clj.math :as m]
            [play-clj.utils :as u])
  (:import [com.badlogic.gdx.physics.box2d Body BodyDef ChainShape CircleShape
            Contact EdgeShape Fixture FixtureDef JointDef PolygonShape Transform
            World]))

; world

(defn box-2d*
  "The function version of `box-2d`"
  ([]
    (box-2d* 0 0 true))
  ([gravity-x gravity-y]
    (box-2d* gravity-x gravity-y true))
  ([gravity-x gravity-y sleep?]
    (World. (m/vector-2 gravity-x gravity-y) sleep?)))

(defmacro box-2d
  "Returns a [World](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/World.html)

    (box-2d 0 0)
"
  [gravity-x gravity-y & options]
  `(let [^World object# (box-2d* ~gravity-x ~gravity-y)]
     (u/calls! object# ~@options)))

(defmacro box-2d!
  "Calls a single method on a `box-2d`"
  [screen k & options]
  `(let [^World object# (u/get-obj ~screen :world)]
     (u/call! object# ~k ~@options)))

; bodies

(defmacro ^:private body-type
  "Internal use only"
  [k]
  `(symbol (str u/main-package ".physics.box2d.BodyDef$BodyType/"
                (u/key->pascal ~k) "Body")))

(defmacro body-def
  "Returns a [BodyDef](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/BodyDef.html)

    (body-def :dynamic)
"
  [k & options]
  `(let [^BodyDef object# (BodyDef.)]
     (set! (. object# type) ~(body-type k))
     (u/fields! object# ~@options)))

(defmacro body!
  "Calls a single method on a body"
  [entity k & options]
  `(let [^Body object# (u/get-obj ~entity :body)]
     (u/call! object# ~k ~@options)))

(defn create-body!*
  "The function version of `create-body!`"
  [screen b-def]
  (box-2d! screen :create-body b-def))

(defmacro create-body!
  "Returns a [Body](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/Body.html)"
  [screen b-def & options]
  `(let [^Body object# (create-body!* ~screen ~b-def)]
     (u/calls! object# ~@options)))

(defn body-x
  "Returns the x position of the body in `entity`"
  [entity]
  (. (body! entity :get-position) x))

(defn body-y
  "Returns the y position of the body in `entity`"
  [entity]
  (. (body! entity :get-position) y))

(defn body-angle
  "Returns the angle of the body in `entity`"
  [entity]
  (.getRotation ^Transform (body! entity :get-transform)))

(defn body-transform!
  "Changes the `x`, `y`, and `angle` of the body in `entity`"
  [entity x y angle]
  (body! entity :set-transform x y angle)
  entity)

(defn body-x!
  "Changes the `x` of the body in `entity`"
  [entity x]
  (body-transform! entity x (body-y entity) (body-angle entity)))

(defn body-y!
  "Changes the `y` of the body in `entity`"
  [entity y]
  (body-transform! entity (body-x entity) y (body-angle entity)))

(defn body-angle!
  "Changes the `angle` of the body in `entity`"
  [entity angle]
  (body-transform! entity (body-x entity) (body-y entity) angle))

; joints

(defmacro ^:private joint-init
  "Internal use only"
  [k]
  `(symbol (str u/main-package ".physics.box2d.joints."
                (u/key->pascal ~k) "JointDef.")))

(defmacro joint-def
  "Returns a [JointDef](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/JointDef.html)

    (joint-def :rope)
"
  [k & options]
  `(let [object# (~(joint-init k))]
     (u/fields! object# ~@options)))

(defmacro joint!
  "Calls a single method on a joint"
  [object k & options]
  `(u/call! ^Joint ~object ~k ~@options))

(defn create-joint!*
  "The function version of `create-joint`"
  [screen j-def]
  (box-2d! screen :create-joint j-def))

(defmacro create-joint!
  "Returns a [Joint](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/Joint.html)"
  [screen j-def & options]
  `(let [object# (create-joint!* ~screen ~j-def)]
     (u/calls! object# ~@options)))

; fixtures

(defmacro fixture-def
  "Returns a [FixtureDef](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/FixtureDef.html)"
  [& options]
  `(let [^FixtureDef object# (FixtureDef.)]
     (u/fields! object# ~@options)
     object#))

(defmacro fixture!
  "Calls a single method on a fixture"
  [object k & options]
  `(u/call! ^Fixture ~object ~k ~@options))

; shapes

(defn chain*
  "The function version of `chain`"
  []
  (ChainShape.))

(defmacro chain
  "Returns a [ChainShape](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/ChainShape.html)"
  [& options]
  `(u/calls! ^ChainShape (chain*) ~@options))

(defmacro chain-shape!
  "Calls a single method on a `chain`"
  [object k & options]
  `(u/call! ^ChainShape ~object ~k ~@options))

(defn circle*
  "The function version of `circle`"
  ([]
    (CircleShape.))
  ([radius]
    (doto ^CircleShape (circle*)
      (.setRadius radius)
      (.setPosition (m/vector-2 radius radius)))))

(defmacro circle
  "Returns a [CircleShape](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/CircleShape.html)"
  [radius & options]
  `(u/calls! ^CircleShape (circle* ~radius) ~@options))

(defmacro circle!
  "Calls a single method on a `circle`"
  [object k & options]
  `(u/call! ^CircleShape ~object ~k ~@options))

(defn edge*
  "The function version of `edge`"
  []
  (EdgeShape.))

(defmacro edge
  "Returns an [EdgeShape](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/EdgeShape.html)"
  [& options]
  `(u/calls! ^EdgeShape (edge*) ~@options))

(defmacro edge!
  "Calls a single method on a `edge`"
  [object k & options]
  `(u/call! ^EdgeShape ~object ~k ~@options))

(defn polygon*
  "The function version of `polygon`"
  []
  (PolygonShape.))

(defmacro polygon
  "Returns a [PolygonShape](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/PolygonShape.html)"
  [& options]
  `(u/calls! ^PolygonShape (polygon*) ~@options))

(defmacro polygon!
  "Calls a single method on a `polygon`"
  [object k & options]
  `(u/call! ^PolygonShape ~object ~k ~@options))

; misc

(defmacro contact!
  "Calls a single method on a [Contact](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/Contact.html)"
  [object k & options]
  `(u/call! ^Contact ~object ~k ~@options))

(defn find-body
  "Returns the first entity in `entities` whose body matches `body`"
  [body entities]
  (some #(if (= body (:body %)) %) entities))

(defn first-body
  "Returns the first body in a contact"
  [screen]
  (let [^Contact contact (u/get-obj screen :contact)]
    (assert contact)
    (-> contact .getFixtureA .getBody)))

(defn second-body
  "Returns the second body in a contact"
  [screen]
  (let [^Contact contact (u/get-obj screen :contact)]
    (assert contact)
    (-> contact .getFixtureB .getBody)))

(defn step!
  "Runs the physics simulations for a single frame and optionally returns the
`entities` with their positions updated."
  ([{:keys [world time-step velocity-iterations position-iterations]
     :or {time-step (/ 1 60) velocity-iterations 10 position-iterations 10}}]
    (assert world)
    (cond
      (isa? (type world) World)
      (.step ^World world time-step velocity-iterations position-iterations)))
  ([screen entities]
    (step! screen)
    (map (fn [entity]
           (if-let [body (:body entity)]
             (assoc entity
                    :x (body-x body)
                    :y (body-y body))
             entity))
         entities)))
