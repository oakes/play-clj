(ns play-clj.g3d
  (:require [play-clj.utils :as u])
  (:import [com.badlogic.gdx.graphics.g3d Environment ModelBatch]))

; environment

(defn environment*
  "The function version of `environment`"
  []
  (Environment.))

(defmacro environment
  "Returns an [Environment](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g3d/Environment.html)

    (environment)"
  [& options]
  `(let [^Environment object# (environment*)]
     (u/calls! object# ~@options)))

(defmacro environment!
  "Calls a single method on an `environment`"
  [screen k & options]
  `(let [^Environment object# (u/get-obj ~screen :attributes)]
     (u/call! object# ~k ~@options)))

; model-batch

(defn model-batch*
  "The function version of `model-batch`"
  []
  (ModelBatch.))

(defmacro model-batch
  "Returns an [ModelBatch](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g3d/Environment.html)

    (model-batch)"
  [& options]
  `(let [^ModelBatch object# (model-batch*)]
     (u/calls! object# ~@options)))

(defmacro model-batch!
  "Calls a single method on an `model-batch`"
  [screen k & options]
  `(let [^ModelBatch object# (u/get-obj ~screen :attributes)]
     (u/call! object# ~k ~@options)))
