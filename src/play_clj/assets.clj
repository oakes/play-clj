(ns play-clj.assets
  (:require [play-clj.core :as play])
  (:import [com.badlogic.gdx.assets AssetManager AssetErrorListener]
           [com.badlogic.gdx.graphics Texture Pixmap]
           [com.badlogic.gdx.graphics.g2d BitmapFont TextureAtlas ParticleEffect]
           [com.badlogic.gdx.audio Music Sound]
           [com.badlogic.gdx.scenes.scene2d.ui Skin]))

(let [last-error (atom nil)]
  (defn get-last-error [] @last-error)
  (defn error-logger []
    (proxy [AssetErrorListener] []
      (error [asset thrown]
             (reset! last-error [asset thrown])
             (println "Error loading asset:" asset "--" thrown)))))

(let [asset-mgr (delay (doto (AssetManager.)
                         (.setErrorListener (error-logger))))]
  (defn get-asset-mgr [] @asset-mgr)
  (defn clear [] (println "Clearing asset manager") (.clear @asset-mgr))
  (defn unload
    [filename]
    (.unload @asset-mgr filename))
  (defn load-asset
    [filename asset-type]
    (try
      ;;This will only work if we're on the OpenGL thread
      (.load @asset-mgr filename asset-type)
      (.finishLoading @asset-mgr)
      (.get @asset-mgr filename asset-type)
      (catch Exception e
        ;;If we're not on that thread... attempt to load it again from there
        (.load @asset-mgr filename asset-type)
        (let [done? (atom nil)]
          (play/on-gl (.finishLoading @asset-mgr) (reset! done? true))
          (while (not @done?) (Thread/sleep 1))
          (when (.isLoaded @asset-mgr filename)
            (.get @asset-mgr filename asset-type)))))))

(defn texture-asset [filename] (load-asset filename Texture))
(defn pixmap-asset [filename] (load-asset filename Pixmap))
(defn bitmap-font-asset [filename] (load-asset filename BitmapFont))
(defn texture-atlas-asset [filename] (load-asset filename TextureAtlas))
(defn particle-effect-asset [filename] (load-asset filename ParticleEffect))

(defn music-asset [filename] (load-asset filename Music))
(defn sound-asset [filename] (load-asset filename Sound))

(defn skin-asset [filename] (load-asset filename Skin))
