(ns play-clj.core
  (:require [play-clj.utils :as utils])
  (:import [com.badlogic.gdx Game Gdx Input$Keys Screen]
           [com.badlogic.gdx.graphics Camera Color GL20 OrthographicCamera
            PerspectiveCamera Texture]
           [com.badlogic.gdx.graphics.g2d Animation BitmapFont SpriteBatch
            TextureRegion]
           [com.badlogic.gdx.maps MapLayer MapLayers]
           [com.badlogic.gdx.maps.tiled TiledMap TiledMapTileLayer TmxMapLoader]
           [com.badlogic.gdx.maps.tiled.renderers
            BatchTiledMapRenderer
            HexagonalTiledMapRenderer
            IsometricStaggeredTiledMapRenderer
            IsometricTiledMapRenderer
            OrthogonalTiledMapRenderer]
           [com.badlogic.gdx.scenes.scene2d Actor Stage]
           [com.badlogic.gdx.scenes.scene2d.ui Label Label$LabelStyle]))

(load "core_2d")
(load "core_global")
(load "core_render")
(load "core_ui")

(defn- dummy [& args])

(defn defscreen*
  [{:keys [on-show on-render on-hide on-pause on-resize on-resume]
    :or {on-show dummy on-render dummy on-hide dummy
         on-pause dummy on-resize dummy on-resume dummy}}]
  (let [screen (atom {})
        entities (atom '())
        execute-fn! (fn [func screen-map]
                      (some->> (func screen-map @entities)
                               list
                               flatten
                               (remove nil?)
                               (reset! entities)))
        create-renderer-fn! #(swap! screen assoc :renderer (renderer %))
        create-camera-fn! #(swap! screen assoc :camera (camera %))]
    {:show (fn []
             (->> (swap! screen assoc
                         :total-time 0
                         :delta-time 0
                         :create-renderer create-renderer-fn!
                         :create-camera create-camera-fn!)
                  (execute-fn! on-show)))
     :render (fn [delta-time]
               (->> (fn [val]
                      (assoc val
                             :total-time (+ (:total-time val) delta-time)
                             :delta-time delta-time))
                    (swap! screen)
                    (execute-fn! on-render)))
     :hide #(execute-fn! on-hide @screen)
     :pause #(execute-fn! on-pause @screen)
     :resize #(execute-fn! on-resize @screen)
     :resume #(execute-fn! on-resume @screen)}))

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
  [^Game game & screens]
  (let [run-fn! (fn [key & args]
                  (doseq [screen screens]
                    (apply (get screen key) args)))]
    (.setScreen game (proxy [Screen] []
                       (show [] (run-fn! :show))
                       (render [delta-time] (run-fn! :render delta-time))
                       (hide [] (run-fn! :hide))
                       (pause [] (run-fn! :pause))
                       (resize [w h] (run-fn! :resize))
                       (resume [] (run-fn! :resume))))))

(defn create-renderer!
  [{:keys [create-renderer]} & {:keys [] :as args}]
  (:renderer (create-renderer args)))

(defn create-camera!
  [{:keys [create-camera]} & {:keys [] :as args}]
  (:camera (create-camera args)))
