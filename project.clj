(defproject play-clj "0.3.9-SNAPSHOT"
  :description "A libGDX wrapper for easy cross-platform game development"
  :url "https://github.com/oakes/play-clj"
  :license {:name "Public Domain"
            :url "http://unlicense.org/UNLICENSE"}
  :dependencies [[com.badlogicgames.gdx/gdx "1.2.1-SNAPSHOT"]
                 [com.badlogicgames.gdx/gdx-box2d "1.2.1-SNAPSHOT"]
                 [com.badlogicgames.gdx/gdx-bullet "1.2.1-SNAPSHOT"]
                 [org.clojure/clojure "1.6.0"]]
  :repositories [["sonatype"
                  "https://oss.sonatype.org/content/repositories/snapshots/"]]
  :source-paths ["src"]
  :java-source-paths ["src-java"]
  :javac-options ["-target" "1.6" "-source" "1.6" "-Xlint:-options"])
