(ns play-clj.core
  (:require [clojure.set :as set]
            [play-clj.utils :as utils])
  (:import [com.badlogic.gdx Application Audio Files Game Gdx Graphics Input
            InputMultiplexer InputProcessor Net Screen]
           [com.badlogic.gdx.graphics Camera Color GL20 OrthographicCamera
            PerspectiveCamera Texture]
           [com.badlogic.gdx.graphics.g2d Animation BitmapFont SpriteBatch
            TextureRegion]
           [com.badlogic.gdx.input GestureDetector
            GestureDetector$GestureListener]
           [com.badlogic.gdx.maps MapLayer MapLayers]
           [com.badlogic.gdx.maps.tiled TiledMap TiledMapTileLayer TmxMapLoader]
           [com.badlogic.gdx.maps.tiled.renderers
            BatchTiledMapRenderer
            HexagonalTiledMapRenderer
            IsometricStaggeredTiledMapRenderer
            IsometricTiledMapRenderer
            OrthogonalTiledMapRenderer]
           [com.badlogic.gdx.scenes.scene2d Actor Stage]
           [com.badlogic.gdx.scenes.scene2d.ui ButtonGroup CheckBox Dialog
            ImageButton ImageTextButton Label Skin Slider TextButton TextField]
           [com.badlogic.gdx.scenes.scene2d.utils ActorGestureListener
            ChangeListener ClickListener DragListener FocusListener
            NinePatchDrawable SpriteDrawable TextureRegionDrawable TiledDrawable]))

(defmulti create-entity class)

(defmethod create-entity TextureRegion
  [obj]
  {:type :image :object obj})

(defmethod create-entity Actor
  [obj]
  {:type :actor :object obj})

(load "core_2d")
(load "core_deprecated")
(load "core_global")
(load "core_interop")
(load "core_render")
(load "core_ui")

(defn defscreen*
  [{:keys [on-show on-render on-hide on-pause on-resize on-resume]
    :as options}]
  (let [screen (atom {})
        entities (atom '())
        execute-fn! (fn [func & {:keys [] :as options}]
                      (when func
                        (let [entities-list @entities]
                          (some->> (func (merge @screen options) entities-list)
                                   list
                                   flatten
                                   (remove nil?)
                                   (compare-and-set! entities entities-list)))))
        ui-listeners [(ui-gesture-listener options execute-fn!)
                      (ui-change-listener options execute-fn!)
                      (ui-click-listener options execute-fn!)
                      (ui-drag-listener options execute-fn!)
                      (ui-focus-listener options execute-fn!)]
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
     :input-processor (input-processor options execute-fn!)
     :gesture-detector (gesture-detector options execute-fn!)}))

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
                      (input! :setInputProcessor (InputMultiplexer.))
                      (doseq [screen screens]
                        (add-input! (:input-processor screen))
                        (add-input! (:gesture-detector screen))))
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
