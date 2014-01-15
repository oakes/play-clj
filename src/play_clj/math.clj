(ns play-clj.math
  (:import [com.badlogic.gdx.math Vector2 Vector3]))

(defn vector-2*
  [x y]
  (Vector2. x y))

(defmacro vector-2
  [x y & options]
  `(u/calls! ^Vector2 (vector-2* ~x ~y) ~@options))

(defmacro vector-2!
  [object k & options]
  `(u/call! ^Vector2 ~object ~k ~@options))

(defn vector-3*
  [x y z]
  (Vector3. x y z))

(defmacro vector-3
  [x y z & options]
  `(u/calls! ^Vector3 (vector-3* ~x ~y ~z) ~@options))

(defmacro vector-3!
  [object k & options]
  `(u/call! ^Vector3 ~object ~k ~@options))
