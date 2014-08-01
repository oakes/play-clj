(ns play-clj.repl
  (:require [play-clj.core :refer :all]))

(defn s
  "Returns the screen map in `screen-object`.

    (s main-screen)"
  [screen-object]
  (-> screen-object :screen deref))

(defn s!
  "Associates values to the screen map in `screen-object`. Returns the new
screen map.

    (s! main-screen :camera (orthographic))"
  [screen-object & args]
  (apply swap! (:screen screen-object) assoc args))

(defn e
  "Returns the entities in `screen-object`, optionally filtered by a supplied
function.

    (e main-screen :player)"
  ([screen-object]
    (-> screen-object :entities deref))
  ([screen-object filter-fn]
    (filter filter-fn (e screen-object))))

(defn e!
  "Resets the entities in `screen-object`, or associates values to the entities
in `screen-object` that match the supplied function. Returns the entities that
were changed.

    (e! main-screen [])
    (e! main-screen :player :health 10)"
  ([screen-object new-entities]
    (reset! (:entities screen-object) new-entities))
  ([screen-object filter-fn & args]
    (swap! (:entities screen-object)
           (fn [entities]
             (for [e entities]
               (if (filter-fn e)
                 (apply assoc e args)
                 e))))
    (e screen-object filter-fn)))
