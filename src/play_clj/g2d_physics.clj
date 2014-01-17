(ns play-clj.g2d-physics
  (:require [play-clj.math :as m]
            [play-clj.utils :as u])
  (:import [com.badlogic.gdx.physics.box2d ContactListener World]))

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
