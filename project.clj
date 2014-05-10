(defproject play-clj "0.3.4-SNAPSHOT"
  :description "A LibGDX wrapper for easy cross-platform game development"
  :url "https://github.com/oakes/play-clj"
  :license {:name "Public Domain"
            :url "http://unlicense.org/UNLICENSE"}
  :dependencies [[com.badlogicgames.gdx/gdx "1.0.1"]
                 [com.badlogicgames.gdx/gdx-box2d "1.0.1"]
                 [com.badlogicgames.gdx/gdx-bullet "1.0.1"]
                 [org.clojure/clojure "1.6.0"]]
  :source-paths ["src"]
  :java-source-paths ["src-java"]
  :javac-options ["-target" "1.6" "-source" "1.6" "-Xlint:-options"])
