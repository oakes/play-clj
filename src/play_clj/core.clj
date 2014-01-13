(ns play-clj.core
  (:require [clojure.set :as set]
            [play-clj.ui :as ui]
            [play-clj.utils :as u])
  (:import [com.badlogic.gdx Application Audio Files Game Gdx Graphics Input
            InputMultiplexer InputProcessor Net Screen]
           [com.badlogic.gdx.graphics Camera Color GL20 OrthographicCamera
            PerspectiveCamera Texture]
           [com.badlogic.gdx.graphics.g2d Animation SpriteBatch TextureRegion]
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
           [com.badlogic.gdx.scenes.scene2d Actor Stage]))

(load "core_2d")
(load "core_deprecated")
(load "core_global")
(load "core_render")

(defn ^:private reset-if-changed!
  [e-atom e-old e-new]
  (when (not= e-old e-new)
    (compare-and-set! e-atom e-old e-new)))

(defn defscreen*
  [{:keys [on-show on-render on-hide on-pause on-resize on-resume]
    :as options}]
  (let [screen (atom {})
        entities (atom '())
        _ (add-watch entities
                     :changed
                     (fn [_ _ _ new-entities]
                       (refresh-renderer! @screen new-entities)))
        execute-fn! (fn [func & {:keys [] :as options}]
                      (when func
                        (let [old-entities @entities]
                          (some->> (func (merge @screen options) old-entities)
                                   list
                                   flatten
                                   (remove nil?)
                                   (reset-if-changed! entities old-entities)))))
        listeners [(input-processor options execute-fn!)
                   (gesture-detector options execute-fn!)]
        ui-listeners (ui/create-listeners options execute-fn!)
        create-renderer-fn! #(swap! screen assoc :renderer (renderer %))
        update-fn! #(swap! screen merge %)]
    {:screen screen
     :entities entities
     :show (fn []
             (swap! screen assoc
                    :total-time 0
                    :create-renderer-fn! create-renderer-fn!
                    :update-fn! update-fn!
                    :ui-listeners ui-listeners)
             (execute-fn! on-show))
     :render (fn [d]
               (swap! screen #(assoc % :total-time (+ (:total-time %) d)))
               (execute-fn! on-render :delta-time d))
     :hide #(execute-fn! on-hide)
     :pause #(execute-fn! on-pause)
     :resize #(execute-fn! on-resize :width %1 :height %2)
     :resume #(execute-fn! on-resume)
     :listeners listeners}))

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
                      (doseq [{:keys [listeners]} screens]
                        (doseq [listener listeners]
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
