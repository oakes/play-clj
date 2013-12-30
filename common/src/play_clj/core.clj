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

(defn find-pos
  [val coll]
  (let [pos (if (number? val)
              val
              (.indexOf coll val))]
    (if (and (>= pos 0) (< pos (count coll)))
      pos
      nil)))

(defn defscreen*
  [{:keys [on-show on-render on-dispose on-hide on-pause on-resize on-resume
           state renderer camera]
    :as options}]
  (let [screen (atom {})
        on-show (or on-show (fn [s]))
        on-render (or on-render (fn [s d]))
        on-dispose (or on-dispose (fn [s]))
        on-hide (or on-hide (fn [s]))
        on-pause (or on-pause (fn [s]))
        on-resize (or on-resize (fn [s w h]))
        on-resume (or on-resume (fn [s]))
        add-entity (fn [entity]
                     (->> entity
                          (conj (:entities @screen))
                          (swap! screen assoc :entities)))
        rem-entity (fn [entity]
                     (when-let [pos (find-pos entity (:entities @screen))]
                       (->> (subvec (:entities @screen) (inc pos))
                            (concat (subvec (:entities @screen) 0 pos))
                            vec
                            (swap! screen assoc :entities))))
        upd-entity (fn [entity args]
                     (when-let [pos (find-pos entity (:entities @screen))]
                       (swap! screen assoc-in
                              [:entities pos] (apply assoc entity args))))]
    (proxy [Screen] []
      (show []
        (swap! screen assoc
               :renderer (create-renderer renderer)
               :camera (create-camera camera)
               :total-time 0
               :entities []
               :add-entity add-entity
               :rem-entity rem-entity
               :upd-entity upd-entity)
        (when state (swap! screen assoc :state state))
        (on-show @screen))
      (render [delta-time]
        (swap! screen assoc :total-time (+ (:total-time @screen) delta-time))
        (on-render @screen delta-time))
      (dispose [] (on-dispose @screen))
      (hide [] (on-hide @screen))
      (pause [] (on-pause @screen))
      (resize [w h] (on-resize @screen w h))
      (resume [] (on-resume @screen)))))

(defmacro defscreen
  [name & {:keys [] :as options}]
  `(def ~name (defscreen* ~options)))

(defn add!
  [{:keys [add-entity]} e]
  (add-entity e))

(defn remove!
  [{:keys [rem-entity]} e]
  (rem-entity e))

(defn update!
  [{:keys [upd-entity]} e & args]
  (upd-entity e args))

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
