(ns play-clj.core
  (:require [play-clj.utils :as u])
  (:import [com.badlogic.gdx Application Audio Files Game Gdx Graphics Input
            InputMultiplexer InputProcessor Net Screen]
           [com.badlogic.gdx.audio Sound]
           [com.badlogic.gdx.graphics Camera Color GL20 OrthographicCamera
            PerspectiveCamera]
           [com.badlogic.gdx.graphics.g2d NinePatch ParticleEffect SpriteBatch
            TextureRegion]
           [com.badlogic.gdx.input GestureDetector
            GestureDetector$GestureListener]
           [com.badlogic.gdx.maps MapLayer MapLayers]
           [com.badlogic.gdx.maps.tiled TiledMap TiledMapTileLayer
            TiledMapTileLayer$Cell TmxMapLoader]
           [com.badlogic.gdx.maps.tiled.renderers
            BatchTiledMapRenderer
            HexagonalTiledMapRenderer
            IsometricStaggeredTiledMapRenderer
            IsometricTiledMapRenderer
            OrthogonalTiledMapRenderer]
           [com.badlogic.gdx.physics.box2d ContactListener Joint World]
           [com.badlogic.gdx.scenes.scene2d Actor Stage]
           [com.badlogic.gdx.scenes.scene2d.utils ActorGestureListener Align
            ChangeListener ClickListener DragListener FocusListener]))

(load "core_global")
(load "core_graphics")
(load "core_listeners")

(defn ^:private reset-changed!
  "Internal use only"
  [e-atom e-old e-new]
  (when (not= e-old e-new)
    (compare-and-set! e-atom e-old e-new)))

(defn defscreen*
  "Internal use only"
  [{:keys [on-show on-render on-hide on-pause on-resize on-resume]
    :as options}]
  (let [screen (atom {})
        entities (atom '())
        execute-fn! (fn [func & {:keys [] :as options}]
                      (when func
                        (let [old-entities @entities]
                          (some->> (func (merge @screen options) old-entities)
                                   list
                                   flatten
                                   (remove nil?)
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
                    :update-fn! #(swap! screen merge %)
                    :ui-listeners (ui-listeners options execute-fn!)
                    :g2dp-listener (contact-listener options execute-fn!))
             (execute-fn! on-show))
     :render (fn [d]
               (swap! screen #(assoc % :total-time (+ (:total-time %) d)))
               (execute-fn! on-render :delta-time d))
     :hide #(execute-fn! on-hide)
     :pause #(execute-fn! on-pause)
     :resize #(execute-fn! on-resize :width %1 :height %2)
     :resume #(execute-fn! on-resume)
     :input-listeners (global-listeners options execute-fn!)}))

(defmacro defscreen
  "Creates vars for all the anonymous functions provided to it, so they can be
replaced by simply reloading the namespace, and creates a var for the symbol `n`
bound to a map containing various important values related to the screen"
  [n & {:keys [] :as options}]
  `(let [fns# (->> (for [[k# v#] ~options]
                     [k# (intern *ns* (symbol (str '~n "-" (name k#))) v#)])
                   flatten
                   (apply hash-map))]
     (defonce ~n (defscreen* fns#))))

(defn defgame*
  "Internal use only"
  [{:keys [on-create]}]
  (proxy [Game] []
    (create []
      (when on-create (on-create this)))))

(defmacro defgame
  "Creates a var for the symbol `n` bound to a [Game](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/Game.html)
object"
  [n & {:keys [] :as options}]
  `(defonce ~n (defgame* ~options)))

(defn set-screen!
  "Creates a [Screen](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/Screen.html)
object, sets it as the screen for the `game`, and runs the functions from
`screens` in the order they are provided in

    (set-screen! hello-world main-screen text-screen)"
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

(defn update!
  "Runs the equivalent of `(swap! screen-atom assoc ...)`, where `screen-atom`
is the atom storing the screen map behind the scenes, and returns the new screen
map

    (update! screen :renderer (stage))"
  [{:keys [update-fn!]} & {:keys [] :as args}]
  (update-fn! args))
