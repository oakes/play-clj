(ns play-clj.core
  (:require [clojure.set]
            [play-clj.entities :as e]
            [play-clj.math :as m]
            [play-clj.utils :as u])
  (:import [com.badlogic.gdx Application Audio Files Game Gdx Graphics Input
            InputMultiplexer InputProcessor Net Screen]
           [com.badlogic.gdx.audio Sound]
           [com.badlogic.gdx.assets AssetManager]
           [com.badlogic.gdx.assets.loaders AsynchronousAssetLoader]
           [com.badlogic.gdx.graphics Camera Color GL20 OrthographicCamera
            PerspectiveCamera Pixmap Pixmap$Format PixmapIO Texture
            VertexAttributes$Usage]
           [com.badlogic.gdx.graphics.g2d SpriteBatch]
           [com.badlogic.gdx.graphics.g3d ModelBatch]
           [com.badlogic.gdx.graphics.glutils ShapeRenderer]
           [com.badlogic.gdx.input GestureDetector
            GestureDetector$GestureListener]
           [com.badlogic.gdx.maps MapLayer MapLayers MapObject MapObjects
            MapProperties]
           [com.badlogic.gdx.maps.tiled TiledMap TiledMapTileLayer
            TiledMapTileLayer$Cell TmxMapLoader]
           [com.badlogic.gdx.maps.tiled.renderers
            BatchTiledMapRenderer
            HexagonalTiledMapRenderer
            IsometricStaggeredTiledMapRenderer
            IsometricTiledMapRenderer
            OrthogonalTiledMapRenderer]
           [com.badlogic.gdx.scenes.scene2d Actor Stage]
           [com.badlogic.gdx.scenes.scene2d.utils ActorGestureListener Align
            ChangeListener ClickListener DragListener FocusListener]
           [com.badlogic.gdx.utils ScreenUtils Timer$Task]
           [play_clj.entities BundleEntity ShapeEntity]))

(load "core_basics")
(load "core_cameras")
(load "core_graphics")
(load "core_listeners")
(load "core_utils")

(defn ^:private reset-changed!
  [e-atom e-old e-new]
  (when (not= e-old e-new)
    (compare-and-set! e-atom e-old e-new)))

(defn ^:private normalize
  [entities]
  (some->> entities
           list
           flatten
           (remove nil?)
           vec))

(defn ^:private wrapper
  [screen screen-fn]
  (screen-fn))

(defn defscreen*
  [{:keys [screen entities
           on-show on-render on-hide on-pause on-resize on-resume on-timer]
    :as options}]
  (let [execute-fn! (fn [func & {:keys [] :as options}]
                      (when func
                        (let [screen-map (merge @screen options)
                              old-entities @entities]
                          (some->> (fn []
                                     (normalize (func screen-map old-entities)))
                                   (wrapper screen)
                                   (reset-changed! entities old-entities)))))]
    ; update screen when either the screen or entities are changed
    (add-watch screen :changed (fn [_ _ _ new-screen]
                                 (update-screen! new-screen)))
    (add-watch entities :changed (fn [_ _ _ new-entities]
                                   (update-screen! @screen new-entities)))
    ; return a map with all values related to the screen
    {:screen screen
     :entities entities
     :show (fn []
             (swap! screen assoc
                    :total-time 0
                    :update-fn! #(apply swap! screen %1 %2)
                    :execute-fn! execute-fn!
                    :on-timer on-timer
                    :ui-listeners (ui-listeners options execute-fn!))
             (execute-fn! on-show)
             (when-not (:contact-listener @screen)
               (->> (contact-listener @screen options execute-fn!)
                    (swap! screen assoc :contact-listener))))
     :render (fn [d]
               (swap! screen #(assoc % :total-time (+ (:total-time %) d)))
               (execute-fn! on-render :delta-time d))
     :hide #(execute-fn! on-hide)
     :pause #(execute-fn! on-pause)
     :resize #(execute-fn! on-resize :width %1 :height %2)
     :resume #(execute-fn! on-resume)
     :input-listeners (global-listeners options execute-fn!)}))

(defmacro defscreen
  "Defines a screen, and creates vars for all the functions inside of it."
  [n & {:keys [] :as options}]
  `(let [fn-syms# (->> (for [[k# v#] ~options]
                         [k# (intern *ns* (symbol (str '~n "-" (name k#))) v#)])
                       flatten
                       (apply hash-map))
         map-sym# (symbol (str '~n "-map"))
         entities-sym# (symbol (str '~n "-entities"))
         syms# (assoc fn-syms#
                      :screen (deref
                                (or (resolve map-sym#)
                                    (intern *ns* map-sym# (atom {}))))
                      :entities (deref
                                  (or (resolve entities-sym#)
                                      (intern *ns* entities-sym# (atom [])))))]
     (def ~n (defscreen* syms#))))

(defn defgame*
  [{:keys [on-create]}]
  (proxy [Game] []
    (create []
      (when on-create
        (on-create this)))))

(defmacro defgame
  "Defines a game. This should only be called once."
  [n & {:keys [] :as options}]
  `(defonce ~n (defgame* ~options)))

(defn set-screen!
  "Creates and displays a screen for the `game` object, using one or more
`screen` maps in the order they were provided.

    (set-screen! my-game main-screen text-screen)"
  [^Game game & screens]
  (let [add-inputs! (fn []
                      (input! :set-input-processor (InputMultiplexer.))
                      (doseq [{:keys [input-listeners]} screens]
                        (doseq [listener input-listeners]
                          (add-input! listener))))
        run-fn! (fn [k & args]
                  (doseq [screen screens]
                    (apply (get screen k) args)))]
    (.setScreen game (reify Screen
                       (show [this] (add-inputs!) (run-fn! :show))
                       (render [this d] (run-fn! :render d))
                       (hide [this] (run-fn! :hide))
                       (pause [this] (run-fn! :pause))
                       (resize [this w h] (run-fn! :resize w h))
                       (resume [this] (run-fn! :resume))
                       (dispose [this])))))

(defn set-screen-wrapper!
  "Sets a function that wraps around all screen functions, allowing you to
handle errors and perform other custom actions each time they run.

    ; default behavior
    (set-screen-wrapper! (fn [screen screen-fn]
                           (screen-fn)))
    ; if there is an error, print it out and switch to a blank screen
    ; (this is useful because it makes error recovery easier in a REPL)
    (set-screen-wrapper! (fn [screen screen-fn]
                           (try (screen-fn)
                             (catch Exception e
                               (.printStackTrace e)
                               (set-screen! my-game blank-screen)))))"
  [wrapper-fn]
  (intern 'play-clj.core 'wrapper wrapper-fn))

(defn update!
  "Runs the equivalent of `(swap! screen-atom assoc ...)`, where `screen-atom`
is the atom storing the screen map behind the scenes. Returns the updated
`screen` map.

    (update! screen :renderer (stage))"
  [{:keys [update-fn!] :as screen} & args]
  (update-fn! assoc args))
