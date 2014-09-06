(ns play-clj-doclet.core
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [marginalia.core :as marg]
            [markdown.core :as m]
            [play-clj-doclet.html :as html])
  (:import [com.sun.javadoc ClassDoc ConstructorDoc Doc ExecutableMemberDoc
            FieldDoc MethodDoc Parameter RootDoc]))

(def ^:const default-ns "play-clj.core")
(def ^:const ignore-files #{"physics.clj" "utils.clj"})

(def classes (-> "classes.edn" io/resource slurp edn/read-string))

(defn split-string
  [s]
  (if (= s (string/upper-case s))
    (string/split s #"_")
    (string/split s #"(?=[A-Z])")))

(defn string->keyword
  [s]
  (->> (split-string s)
       (map string/lower-case)
       (string/join "-")
       keyword))

(defn parse-param
  [^Parameter p]
  [(.typeName p) (-> (.name p) string->keyword name)])

(defn parse-doc-name
  [^Doc d]
  (cond
    (isa? (type d) ConstructorDoc)
    nil

    (isa? (type d) ClassDoc)
    (->> (+ 1 (.lastIndexOf (.name d) "."))
         (subs (.name d))
         string->keyword)

    (isa? (type d) FieldDoc)
    (let [k (string->keyword (.name d))
          s (name k)]
      (if (= \- (first s))
        (keyword (subs s 1))
        k))

    :else
    (string->keyword (.name d))))

(defn parse-doc
  [^Doc d clj-name]
  (merge {}
         {:name (->> [(second (string/split clj-name #" "))
                      (parse-doc-name d)]
                     (remove nil?)
                     (string/join " "))}
         (when (> (count (.commentText d)) 0)
           {:text (.commentText d)})
         (when (and (isa? (type d) MethodDoc)
                    (not= (-> d .returnType .typeName) "void"))
           {:type (-> d .returnType .typeName)})
         (cond
           (isa? (type d) ExecutableMemberDoc)
           {:args (->> d .parameters (map parse-param) vec)}
           (and (isa? (type d) FieldDoc) (not (.isStatic d)))
           {:args [[(-> d .type .typeName) "value"]]})))

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
           (map #(parse-doc % clj-name))
           (concat (when-let [sc (.superclass c)]
                     (when (not= (.typeName sc) "Object")
                       (parse-class-entry sc [clj-name type]))))
           distinct
           (sort-by :name)
           vec))

(defn parse-class
  [^ClassDoc c]
  (some->> (or (get classes (.qualifiedTypeName c))
               (get classes (.typeName c)))
           (map #(vector (first (string/split (first %) #" "))
                         {:text (.commentText c)
                          :items (parse-class-entry c %)}))))

(defn remove-destructuring
  [arglist]
  (map #(or (:as %) %) arglist))

(defn arglists
  [form]
  (->> (or (some-> (some #(if (vector? %) %) form) list)
           (map first (filter list? form)))
       (map #(cons (second form) %))
       (map remove-destructuring)
       vec))

(defn process-group
  [ns {:keys [type raw docstring] :as group} java-docs]
  (let [form (read-string raw)
        n (second form)]
    (when (and (contains? #{'defn 'defmacro} (first form))
               (-> n meta :private not))
      (assoc group
             :ns (if (> (count ns) 0) ns default-ns)
             :name (str n)
             :java (->> java-docs
                        (filter #(= (first %) (str n)))
                        (sort-by first)
                        vec)
             :arglists (arglists form)
             :docstring (m/md-to-html-string docstring)))))

(defn merge-groups
  [groups]
  (for [{:keys [name] :as group} groups]
    (when (and name (not (.endsWith name "*")))
      (->> groups
           (some #(if (= (:name %) (str name "*")) %))
           :raw
           (assoc group :raw*)))))

(defn process-groups
  [{:keys [ns groups] :as parsed-file} java-docs]
  (->> groups
       (map #(process-group ns % java-docs))
       merge-groups
       (remove nil?)))

(defn parse-clj
  [java-docs files]
  (->> files
       (filter #(-> % .getName (.endsWith ".clj")))
       (remove #(contains? ignore-files (.getName %)))
       (sort-by #(.getName %))
       (map #(.getCanonicalPath %))
       (map marg/path-to-doc)
       (map #(process-groups % java-docs))
       (apply concat)
       (group-by :ns)
       (into (sorted-map))))

(defn generate!
  [java-docs]
  (let [play-clj-files (file-seq (io/file "../src/"))
        nightmod-file (io/file "../../nightmod/src/clojure/nightmod/game.clj")
        net-file (io/file "../../play-clj.net/src/play_clj/net.clj")]
    (->> play-clj-files
         (parse-clj java-docs)
         (html/create-site! "site"))
    (when (and (.exists nightmod-file) (.exists net-file))
      (->> play-clj-files
           (concat [nightmod-file net-file])
           (parse-clj java-docs)
           (html/create-embed! "embed")))))

(defn generate-all!
  [^RootDoc root]
  (if (= 0 (count (.classes root)))
    (println "No Java classes found")
    (->> (map parse-class (.classes root))
         (filter some?)
         (apply concat)
         generate!)))
