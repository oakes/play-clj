(ns play-clj.core
  (:require [clojure.set :as set]
            [play-clj.g2d-physics :as g2dp]
            [play-clj.ui :as ui]
            [play-clj.utils :as u])
  (:import [com.badlogic.gdx Application Audio Files Game Gdx Graphics Input
            InputMultiplexer InputProcessor Net Screen]
           [com.badlogic.gdx.graphics Camera Color GL20 OrthographicCamera
            PerspectiveCamera]
           [com.badlogic.gdx.graphics.g2d SpriteBatch TextureRegion]
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
           [com.badlogic.gdx.physics.box2d World]
           [com.badlogic.gdx.scenes.scene2d Actor Stage]))

(load "core_global")
(load "core_graphics")

(defn ^:private reset-changed!
  [e-atom e-old e-new]
  (when (not= e-old e-new)
    (compare-and-set! e-atom e-old e-new)))

(defn defscreen*
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
                    :ui-listeners (ui/ui-listeners options execute-fn!)
                    :g2dp-listener (g2dp/contact-listener options execute-fn!))
             (execute-fn! on-show))
     :render (fn [d]
               (swap! screen #(assoc % :total-time (+ (:total-time %) d)))
               (execute-fn! on-render :delta-time d))
     :hide #(execute-fn! on-hide)
     :pause #(execute-fn! on-pause)
     :resize #(execute-fn! on-resize :width %1 :height %2)
     :resume #(execute-fn! on-resume)
     :input-listeners [(input-processor options execute-fn!)
                       (gesture-detector options execute-fn!)]}))

(defmacro defscreen
  [n & {:keys [] :as options}]
  `(let [fns# (->> (for [[k# v#] ~options]
                     [k# (intern *ns* (symbol (str '~n "-" (name k#))) v#)])
                   flatten
                   (apply hash-map))]
     (defonce ~n (defscreen* fns#))))

(defn defgame*
  [{:keys [on-create]}]
  (proxy [Game] []
    (create []
      (when on-create (on-create this)))))

(defmacro defgame
  [n & {:keys [] :as options}]
  `(defonce ~n (defgame* ~options)))

(defn set-screen!
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
  [{:keys [update-fn!]} & {:keys [] :as args}]
  (update-fn! args))
