(ns play-clj.g2d-physics
  (:require [play-clj.math :as m]
            [play-clj.utils :as u])
  (:import [com.badlogic.gdx.physics.box2d Body BodyDef ContactListener
            FixtureDef World]))

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
  `(let [object# (box-2d* ~gravity-x ~gravity-y)]
     (u/calls! ^World object# ~@options)
     object#))

(defmacro box-2d!
  [screen k & options]
  `(u/call! ^World (:world ~screen) ~k ~@options))

; bodies

(defmacro body-type
  [k]
  `~(symbol (str u/main-package ".physics.box2d.BodyDef$BodyType/"
                 (u/key->pascal k) "Body")))

(defn body
  [& {:keys [] :as options}]
  (let [body-def (BodyDef.)]
    (doseq [[k v] options]
      (case k
        :type (set! (. body-def type) v)
        (u/throw-key-not-found k)))
    body-def))

(defmacro body!
  [entity k & options]
  `(u/call! ^Body (or (:body ~entity) ~entity) ~k ~@options))

(defn create-body!*
  [screen body-def]
  (box-2d! screen :create-body body-def))

(defmacro create-body!
  [screen type-name & options]
  `(let [object# (create-body!* ~screen (body :type (body-type ~type-name)))]
     (u/calls! ^Body object# ~@options)
     object#))

; fixtures

(defn fixture
  [& {:keys [] :as options}]
  (let [fixture-def (FixtureDef.)]
    (doseq [[k v] options]
      (case k
        :density (set! (. fixture-def density) v)
        :friction (set! (. fixture-def friction) v)
        :is-sensor? (set! (. fixture-def isSensor) v)
        :restitution (set! (. fixture-def restitution) v)
        :shape (set! (. fixture-def shape) v)))
    fixture-def))

; listeners

(defn contact-listener
  [{:keys [on-begin-contact on-end-contact on-post-solve on-pre-solve]} execute-fn!]
  (reify ContactListener
    (beginContact [this c]
      (execute-fn! on-begin-contact :contact c))
    (endContact [this c]
      (execute-fn! on-end-contact :contact c))
    (postSolve [this c i]
      (execute-fn! on-post-solve :contact c :impulse i))
    (preSolve [this c m]
      (execute-fn! on-pre-solve :contact c :old-manifold m))))
