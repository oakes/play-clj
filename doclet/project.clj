(defproject play-clj-doclet "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[hiccup "1.0.5"]
                 [marginalia "0.7.1"]
                 [markdown-clj "0.9.41"]
                 [org.clojure/clojure "1.6.0"]]
  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"]
  :javac-options ["-target" "1.6" "-source" "1.6" "-Xlint:-options"]
  :aot :all)
