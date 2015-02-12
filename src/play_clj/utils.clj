(ns play-clj.utils
  (:require [clojure.string :as s])
  (:import [com.badlogic.gdx.assets AssetManager]
           [com.badlogic.gdx.utils Array ArrayMap]))

; misc

(defn throw-key-not-found
  [k]
  (throw (Exception. (str "The keyword " k " is not found."))))

(defn get-obj
  [obj k]
  (if (map? obj)
    (or (get obj k)
        (throw-key-not-found k))
    obj))

; assets

(def ^:dynamic *asset-manager* nil)

(defn load-asset
  [path type]
  (when-let [^AssetManager am *asset-manager*]
    (.load am path type)
    (.finishLoading am)
    (.get am path type)))

; timers

(def ^:dynamic *timers* nil)

(defn track-timers!
  []
  (intern 'play-clj.utils '*timers* (atom #{})))

(defn stop-timers!
  []
  (when *timers*
    (doseq [t (deref *timers*)]
      (.stop t))
    (reset! *timers* #{})))

; converting keys

(def ^:const main-package "com.badlogic.gdx")

(defn ^:private split-key
  [k]
  (-> k name (s/split #"-")))

(defn ^:private join-keys
  [k-list]
  (->> k-list (map name) (s/join ".")))

(defn key->upper
  "Returns a string based on keyword `k` with upper case and underscores."
  [k]
  (->> (split-key k)
       (map s/upper-case)
       (s/join "_")))

(defn key->pascal
  "Returns a string based on keyword `k` with pascal case and no delimiters."
  [k]
  (->> (split-key k)
       (map s/capitalize)
       (s/join "")))

(defn key->camel
  "Returns a string based on keyword `k` with camel case and no delimiters."
  [k]
  (let [parts (split-key k)]
    (->> (rest parts)
         (map s/capitalize)
         (cons (first parts))
         (s/join ""))))

(defn key->method
  "Returns a symbol based on keyword `k` formatted as a method call."
  [k]
  (symbol (str "." (key->camel k))))

; classes/methods/fields

(defn ^:private add-divider
  [args divider]
  (let [[a1 a2] (take-last 2 args)]
    (conj (vec (drop-last 2 args)) (str (name a1) divider (name a2)))))

(defn gdx
  "Returns a fully-qualified libGDX symbol."
  [& args]
  (symbol (str main-package "." (join-keys args))))

(defn gdx-field
  "Returns a fully-qualified libGDX static method or field."
  [& args]
  (apply gdx (add-divider args "/")))

(defn gdx-class
  "Returns a fully-qualified libGDX static class."
  [& args]
  (apply gdx (add-divider args "$")))

; java interop

(defmacro call!
  "Calls method `k` of `obj` with `args`.

    (call! \"I'm a string\" :index-of \"s\")"
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
  "Calls methods on `obj`.

    (calls! (java.util.ArrayList.) :add \"I'm a string\" :add \"So am I\")"
  [obj & args]
  `(doto ~obj ~@(create-method-calls [] args)))

(defn create-field-setters
  [obj {:keys [] :as args}]
  (map (fn [[k v]]
         `(set! (. ~obj ~(symbol (key->camel k))) ~v))
       args))

(defmacro fields!
  "Sets fields on `obj` (it is important that `obj` is a symbol, because when
the macro expands it will use it several times, and you probably don't want a
new object to be created each time a field is set).

    (fields! obj
             :active true
             :angle 2
             :awake true
             :fixed-rotation false)"
  [obj & args]
  `(do ~@(create-field-setters obj args)
     ~obj))

; data structures

(defn gdx-array*
  [clj-arr]
  (Array. (into-array clj-arr)))

(defmacro gdx-array
  "Returns an [Array](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/utils/Array.html).

    (gdx-array [1 2 3 4])"
  [clj-arr & options]
  `(calls! ^Array (gdx-array* ~clj-arr) ~@options))

(defmacro gdx-array!
  "Calls a single method on a `gdx-array`."
  [object k & options]
  `(call! ^Array ~object ~k ~@options))

(defn gdx-array-map*
 [clj-map]
 (let [gdx-map (ArrayMap.)]
   (doseq [[k v] clj-map]
     (.put gdx-map k v))
   gdx-map))

(defmacro gdx-array-map
  "Returns an [ArrayMap](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/utils/ArrayMap.html).

    (gdx-array-map {:key-1 1 :key-2 2})"
  [clj-map & options]
  `(calls! ^ArrayMap (gdx-array-map* ~clj-map) ~@options))

(defmacro gdx-array-map!
  "Calls a single method on a `gdx-array-map`."
  [object k & options]
  `(call! ^ArrayMap ~object ~k ~@options))
