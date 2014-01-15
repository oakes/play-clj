(ns play-clj.physics
  (:require [play-clj.math :as m]
            [play-clj.utils :as u])
  (:import [com.badlogic.gdx.physics.box2d World]))

(defn world*
  ([]
    (world* 0 0 true))
  ([gravity-x gravity-y]
    (world* gravity-x gravity-y true))
  ([gravity-x gravity-y sleep?]
    (World. (m/vector-2 gravity-x gravity-y) sleep?)))

(defmacro world
  [gravity-x gravity-y & options]
  `(let [object# (world* ~gravity-x ~gravity-y)]
     (u/calls! ^World object# ~@options)
     object#))

(defmacro world!
  [{:keys [^World world]} k & options]
  `(u/call! ^World ~world ~k ~@options))
