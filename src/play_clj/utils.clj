(ns play-clj.utils
  (:require [clojure.string :as s])
  (:import [com.badlogic.gdx.utils Array]))

(def ^:const gdx-package "com.badlogic.gdx")

(defn- split-key
  [key]
  (-> key name (s/split #"-")))

(defn- join-keys
  [keys]
  (->> keys (map name) (s/join ".") (str gdx-package ".")))

(defn key->static-field
  [key]
  (->> (split-key key)
       (map s/upper-case)
       (s/join "_")
       symbol))

(defn key->class
  [key]
  (->> (split-key key)
       (map s/capitalize)
       (s/join "")
       symbol))

(defn key->method
  [key]
  (let [parts (split-key key)]
    (->> (rest parts)
         (map s/capitalize)
         (cons (first parts))
         (s/join "")
         (str ".")
         symbol)))

(defn gdx-static-field*
  [args]
  (->> (key->static-field (last args))
       (str (join-keys (butlast args)) "/")
       symbol))

(defmacro gdx-static-field
  [& args]
  `~(gdx-static-field* args))

(defn gdx-into-array
  [a]
  (Array. true (into-array a) 1 (count a)))

(defmacro call!
  [obj k & args]
  `(~(key->method k) ~obj ~@args))

(defn calls!*
  [[k v]]
  (flatten (list (key->method k) v)))

(defmacro calls!
  [obj & {:keys [] :as args}]
  `(doto ~obj ~@(map calls!* args)))
