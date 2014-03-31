(ns play-clj-doclet.core
  (:require [clojure.edn :as edn]
            [clojure.string :as string]
            [clojure.java.io :as io])
  (:import [com.sun.javadoc ClassDoc ConstructorDoc Doc ExecutableMemberDoc
            Parameter RootDoc]))

(def targets (-> "targets.edn" io/resource slurp edn/read-string))

(defn camel->keyword
  [s]
  (->> (string/split (string/replace s "_" "-") #"(?<=[a-z])(?=[A-Z])")
       (map string/lower-case)
       (string/join "-")
       keyword))

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
           camel->keyword)
   (.commentText d)
   (when (isa? (type d) ExecutableMemberDoc)
     (->> d .parameters (map parse-param) vec))])

(defn parse-class-entry
  [^ClassDoc c [clj-name type]]
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
                       (parse-class-entry sc [clj-name type]))))
           vec
           (vector clj-name)))

(defn parse-class
  [^ClassDoc c]
  (some->> (get targets (.typeName c))
           (map #(parse-class-entry c %))
           (into {})))

(defn parse
  [^RootDoc root]
  (->> (map parse-class (.classes root))
       (filter some?)
       (into {})
       pr-str
       (spit (io/file "java.edn")))
  (println "Created edn file."))
