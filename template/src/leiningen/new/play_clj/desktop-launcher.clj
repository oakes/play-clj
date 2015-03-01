(ns {{desktop-namespace}}
  (:require [{{namespace}} :refer :all])
  (:import [com.badlogic.gdx.backends.lwjgl LwjglApplication]
           [org.lwjgl.input Keyboard])
  (:gen-class))

(defn -main
  []
  (LwjglApplication. {{game-name}} "{{app-name}}" 800 600)
  (Keyboard/enableRepeatEvents true))
