(ns leiningen.new.play-clj
  (:require [clojure.java.io :as io]
            [leiningen.core.main :as main]
            [leiningen.droid.new :as droid-new]
            [leiningen.new.templates :as t]))

(defn play-clj
  [name & [package-name]]
  (when (contains? #{"game" "main-screen"} name)
    (main/abort "Choose a more creative name than that, silly!"))
  (let [render (t/renderer "play-clj")
        lein-droid-render (droid-new/renderer "templates")
        desktop-class-name "desktop-launcher"
        android-class-name "AndroidLauncher"
        ios-class-name "IOSLauncher"
        package-name (t/sanitize (t/multi-segment (or package-name name)))
        package-prefix (->> (.lastIndexOf package-name ".")
                            (subs package-name 0))
        main-ns (t/sanitize-ns package-name)
        desktop-ns (str main-ns "." desktop-class-name)
        android-ns (str package-name "." android-class-name)
        ios-ns (str package-name "." ios-class-name)
        data {:app-name name
              :name (t/project-name name)
              :package package-name
              :package-sanitized package-name
              :package-prefix package-prefix
              :desktop-class-name desktop-class-name
              :android-class-name android-class-name
              :ios-class-name ios-class-name
              :activity android-class-name
              :namespace main-ns
              :desktop-namespace desktop-ns
              :android-namespace android-ns
              :ios-namespace ios-ns
              :path (t/name-to-path main-ns)
              :desktop-path (t/name-to-path desktop-ns)
              :android-path (t/name-to-path android-ns)
              :ios-path (t/name-to-path ios-ns)
              :year (t/year)
              :target-sdk "15"}]
    (t/->files data
               ; main
               ["README.md" (render "README.md" data)]
               [".gitignore" (render "gitignore" data)]
               ; desktop
               ["desktop/project.clj" (render "desktop-project.clj" data)]
               ["desktop/src-common/{{path}}.clj" (render "core.clj" data)]
               ["desktop/src/{{desktop-path}}.clj"
                (render "desktop-launcher.clj" data)]
               "desktop/src-common"
               "desktop/src"
               "desktop/resources"
               ; android
               ["android/project.clj"
                (render "android-project.clj" data)]
               ["android/src/java/{{android-path}}.java"
                (render "AndroidLauncher.java" data)]
               "android/src/clojure"
               ["android/AndroidManifest.xml"
                (render "AndroidManifest.xml" data)]
               ["android/res/drawable-hdpi/ic_launcher.png"
                (lein-droid-render "ic_launcher_hdpi.png")]
               ["android/res/drawable-mdpi/ic_launcher.png"
                (lein-droid-render "ic_launcher_mdpi.png")]
               ["android/res/drawable-ldpi/ic_launcher.png"
                (lein-droid-render "ic_launcher_ldpi.png")]
               ["android/res/values/strings.xml"
                (lein-droid-render "strings.xml" data)]
               ["android/res/drawable-hdpi/splash_circle.png"
                (lein-droid-render "splash_circle.png")]
               ["android/res/drawable-hdpi/splash_droid.png"
                (lein-droid-render "splash_droid.png")]
               ["android/res/drawable-hdpi/splash_hands.png"
                (lein-droid-render "splash_hands.png")]
               ["android/res/drawable/splash_background.xml"
                (lein-droid-render "splash_background.xml")]
               ["android/res/anim/splash_rotation.xml"
                (lein-droid-render "splash_rotation.xml")]
               ["android/res/layout/splashscreen.xml"
                (lein-droid-render "splashscreen.xml")]
               ["android/src/java/{{path}}/SplashActivity.java"
                (lein-droid-render "SplashActivity.java" data)]
               ; android libgdx.so
               ["android/libs/armeabi/libgdx.so"
                (io/input-stream (io/resource "armeabi/libgdx.so"))]
               ["android/libs/armeabi-v7a/libgdx.so"
                (io/input-stream (io/resource "armeabi-v7a/libgdx.so"))]
               ["android/libs/x86/libgdx.so"
                (io/input-stream (io/resource "x86/libgdx.so"))]
               ; android libgdx-box2d.so
               ["android/libs/armeabi/libgdx-box2d.so"
                (io/input-stream (io/resource "armeabi/libgdx-box2d.so"))]
               ["android/libs/armeabi-v7a/libgdx-box2d.so"
                (io/input-stream (io/resource "armeabi-v7a/libgdx-box2d.so"))]
               ["android/libs/x86/libgdx-box2d.so"
                (io/input-stream (io/resource "x86/libgdx-box2d.so"))]
               ; android libgdx-bullet.so
               ["android/libs/armeabi/libgdx-bullet.so"
                (io/input-stream (io/resource "armeabi/libgdx-bullet.so"))]
               ["android/libs/armeabi-v7a/libgdx-bullet.so"
                (io/input-stream (io/resource "armeabi-v7a/libgdx-bullet.so"))]
               ["android/libs/x86/libgdx-bullet.so"
                (io/input-stream (io/resource "x86/libgdx-bullet.so"))]
               ; ios
               ["ios/project.clj" (render "ios-project.clj" data)]
               ["ios/Info.plist.xml" (render "Info.plist.xml" data)]
               "ios/src/clojure"
               ["ios/src/java/{{ios-path}}.java"
                (render "IOSLauncher.java" data)]
               ; ios libObjectAL.a and libgdx.a
               ["ios/libs/libObjectAL.a"
                (io/input-stream (io/resource "ios/libObjectAL.a"))]
               ["ios/libs/libgdx.a"
                (io/input-stream (io/resource "ios/libgdx.a"))]
               ; ios libgdx-box2d.a
               ["ios/libs/libgdx-box2d.a"
                (io/input-stream (io/resource "ios/libgdx-box2d.a"))]
               ; ios libgdx-bullet.a
               ["ios/libs/libgdx-bullet.a"
                (io/input-stream (io/resource "ios/libgdx-bullet.a"))])))
