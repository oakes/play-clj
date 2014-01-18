(ns play-clj.utils
  (:require [clojure.string :as s])
  (:import [com.badlogic.gdx.graphics.g2d TextureRegion]
           [com.badlogic.gdx.scenes.scene2d Actor]
           [com.badlogic.gdx.utils Array ArrayMap]))

; exceptions

(defn throw-key-not-found
  [k]
  (throw (Exception. (str "The keyword " k " is not supported."))))

; converting keys

(def ^:const main-package "com.badlogic.gdx")

(defn ^:private split-key
  [k]
  (-> k name (s/split #"-")))

(defn ^:private join-keys
  [k-list]
  (->> k-list (map name) (s/join ".") (str main-package ".")))

(defn key->upper
  [k]
  (->> (split-key k)
       (map s/upper-case)
       (s/join "_")))

(defn key->pascal
  [k]
  (->> (split-key k)
       (map s/capitalize)
       (s/join "")))

(defn key->camel
  [k]
  (let [parts (split-key k)]
    (->> (rest parts)
         (map s/capitalize)
         (cons (first parts))
         (s/join ""))))

(defn key->method
  [k]
  (symbol (str "." (key->camel k))))

; static methods/fields

(defn static-symbol
  [args transform-fn]
  (->> (transform-fn (last args))
       (str (join-keys (butlast args)) "/")
       symbol))

(defmacro static-field-lower
  [& args]
  `~(static-symbol args key->camel))

(defmacro static-field-upper
  [& args]
  `~(static-symbol args key->upper))

(defmacro scaling
  [k]
  `(static-field-lower :utils :Scaling ~k))

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

(defn create-field-setters
  [obj {:keys [] :as args}]
  (map (fn [[k v]]
         `(set! (. ~obj ~(symbol (key->camel k))) ~v))
       args))

(defmacro fields!
  [obj & args]
  `(do ~@(create-field-setters obj args)
     ~obj))

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
