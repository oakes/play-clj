(ns play-clj.math
  (:import [com.badlogic.gdx.math Vector2 Vector3]))

(defn vector
  ([x y]
    (Vector2. x y))
  ([x y z]
    (Vector3. x y z)))
