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
            OrthogonalTiledMapRenderer]))

(load "core_2d")
(load "core_global")
(load "core_render")

(defn wrap-entity
  [screen entity]
  (if (map? entity)
    (draw screen entity)
    entity))

(defn transform-entities
  [screen entities]
  (->> entities list flatten (remove nil?)))

(defn execute-entities
  [screen entities]
  (->> entities
       (transform-entities screen)
       (map #(wrap-entity screen %))
       (map #(%))
       (remove nil?)
       doall))

(defn defscreen*
  [{:keys [on-show on-render on-dispose on-hide on-pause on-resize on-resume
           state renderer camera]
    :as options}]
  (let [screen (atom {})
        entities (atom '())
        dummy-fn (fn [s e])
        on-show (or on-show dummy-fn)
        on-render (or on-render dummy-fn)
        on-hide (or on-hide dummy-fn)
        on-pause (or on-pause dummy-fn)
        on-resize (or on-resize dummy-fn)
        on-resume (or on-resume dummy-fn)]
    (proxy [Screen] []
      (show []
        (let [screen-map (swap! screen assoc
                                :renderer (create-renderer renderer)
                                :camera (create-camera camera)
                                :width (game :width)
                                :height (game :height)
                                :total-time 0
                                :delta-time 0)]
          (->> (on-show screen-map @entities)
               (transform-entities screen-map)
               (reset! entities))))
      (render [delta-time]
        (let [total-time (+ (:total-time @screen) delta-time)
              screen-map (swap! screen assoc
                                :total-time total-time
                                :delta-time delta-time)]
          (->> (on-render screen-map @entities)
               (execute-entities screen-map)
               (reset! entities))))
      (hide []
        (->> (on-hide @screen @entities)
             (execute-entities @screen)
             (reset! entities)))
      (pause []
        (->> (on-pause @screen @entities)
             (execute-entities @screen)
             (reset! entities)))
      (resize [w h]
        (let [screen-map (swap! screen assoc
                                :width w
                                :height h)]
          (->> (on-resize screen-map @entities)
               (execute-entities screen-map)
               (reset! entities))))
      (resume []
        (->> (on-resume @screen @entities)
             (execute-entities @screen)
             (reset! entities))))))

(defmacro defscreen
  [name & {:keys [] :as options}]
  `(def ~name (defscreen* ~options)))

(defn defgame*
  [{:keys [on-create]}]
  (proxy [Game] []
    (create []
      (execute-entities nil (on-create this)))))

(defmacro defgame
  [name & {:keys [] :as options}]
  `(def ~name (defgame* ~options)))

(defn set-screen
  [^Game game ^Screen screen]
  (fn []
    (.setScreen game screen)
    nil))
