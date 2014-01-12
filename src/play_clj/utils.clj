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

(defn convert-array
  [a]
  (Array. true (into-array a) 1 (count a)))

(defn create-method-call
  [[k v]]
  (flatten (list (key->method k) (try (eval v)
                                   (catch Exception _ v)))))

(defmacro call!
  [obj k & args]
  `(~(key->method k) ~obj ~@args))

(defmacro calls!
  [obj & {:keys [] :as args}]
  `(doto ~obj ~@(map create-method-call args)))

(defmulti create-entity class)

(defmethod create-entity TextureRegion
  [obj]
  {:type :texture :object obj})

(defmethod create-entity Actor
  [obj]
  {:type :actor :object obj})
