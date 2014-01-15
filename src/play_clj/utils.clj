(ns play-clj.utils
  (:require [clojure.string :as s])
  (:import [com.badlogic.gdx.graphics.g2d TextureRegion]
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

; static fields

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

(defmacro scaling
  [key]
  `(static-field-lower :utils :Scaling ~key))

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

; data structures

(defn gdx-array*
  [clj-arr]
  (Array. true (into-array clj-arr) 1 (count clj-arr)))

(defmacro gdx-array
  [clj-arr & options]
  `(calls! ^Array (gdx-array* ~clj-arr) ~@options))

(defmacro gdx-array!
  [object k & options]
  `(call! ^Array ~object ~k ~@options))

(defn gdx-array-map*
 [clj-map]
 (let [gdx-map (ArrayMap.)]
   (doseq [[k v] clj-map]
     (.put gdx-map k v))
   gdx-map))

(defmacro gdx-array-map
  [clj-map & options]
  `(calls! ^ArrayMap (gdx-array-map* ~clj-map) ~@options))

(defmacro gdx-array-map!
  [object k & options]
  `(call! ^ArrayMap ~object ~k ~@options))

; entities

(defmulti create-entity class)

(defmethod create-entity TextureRegion
  [obj]
  {:type :texture :object obj})

(defmethod create-entity Actor
  [obj]
  {:type :actor :object obj})
