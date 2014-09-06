(defproject {{app-name}} "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  
  :dependencies [[com.badlogicgames.gdx/gdx "1.3.1" :use-resources true]
                 [com.badlogicgames.gdx/gdx-backend-android "1.3.1"]
                 [com.badlogicgames.gdx/gdx-box2d "1.3.1"]
                 [com.badlogicgames.gdx/gdx-bullet "1.3.1"]
                 [neko/neko "3.0.2"]
                 [org.clojure-android/clojure "1.6.0-RC1" :use-resources true]
                 [play-clj "0.3.11"]]
  :profiles {:dev {:dependencies [[android/tools.nrepl "0.2.0-bigstack"]
                                  [compliment "0.1.3"]]
                   :android {:aot :all-with-unused}}
             :release {:android
                       {;; Specify the path to your private
                        ;; keystore and the the alias of the
                        ;; key you want to sign APKs with.
                        ;; :keystore-path "/home/user/.android/private.keystore"
                        ;; :key-alias "mykeyalias"
                        :aot :all}}}
  
  :android {;; Specify the path to the Android SDK directory either
            ;; here or in your ~/.lein/profiles.clj file.
            ;; :sdk-path "/home/user/path/to/android-sdk/"
            
            ;; Uncomment this if dexer fails with OutOfMemoryException
            ;; :force-dex-optimize true
            
            :assets-path "../desktop/resources"
            :native-libraries-paths ["libs"]
            :target-version "{{target-sdk}}"
            :aot-exclude-ns ["clojure.parallel" "clojure.core.reducers"]
            :dex-opts ["-JXmx2048M"]}
  
  :source-paths ["src/clojure" "../desktop/src-common"]
  :java-source-paths ["src/java" "gen"]
  :javac-options ["-target" "1.6" "-source" "1.6" "-Xlint:-options"])
