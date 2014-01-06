(ns play-clj.utils
  (:require [clojure.string :as s])
  (:import [com.badlogic.gdx.utils Array]))

(defn- split-key
  [key]
  (-> key name (s/split #"-")))

(defn- join-keys
  [keys]
  (->> keys (map name) (s/join ".") (str "com.badlogic.gdx.")))

(defn gdx-static-field*
  [args]
  (->> (last args)
       split-key
       (map s/upper-case)
       (s/join "_")
       (str (join-keys (butlast args)) "/")
       symbol))

(defmacro gdx-static-field
  [& args]
  `~(gdx-static-field* args))

(defn gdx-into-array
  [a]
  (Array. true (into-array a) 1 (count a)))
