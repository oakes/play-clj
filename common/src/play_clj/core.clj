(ns play-clj.core
  (:import [com.badlogic.gdx Game Gdx Screen]
           [com.badlogic.gdx.graphics.g2d SpriteBatch]
           [com.badlogic.gdx.graphics
            Camera Color GL20 OrthographicCamera PerspectiveCamera]
           [com.badlogic.gdx.maps.tiled TmxMapLoader]
           [com.badlogic.gdx.maps.tiled.renderers
            BatchTiledMapRenderer
            HexagonalTiledMapRenderer
            IsometricStaggeredTiledMapRenderer
            IsometricTiledMapRenderer
            OrthogonalTiledMapRenderer]))

(load "render")

(defn clear!
  ([] (clear! 0 0 0 0))
  ([r g b a]
    (doto (Gdx/gl)
      (.glClearColor r g b a)
      (.glClear GL20/GL_COLOR_BUFFER_BIT))))

(defn defscreen*
  [{:keys [on-show on-render on-dispose on-hide on-pause on-resize on-resume
           renderer camera]
    :as options}]
  (let [screen (atom {})
        on-show (or on-show (fn [s]))
        on-render (or on-render (fn [s d]))
        on-dispose (or on-dispose (fn [s]))
        on-hide (or on-hide (fn [s]))
        on-pause (or on-pause (fn [s]))
        on-resize (or on-resize (fn [s w h]))
        on-resume (or on-resume (fn [s]))]
    (proxy [Screen] []
      (show []
        (swap! screen assoc
               :renderer (when renderer (renderer))
               :camera (create-camera camera)
               :total-time 0)
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

(defn set-screen!
  [^Game game ^Screen screen]
  (.setScreen game screen))

(defn defgameobj*
  [{:keys [start-screen]}]
  (proxy [Game] []
    (create [] (when start-screen (set-screen! this start-screen)))))

(defmacro defgameobj
  [name options]
  `(def ~name (defgameobj* ~options)))
