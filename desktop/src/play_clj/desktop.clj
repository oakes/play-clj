(ns play-clj.desktop
  (:require [play-clj.core :refer :all])
  (:import [com.badlogic.gdx.backends.lwjgl LwjglApplication]
           [org.lwjgl.input Keyboard]))

(defmacro defgame
  [name & {:keys [title width height]
           :as options}]
  (let [title (or title "")
        width (or width 800)
        height (or height 600)]
    `(do
       (defgameobj ~name ~options)
       (defn ~'-main
         []
         (LwjglApplication. ~name ~title ~width ~height true)
         (Keyboard/enableRepeatEvents true)))))
