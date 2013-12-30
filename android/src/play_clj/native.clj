(ns play-clj.native
  (:require [play-clj.core :refer :all]))

(defmacro defgame
  [name & {:keys [] :as options}]
  `(def ~name (create-game ~options)))
