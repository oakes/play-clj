(defproject play-clj "0.4.5-SNAPSHOT"
  :description "A libGDX wrapper for easy cross-platform game development"
  :url "https://github.com/oakes/play-clj"
  :license {:name "Public Domain"
            :url "http://unlicense.org/UNLICENSE"}
  :dependencies [[com.badlogicgames.gdx/gdx "1.5.3"]
                 [com.badlogicgames.gdx/gdx-box2d "1.5.3"]
                 [com.badlogicgames.gdx/gdx-bullet "1.5.3"]
                 [org.clojure/clojure "1.6.0"]]
  :repositories [["sonatype"
                  "https://oss.sonatype.org/content/repositories/releases/"]]
  :source-paths ["src"]
  :java-source-paths ["src-java"]
  :javac-options ["-target" "1.6" "-source" "1.6" "-Xlint:-options"])
