(ns play-clj.repl
  (:require [clojure.pprint :refer :all]
            [play-clj.core :refer :all]))

(defn entities
  "Returns the entities in `screen-object`, optionally filtered by a supplied
function.

    (entities main-screen :player)"
  ([screen-object]
    (-> screen-object :entities deref))
  ([screen-object filter-fn]
    (filter filter-fn (entities screen-object))))

(defn entities!
  "Associates values to the entities in `screen-object` that match the supplied
function.

    (entities! main-screen :player :health 10)"
  [screen-object filter-fn & args]
  (on-gl (swap! (:entities screen-object)
                (fn [entities]
                  (for [e entities]
                    (if (filter-fn e)
                      (apply assoc e args)
                      e)))))
  (entities screen-object filter-fn))
