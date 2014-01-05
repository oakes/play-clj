(ns play-clj.core
  (:require [play-clj.utils :as utils])
  (:import [com.badlogic.gdx Game Gdx Input$Keys Screen]
           [com.badlogic.gdx.graphics Camera Color GL20 OrthographicCamera
            PerspectiveCamera Texture]
           [com.badlogic.gdx.graphics.g2d Animation SpriteBatch TextureRegion]
           [com.badlogic.gdx.maps MapLayer MapLayers]
           [com.badlogic.gdx.maps.tiled TiledMap TiledMapTileLayer TmxMapLoader]
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

(defn- dummy [& args])

(defn defscreen*
  [{:keys [on-show on-render on-hide on-pause on-resize on-resume]
    :or {on-show dummy on-render dummy on-hide dummy
         on-pause dummy on-resize dummy on-resume dummy}}]
  (let [screen (atom {})
        entities (atom '())
        execute (fn [func screen-map]
                  (some->> (func screen-map @entities)
                           list
                           flatten
                           (remove nil?)
                           (reset! entities)))
        create-renderer-fn! #(swap! screen assoc :renderer (renderer %))
        create-camera-fn! #(swap! screen assoc :camera (camera %))]
    (proxy [Screen] []
      (show []
        (->> (swap! screen assoc
                    :total-time 0
                    :delta-time 0
                    :create-renderer create-renderer-fn!
                    :create-camera create-camera-fn!)
             (execute on-show)))
      (render [delta-time]
        (->> (swap! screen (fn [val]
                             (assoc val
                                    :total-time (+ (:total-time val) delta-time)
                                    :delta-time delta-time)))
             (execute on-render)))
      (hide [] (execute on-hide @screen))
      (pause [] (execute on-pause @screen))
      (resize [w h] (execute on-resize @screen))
      (resume [] (execute on-resume @screen)))))

(defmacro defscreen
  [n & {:keys [] :as options}]
  `(->> (for [[k# v#] ~options]
          [k# (intern *ns* (symbol (str '~n "-" (name k#))) v#)])
        flatten
        (apply hash-map)
        defscreen*
        (def ~n)))

(defn defgame*
  [{:keys [on-create] :or {on-create dummy}}]
  (proxy [Game] []
    (create [] (on-create this))))

(defmacro defgame
  [name & {:keys [] :as options}]
  `(defonce ~name (defgame* ~options)))

(defn set-screen!
  [^Game game ^Screen screen]
  (.setScreen game screen))

(defn create-renderer!
  [{:keys [create-renderer]} & {:keys [] :as args}]
  (:renderer (create-renderer args)))

(defn create-camera!
  [{:keys [create-camera]} & {:keys [] :as args}]
  (:camera (create-camera args)))
