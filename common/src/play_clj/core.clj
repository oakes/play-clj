(ns play-clj.core
  (:require [play-clj.utils :as utils])
  (:import [com.badlogic.gdx Game Gdx Input$Keys Screen]
           [com.badlogic.gdx.graphics Camera Color GL20 OrthographicCamera
            PerspectiveCamera Texture]
           [com.badlogic.gdx.graphics.g2d Animation SpriteBatch TextureRegion]
           [com.badlogic.gdx.maps.tiled TmxMapLoader]
           [com.badlogic.gdx.maps.tiled.renderers
            BatchTiledMapRenderer
            HexagonalTiledMapRenderer
            IsometricStaggeredTiledMapRenderer
            IsometricTiledMapRenderer
            OrthogonalTiledMapRenderer]
           [com.badlogic.gdx.scenes.scene2d Actor Stage]))

(load "core_2d")
(load "core_global")
(load "core_render")

(defn defscreen*
  [{:keys [on-show on-render on-dispose on-hide on-pause on-resize on-resume]
    :as options}]
  (let [screen (atom {})
        dummy-fn (fn [s])
        on-show (or on-show dummy-fn)
        on-render (or on-render dummy-fn)
        on-hide (or on-hide dummy-fn)
        on-pause (or on-pause dummy-fn)
        on-resize (or on-resize dummy-fn)
        on-resume (or on-resume dummy-fn)]
    (proxy [Screen] []
      (show []
        (on-show (swap! screen assoc
                        :width (game :width)
                        :height (game :height)
                        :total-time 0
                        :delta-time 0
                        :set-entities #(swap! screen assoc :entities %)
                        :create-renderer #(swap! screen assoc
                                                 :renderer (renderer %))
                        :create-camera #(swap! screen assoc
                                               :camera (camera %)))))
      (render [delta-time]
        (on-render (swap! screen assoc
                          :total-time (+ (:total-time @screen) delta-time)
                          :delta-time delta-time)))
      (hide [] (on-hide @screen))
      (pause [] (on-pause @screen))
      (resize [w h] (on-resize (swap! screen assoc :width w :height h)))
      (resume [] (on-resume @screen)))))

(defmacro defscreen
  [name & {:keys [] :as options}]
  `(def ~name (defscreen* ~options)))

(defn defgame*
  [{:keys [on-create]}]
  (proxy [Game] []
    (create [] (on-create this))))

(defmacro defgame
  [name & {:keys [] :as options}]
  `(def ~name (defgame* ~options)))

(defn set-screen!
  [^Game game ^Screen screen]
  (.setScreen game screen))

(defn set-entities!
  [{:keys [set-entities]} entities]
  (:entities (set-entities entities)))

(defn get-entities
  [{:keys [entities]}]
  entities)

(defn create-renderer!
  [{:keys [create-renderer]} & {:keys [] :as args}]
  (:renderer (create-renderer args)))

(defn create-camera!
  [{:keys [create-camera]} & {:keys [] :as args}]
  (:camera (create-camera args)))
