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

    (e :player? main-screen)
    (e texture? main-screen)"
  ([screen-object]
    (-> screen-object :entities deref))
  ([filter-fn screen-object]
    (vec (filter filter-fn (e screen-object)))))

(defn e!
  "Associates values to the entities in `screen-object` that match the supplied
function. Returns the entities that were changed.

    (e! :player? main-screen :health 10)"
  [filter-fn screen-object & args]
  (swap! (:entities screen-object)
         (fn [entities]
           (vec (for [e entities]
                  (if (filter-fn e)
                    (apply assoc e args)
                    e)))))
  (e filter-fn screen-object))
