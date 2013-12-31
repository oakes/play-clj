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

(defmulti execute-entity :command :default :draw)

(load "core_2d")
(load "core_global")
(load "core_render")

(defn expand-entity
  [entity]
  (if (keyword? entity)
    {:command entity}
    entity))

(defn transform-entities
  [entities]
  (->> entities list flatten (remove nil?) (map expand-entity)))

(defn execute-entities
  [screen entities]
  (->> entities
       (map #(assoc % :screen screen))
       (map execute-entity)
       (remove #(not (:persistent? %)))
       doall))

(defn defscreen*
  [{:keys [on-show on-render on-dispose on-hide on-pause on-resize on-resume
           state renderer camera]
    :as options}]
  (let [screen (atom {})
        entities (atom '())
        on-show (or on-show (fn [s]))
        on-render (or on-render (fn [s d]))
        on-hide (or on-hide (fn [s]))
        on-pause (or on-pause (fn [s]))
        on-resize (or on-resize (fn [s w h]))
        on-resume (or on-resume (fn [s]))]
    (proxy [Screen] []
      (show []
        (->> (swap! screen assoc
                    :renderer (create-renderer renderer)
                    :camera (create-camera camera)
                    :total-time 0
                    :delta-time 0)
             on-show
             transform-entities
             (reset! entities)))
      (render [delta-time]
        (let [total-time (+ (:total-time @screen) delta-time)
              screen-map (swap! screen assoc
                                :total-time total-time
                                :delta-time delta-time)]
          (->> (on-render screen-map @entities)
               transform-entities
               (execute-entities screen-map)
               (reset! entities))))
      (hide [] (on-hide @screen))
      (pause [] (on-pause @screen))
      (resize [w h] (on-resize @screen w h))
      (resume [] (on-resume @screen)))))

(defmacro defscreen
  [name & {:keys [] :as options}]
  `(def ~name (defscreen* ~options)))

(defn set-screen!
  [^Game game ^Screen screen]
  (.setScreen game screen))

(defn defgame*
  [{:keys [start-screen]}]
  (proxy [Game] []
    (create [] (when start-screen (set-screen! this start-screen)))))

(defmacro defgame
  [name & {:keys [] :as options}]
  `(def ~name (defgame* ~options)))
