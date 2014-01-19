(ns play-clj.g2d-physics
  (:require [play-clj.math :as m]
            [play-clj.utils :as u])
  (:import [com.badlogic.gdx.physics.box2d Body BodyDef ChainShape CircleShape
            Contact EdgeShape Fixture FixtureDef JointDef PolygonShape Transform
            World]))

; world

(defn box-2d*
  ([]
    (box-2d* 0 0 true))
  ([gravity-x gravity-y]
    (box-2d* gravity-x gravity-y true))
  ([gravity-x gravity-y sleep?]
    (World. (m/vector-2 gravity-x gravity-y) sleep?)))

(defmacro box-2d
  [gravity-x gravity-y & options]
  `(let [^World object# (box-2d* ~gravity-x ~gravity-y)]
     (u/calls! object# ~@options)))

(defmacro box-2d!
  [screen k & options]
  `(let [^World object# (u/get-obj ~screen :world)]
     (u/call! object# ~k ~@options)))

; bodies

(defmacro ^:private body-type
  [k]
  `(symbol (str u/main-package ".physics.box2d.BodyDef$BodyType/"
                (u/key->pascal ~k) "Body")))

(defmacro body-def
  [k & options]
  `(let [^BodyDef object# (BodyDef.)]
     (set! (. object# type) ~(body-type k))
     (u/fields! object# ~@options)))

(defmacro body!
  [entity k & options]
  `(let [^Body object# (u/get-obj ~entity :body)]
     (u/call! object# ~k ~@options)))

(defn create-body!*
  [screen b-def]
  (box-2d! screen :create-body b-def))

(defmacro create-body!
  [screen b-def & options]
  `(let [^Body object# (create-body!* ~screen ~b-def)]
     (u/calls! object# ~@options)))

(defn body-x
  [entity]
  (. (body! entity :get-position) x))

(defn body-y
  [entity]
  (. (body! entity :get-position) y))

(defn body-angle
  [entity]
  (.getRotation ^Transform (body! entity :get-transform)))

(defn body-transform!
  [entity x y angle]
  (body! entity :set-transform x y angle)
  entity)

(defn body-x!
  [entity x]
  (body-transform! entity x (body-y entity) (body-angle entity)))

(defn body-y!
  [entity y]
  (body-transform! entity (body-x entity) y (body-angle entity)))

(defn body-angle!
  [entity angle]
  (body-transform! entity (body-x entity) (body-y entity) angle))

; joints

(defmacro ^:private joint-init
  [k]
  `(symbol (str u/main-package ".physics.box2d.joints."
                (u/key->pascal ~k) "JointDef.")))

(defmacro joint-def
  [k & options]
  `(let [object# (~(joint-init k))]
     (u/fields! object# ~@options)))

(defmacro joint!
  [object k & options]
  `(u/call! ^Joint ~object ~k ~@options))

(defn create-joint!*
  [screen j-def]
  (box-2d! screen :create-joint j-def))

(defmacro create-joint!
  [screen j-def & options]
  `(let [object# (create-joint!* ~screen ~j-def)]
     (u/calls! object# ~@options)))

; fixtures

(defmacro fixture-def
  [& options]
  `(let [^FixtureDef object# (FixtureDef.)]
     (u/fields! object# ~@options)
     object#))

(defmacro fixture!
  [object k & options]
  `(u/call! ^Fixture ~object ~k ~@options))

; shapes

(defn chain*
  []
  (ChainShape.))

(defmacro chain
  [& options]
  `(u/calls! ^ChainShape (chain*) ~@options))

(defmacro chain!-shape
  [object k & options]
  `(u/call! ^ChainShape ~object ~k ~@options))

(defn circle*
  ([]
    (CircleShape.))
  ([radius]
    (doto ^CircleShape (circle*)
      (.setRadius radius)
      (.setPosition (m/vector-2 radius radius)))))

(defmacro circle
  [radius & options]
  `(u/calls! ^CircleShape (circle* ~radius) ~@options))

(defmacro circle!
  [object k & options]
  `(u/call! ^CircleShape ~object ~k ~@options))

(defn edge*
  []
  (EdgeShape.))

(defmacro edge
  [& options]
  `(u/calls! ^EdgeShape (edge*) ~@options))

(defmacro edge!
  [object k & options]
  `(u/call! ^EdgeShape ~object ~k ~@options))

(defn polygon*
  []
  (PolygonShape.))

(defmacro polygon
  [& options]
  `(u/calls! ^PolygonShape (polygon*) ~@options))

(defmacro polygon!
  [object k & options]
  `(u/call! ^PolygonShape ~object ~k ~@options))

; misc

(defmacro contact!
  [object k & options]
  `(u/call! ^Contact ~object ~k ~@options))

(defn find-body
  [body entities]
  (some #(if (= body (:body %)) %) entities))

(defn first-contact
  ([screen]
    (let [^Contact contact (u/get-obj screen :contact)]
      (assert contact)
      (-> contact .getFixtureA .getBody)))
  ([screen entities]
    (find-body (first-contact screen) entities)))

(defn second-contact
  ([screen]
    (let [^Contact contact (u/get-obj screen :contact)]
      (assert contact)
      (-> contact .getFixtureB .getBody)))
  ([screen entities]
    (find-body (second-contact screen) entities)))

(defn step!
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
