(ns play-clj-doclet.core
  (:require [clojure.edn :as edn]
            [clojure.string :as string]
            [clojure.java.io :as io]
            [play-clj-doclet.html :as html])
  (:import [com.sun.javadoc ClassDoc ConstructorDoc Doc ExecutableMemberDoc
            Parameter RootDoc]))

(def targets (-> "targets.edn" io/resource slurp edn/read-string))

(defn parse-param
  [^Parameter p]
  [(.typeName p) (.name p)])

(defn parse-doc
  [^Doc d]
  [(some-> (cond
             (isa? (type d) ConstructorDoc)
             nil
             (isa? (type d) ClassDoc)
             (subs (.name d) (+ 1 (.lastIndexOf (.name d) ".")))
             :else
             (.name d))
           html/camel->keyword)
   (.commentText d)
   (when (isa? (type d) ExecutableMemberDoc)
     (->> d .parameters (map parse-param) vec))])

(defn parse-class-entry
  [^ClassDoc c type]
  (some->> (case type
             :methods (filter #(-> % .isStatic not) (.methods c))
             :static-methods (filter #(.isStatic %) (.methods c))
             :fields (filter #(-> % .isStatic not) (.fields c))
             :static-fields (filter #(.isStatic %) (.fields c))
             :classes (filter #(-> % .isStatic not) (.innerClasses c))
             :static-classes (filter #(.isStatic %) (.innerClasses c))
             :constructors (filter #(-> % .isStatic not) (.constructors c))
             nil)
           (filter #(.isPublic %))
           (map parse-doc)
           (concat (when-let [sc (.superclass c)]
                     (when (not= (.typeName sc) "Object")
                       (parse-class-entry sc type))))
           vec))

(defn parse-class
  [^ClassDoc c]
  (some->> (get targets (.typeName c))
           (map #(vector (first %) (parse-class-entry c (second %))))
           (into {})))

(defn save
  [doc-map]
  (->> doc-map pr-str (spit (io/file "uberdoc.edn")))
  (->> doc-map html/create (spit (io/file "uberdoc.html"))))

(defn parse
  [^RootDoc root]
  (->> (map parse-class (.classes root))
       (filter some?)
       (into {})
       save)
  (println "Created uberdoc.html and uberdoc.edn."))
