(ns play-clj.utils
  (:require [clojure.string :as s])
  (:import [com.badlogic.gdx.utils Array]))

(defn- split-keys
  [key]
  (-> key name (s/split #"-")))

(defn- join-keys
  [keys]
  (->> keys (map name) (s/join ".") (str "com.badlogic.gdx.")))

(defn static-field*
  [args]
  (->> (last args)
       split-keys
       (map s/upper-case)
       (s/join "_")
       (str (join-keys (butlast args)) "/")
       symbol))

(defmacro static-field
  [& args]
  `~(static-field* args))

(defn into-gdx-array
  [a]
  (-> a into-array Array.))
