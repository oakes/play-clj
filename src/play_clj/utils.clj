(ns play-clj.utils
  (:require [clojure.string :as s])
  (:import [com.badlogic.gdx.graphics.g2d TextureRegion]
           [com.badlogic.gdx.utils Array]
           [com.badlogic.gdx.scenes.scene2d Actor]))

(def ^:const main-package "com.badlogic.gdx")

(defn ^:private split-key
  [key]
  (-> key name (s/split #"-")))

(defn ^:private join-keys
  [keys]
  (->> keys (map name) (s/join ".") (str main-package ".")))

(defn key->static-field
  [key upper?]
  (->> (split-key key)
       (map (if upper? s/upper-case s/lower-case))
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

(defn ^:private static-field
  [args upper?]
  (->> (key->static-field (last args) upper?)
       (str (join-keys (butlast args)) "/")
       symbol))

(defmacro static-field-lower
  [& args]
  `~(static-field args false))

(defmacro static-field-upper
  [& args]
  `~(static-field args true))

(defn convert-array
  [a]
  (Array. true (into-array a) 1 (count a)))

(defmacro call!
  [obj k & args]
  `(~(key->method k) ~obj ~@args))

(defn calls!*
  [[k v]]
  (flatten (list (key->method k) (eval v))))

(defmacro calls!
  [obj & {:keys [] :as args}]
  `(doto ~obj ~@(map calls!* args)))

(defmulti create-entity class)

(defmethod create-entity TextureRegion
  [obj]
  {:type :texture :object obj})

(defmethod create-entity Actor
  [obj]
  {:type :actor :object obj})
