(ns play-clj.utils
  (:require [clojure.string :as s])
  (:import [com.badlogic.gdx.graphics.g2d TextureRegion]
           [com.badlogic.gdx.math Vector2 Vector3]
           [com.badlogic.gdx.scenes.scene2d Actor]
           [com.badlogic.gdx.utils Array ArrayMap]))

; converting keys

(def ^:const main-package "com.badlogic.gdx")

(defn ^:private split-key
  [key]
  (-> key name (s/split #"-")))

(defn ^:private join-keys
  [keys]
  (->> keys (map name) (s/join ".") (str main-package ".")))

(defn key->upper
  [key]
  (->> (split-key key)
       (map s/upper-case)
       (s/join "_")))

(defn key->pascal
  [key]
  (->> (split-key key)
       (map s/capitalize)
       (s/join "")))

(defn key->camel
  [key]
  (let [parts (split-key key)]
    (->> (rest parts)
         (map s/capitalize)
         (cons (first parts))
         (s/join ""))))

(defn key->method
  [key]
  (symbol (str "." (key->camel key))))

(defn ^:private static-field
  [args transform-fn]
  (->> (transform-fn (last args))
       (str (join-keys (butlast args)) "/")
       symbol))

(defmacro static-field-lower
  [& args]
  `~(static-field args key->camel))

(defmacro static-field-upper
  [& args]
  `~(static-field args key->upper))

; data structures

(defn gdx-array
  [arr]
  (Array. true (into-array arr) 1 (count arr)))

(defn gdx-array-map
 [hmap]
 (let [amap (ArrayMap.)]
   (doseq [[k v] hmap]
     (.put amap k v))
   amap))

(defn gdx-vector
  ([x y]
    (Vector2. x y))
  ([x y z]
    (Vector3. x y z)))

; java interop

(defmacro call!
  [obj k & args]
  `(~(key->method k) ~obj ~@args))

(defn create-method-calls
  [calls args]
  (let [method-name (first args)
        [my-args rest-args] (split-with #(not (keyword? %)) (rest args))]
    (if method-name
      (create-method-calls (conj calls `(~(key->method method-name) ~@my-args))
                           rest-args)
      calls)))

(defmacro calls!
  [obj & args]
  `(doto ~obj ~@(create-method-calls [] args)))

; entities

(defmulti create-entity class)

(defmethod create-entity TextureRegion
  [obj]
  {:type :texture :object obj})

(defmethod create-entity Actor
  [obj]
  {:type :actor :object obj})
