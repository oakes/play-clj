(ns play-clj.g2d-physics
  (:require [play-clj.math :as m]
            [play-clj.utils :as u])
  (:import [com.badlogic.gdx.physics.box2d World]))

(defn box2d*
  ([]
    (box2d* 0 0 true))
  ([gravity-x gravity-y]
    (box2d* gravity-x gravity-y true))
  ([gravity-x gravity-y sleep?]
    (World. (m/vector-2 gravity-x gravity-y) sleep?)))

(defmacro box2d
  [gravity-x gravity-y & options]
  `(let [object# (box2d* ~gravity-x ~gravity-y)]
     (u/calls! ^World object# ~@options)
     object#))

(defmacro box2d!
  [{:keys [^World world]} k & options]
  `(u/call! ^World ~world ~k ~@options))
