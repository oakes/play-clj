(ns play-clj.core
  (:require [clojure.set]
            [play-clj.entities :as e]
            [play-clj.math :as m]
            [play-clj.utils :as u])
  (:import [com.badlogic.gdx Application Audio Files Game Gdx Graphics Input
            InputMultiplexer InputProcessor Net Preferences Screen]
           [com.badlogic.gdx.audio Sound]
           [com.badlogic.gdx.assets AssetManager]
           [com.badlogic.gdx.assets.loaders AsynchronousAssetLoader
            ParticleEffectLoader]
           [com.badlogic.gdx.assets.loaders.resolvers
            InternalFileHandleResolver]
           [com.badlogic.gdx.files FileHandle]
           [com.badlogic.gdx.graphics Camera Color GL20 OrthographicCamera
            PerspectiveCamera Pixmap Pixmap$Format PixmapIO Texture
            VertexAttributes$Usage]
           [com.badlogic.gdx.graphics.g2d ParticleEffect SpriteBatch]
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
           [com.badlogic.gdx.math Vector2 Vector3]
           [com.badlogic.gdx.scenes.scene2d Actor Stage]
           [com.badlogic.gdx.scenes.scene2d.utils ActorGestureListener
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
  (when (and (not= e-old e-new)
             (compare-and-set! e-atom e-old e-new))
    e-new))

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
                          (some->> (with-meta
                                     #(normalize (func screen-map old-entities))
                                     (meta func))
                                   (wrapper screen)
                                   (reset-changed! entities old-entities)
                                   (update-screen! @screen)))))
        update-fn! (fn [func args]
                     (doto (apply swap! screen func args)
                       update-screen!))]
    {:screen screen
     :entities entities
     :options options
     :show (fn []
             (update-fn! assoc
                         [:total-time 0
                          :update-fn! update-fn!
                          :execute-fn! execute-fn!
                          :on-timer on-timer
                          :input-listeners (input-listeners options execute-fn!)
                          :ui-listeners (ui-listeners options execute-fn!)])
             (execute-fn! on-show)
             (->> (contact-listener @screen options execute-fn!)
                  (swap! screen assoc :contact-listener)))
     :render (fn [d]
               (swap! screen #(assoc % :total-time (+ (:total-time %) d)))
               (execute-fn! on-render :delta-time d))
     :hide #(execute-fn! on-hide)
     :pause #(execute-fn! on-pause)
     :resize (fn [w h]
               (execute-fn! on-resize :width w :height h)
               (update-screen! @screen))
     :resume #(execute-fn! on-resume)}))

(defmacro defscreen
  "Defines a screen, and creates vars for all the functions inside of it. All
functions take a screen map and entities vector as arguments, and return the
entities list at the end with any desired changes. If a function returns nil,
the entities list is not changed.

Below are all the possible screen functions. Some of them get special arguments
via the screen map.

    ; main screen functions
    (defscreen my-screen
      :on-show ; the screen first shows
      (fn [screen entities]
        entities)
      :on-render ; the screen must be rendered (many times per second)
      (fn [screen entities]
        (println (:delta-time screen)) ; time (ms) elapsed since last frame
        (println (:total-time screen)) ; time (ms) elapsed since :on-show
        entities)
      :on-hide ; the screen was replaced
      (fn [screen entities]
        entities)
      :on-resize ; the screen was resized
      (fn [screen entities]
        (println (:width screen)) ; the new width of the screen
        (println (:height screen)) ; the new height of the screen
        entities)
      :on-resume ; the screen resumed from a paused state (mobile only)
      (fn [screen entities]
        entities)
      :on-pause ; the screen paused (mobile only)
      (fn [screen entities]
        entities)
      :on-timer ; a timer created with add-timer! executed
      (fn [screen entities]
        (println (:id screen)) ; the id supplied when the timer was created
        entities))

    ; input functions
    (defscreen my-screen
      :on-key-down ; a key was pressed
      (fn [screen entities]
        (println (:key screen)) ; the key that was pressed (see key-code)
        entities)
      :on-key-typed ; a key was typed
      (fn [screen entities]
        (println (:character screen)) ; the character that was pressed
        entities)
      :on-key-up ; a key was released
      (fn [screen entities]
        (println (:key screen)) ; the key that was released (see key-code)
        entities)
      :on-mouse-moved ; the mouse was moved without pressing any buttons
      (fn [screen entities]
        (println (:input-x screen)) ; the x position of the mouse
        (println (:input-y screen)) ; the y position of the mouse
        entities)
      :on-scrolled ; the mouse wheel was scrolled
      (fn [screen entities]
        (println (:amount screen)) ; the amount scrolled
        entities)
      :on-touch-down ; the screen was touched or a mouse button was pressed
      (fn [screen entities]
        (println (:input-x screen)) ; the x position of the finger/mouse
        (println (:input-y screen)) ; the y position of the finger/mouse
        (println (:pointer screen)) ; the pointer for the event
        (println (:button screen)) ; the mouse button that was pressed (see button-code)
        entities)
      :on-touch-dragged ; a finger or the mouse was dragged
      (fn [screen entities]
        (println (:input-x screen)) ; the x position of the finger/mouse
        (println (:input-y screen)) ; the y position of the finger/mouse
        (println (:pointer screen)) ; the pointer for the event
        entities)
      :on-touch-up ; a finger was lifted or a mouse button was released
      (fn [screen entities]
        (println (:input-x screen)) ; the x position of the finger/mouse
        (println (:input-y screen)) ; the y position of the finger/mouse
        (println (:pointer screen)) ; the pointer for the event
        (println (:button screen)) ; the mouse button that was released (see button-code)
        entities))

    ; gesture functions
    ; Tip: use gesture-detector! to configure these functions
    (defscreen my-screen
      :on-fling ; the user dragged over the screen and lifted
      (fn [screen entities]
        (println (:velocity-x screen)) ; the x-axis velocity (s)
        (println (:velocity-y screen)) ; the y-axis velocity (s)
        (println (:button screen)) ; the mouse button that was pressed (see button-code)
        entities)
      :on-long-press ; the user pressed for a long time
      (fn [screen entities]
        (println (:input-x screen)) ; the x position of the finger/mouse
        (println (:input-y screen)) ; the y position of the finger/mouse
        entities)
      :on-pan ; the user dragged a finger over the screen
      (fn [screen entities]
        (println (:input-x screen)) ; the x position of the finger/mouse
        (println (:input-y screen)) ; the y position of the finger/mouse
        (println (:delta-x screen)) ; the x-axis distance moved
        (println (:delta-y screen)) ; the y-axis distance moved
        entities)
      :on-pan-stop ; the user is no longer panning
      (fn [screen entities]
        (println (:input-x screen)) ; the x position of the finger/mouse
        (println (:input-y screen)) ; the y position of the finger/mouse
        (println (:pointer screen)) ; the pointer for the event
        (println (:button screen)) ; the mouse button that was pressed (see button-code)
        entities)
      :on-pinch ; the user performed a pinch zoom gesture
      (fn [screen entities]
        (println (:initial-pointer-1 screen)) ; the start position of finger 1 (see the x and y functions)
        (println (:initial-pointer-2 screen)) ; the start position of finger 2 (see the x and y functions)
        (println (:pointer-1 screen)) ; the end position of finger 1 (see the x and y functions)
        (println (:pointer-2 screen)) ; the end position of finger 2 (see the x and y functions)
        entities)
      :on-tap ; the user tapped
      (fn [screen entities]
        (println (:input-x screen)) ; the x position of the finger/mouse
        (println (:input-y screen)) ; the y position of the finger/mouse
        (println (:count screen)) ; the number of taps
        (println (:button screen)) ; the mouse button that was pressed (see button-code)
        entities)
      :on-zoom ; the user performed a pinch zoom gesture
      (fn [screen entities]
        (println (:initial-distance screen)) ; the start distance between fingers
        (println (:distance screen)) ; the end distance between fingers
        entities))

    ; 2D physics contact (for play-clj.g2d-physics)
    ; Tip: use first-entity and second-entity to get the entities that are contacting
    (defscreen my-screen
      :on-begin-contact ; two bodies began to touch
      (fn [screen entities]
        (println (:contact screen)) ; the Contact - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/Contact.html
        entities)
      :on-end-contact ; two bodies ceased to touch
      (fn [screen entities]
        (println (:contact screen)) ; the Contact - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/Contact.html
        entities))

    ; 3D physics contact (for play-clj.g3d-physics)
    ; Tip: use first-entity and second-entity to get the entities that are contacting
    (defscreen my-screen
      :on-begin-contact ; two bodies began to touch
      (fn [screen entities]
        (println (:first-body screen)) ; the first btCollisionObject - http://bulletphysics.org/Bullet/BulletFull/classbtCollisionObject.html
        (println (:second-body screen)) ; the second btCollisionObject - http://bulletphysics.org/Bullet/BulletFull/classbtCollisionObject.html
        entities)
      :on-end-contact ; two bodies ceased to touch
      (fn [screen entities]
        (println (:first-body screen)) ; the first btCollisionObject - http://bulletphysics.org/Bullet/BulletFull/classbtCollisionObject.html
        (println (:second-body screen)) ; the second btCollisionObject - http://bulletphysics.org/Bullet/BulletFull/classbtCollisionObject.html
        entities))

    ; ui input functions (for play-clj.ui)
    ; Tip: use click-listener! to configure these functions
    (defscreen my-screen
      :on-ui-changed ; the ui entity was changed
      (fn [screen entities]
        (println (:event screen)) ; the ChangeListener.ChangeEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/utils/ChangeListener.ChangeEvent.html
        (println (:actor screen)) ; the Actor - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/Actor.html
        entities)
      :on-ui-clicked ; the ui entity was clicked
      (fn [screen entities]
        (println (:event screen)) ; the InputEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/InputEvent.html
        (println (:input-x screen)) ; the x position of the finger/mouse
        (println (:input-y screen)) ; the y position of the finger/mouse
        entities)
      :on-ui-enter ; the finger/mouse moved over the ui entity
      (fn [screen entities]
        (println (:event screen)) ; the InputEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/InputEvent.html
        (println (:actor screen)) ; the Actor - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/Actor.html
        (println (:input-x screen)) ; the x position of the finger/mouse
        (println (:input-y screen)) ; the y position of the finger/mouse
        (println (:pointer screen)) ; the pointer for the event
        entities)
      :on-ui-exit ; the finger/mouse moved out of the ui entity
      (fn [screen entities]
        (println (:event screen)) ; the InputEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/InputEvent.html
        (println (:actor screen)) ; the Actor - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/Actor.html
        (println (:input-x screen)) ; the x position of the finger/mouse
        (println (:input-y screen)) ; the y position of the finger/mouse
        (println (:pointer screen)) ; the pointer for the event
        entities)
      :on-ui-touch-down ; the finger/mouse went down on the ui entity
      (fn [screen entities]
        (println (:event screen)) ; the InputEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/InputEvent.html
        (println (:input-x screen)) ; the x position of the finger/mouse
        (println (:input-y screen)) ; the y position of the finger/mouse
        (println (:pointer screen)) ; the pointer for the event
        (println (:button screen)) ; the mouse button that was pressed (see button-code)
        entities)
      :on-ui-touch-dragged ; the finger/mouse moved anywhere
      (fn [screen entities]
        (println (:event screen)) ; the InputEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/InputEvent.html
        (println (:input-x screen)) ; the x position of the finger/mouse
        (println (:input-y screen)) ; the y position of the finger/mouse
        (println (:pointer screen)) ; the pointer for the event
        entities)
      :on-ui-touch-up ; the finger/mouse went up anywhere
      (fn [screen entities]
        (println (:event screen)) ; the InputEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/InputEvent.html
        (println (:input-x screen)) ; the x position of the finger/mouse
        (println (:input-y screen)) ; the y position of the finger/mouse
        (println (:pointer screen)) ; the pointer for the event
        (println (:button screen)) ; the mouse button that was released (see button-code)
        entities))

    ; ui drag functions (for play-clj.ui)
    ; Tip: use drag-listener! to configure these functions
    (defscreen my-screen
      :on-ui-drag
      (fn [screen entities]
        (println (:event screen)) ; the InputEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/InputEvent.html
        (println (:input-x screen)) ; the x position of the finger/mouse
        (println (:input-y screen)) ; the y position of the finger/mouse
        (println (:pointer screen)) ; the pointer for the event
        entities)
      :on-ui-drag-start
      (fn [screen entities]
        (println (:event screen)) ; the InputEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/InputEvent.html
        (println (:input-x screen)) ; the x position of the finger/mouse
        (println (:input-y screen)) ; the y position of the finger/mouse
        (println (:pointer screen)) ; the pointer for the event
        entities)
      :on-ui-drag-stop
      (fn [screen entities]
        (println (:event screen)) ; the InputEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/InputEvent.html
        (println (:input-x screen)) ; the x position of the finger/mouse
        (println (:input-y screen)) ; the y position of the finger/mouse
        (println (:pointer screen)) ; the pointer for the event
        entities))

    ; ui focus functions (for play-clj.ui)
    (defscreen my-screen
      :on-ui-keyboard-focus-changed
      (fn [screen entities]
        (println (:event screen)) ; the FocusListener.FocusEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/utils/FocusListener.FocusEvent.html
        (println (:actor screen)) ; the Actor - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/Actor.html
        (println (:focused? screen)) ; whether it is focused
        entities)
      :on-ui-scroll-focus-changed
      (fn [screen entities]
        (println (:event screen)) ; the FocusListener.FocusEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/utils/FocusListener.FocusEvent.html
        (println (:actor screen)) ; the Actor - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/Actor.html
        (println (:focused? screen)) ; whether it is focused
        entities))

    ; ui gesture functions (for play-clj.ui)
    (defscreen my-screen
      :on-ui-fling ; the user dragged a finger over the screen and lifted it
      (fn [screen entities]
        (println (:event screen)) ; the InputEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/InputEvent.html
        (println (:velocity-x screen)) ; the x-axis velocity (s)
        (println (:velocity-y screen)) ; the y-axis velocity (s)
        (println (:button screen)) ; the mouse button that was pressed (see button-code)
        entities)
      :on-ui-long-press ; the user pressed
      (fn [screen entities]
        (println (:actor screen)) ; the Actor - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/Actor.html
        (println (:input-x screen)) ; the x position of the finger
        (println (:input-y screen)) ; the y position of the finger
        entities)
      :on-ui-pan ; the user dragged a finger over the screen
      (fn [screen entities]
        (println (:event screen)) ; the InputEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/InputEvent.html
        (println (:input-x screen)) ; the x position of the finger
        (println (:input-y screen)) ; the y position of the finger
        (println (:delta-x screen)) ; the x-axis distance moved
        (println (:delta-y screen)) ; the y-axis distance moved
        entities)
      :on-ui-pan-stop ; the user is no longer panning
      (fn [screen entities]
        (println (:event screen)) ; the InputEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/InputEvent.html
        (println (:input-x screen)) ; the x position of the finger
        (println (:input-y screen)) ; the y position of the finger
        (println (:pointer screen)) ; the pointer for the event
        (println (:button screen)) ; the mouse button that was pressed (see button-code)
        entities)
      :on-ui-pinch ; the user performed a pinch zoom gesture
      (fn [screen entities]
        (println (:event screen)) ; the InputEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/InputEvent.html
        (println (:initial-pointer-1 screen)) ; the start position of finger 1 (see the x and y functions)
        (println (:initial-pointer-2 screen)) ; the start position of finger 2 (see the x and y functions)
        (println (:pointer-1 screen)) ; the end position of finger 1 (see the x and y functions)
        (println (:pointer-2 screen)) ; the end position of finger 2 (see the x and y functions)
        entities)
      :on-ui-tap ; the user tapped
      (fn [screen entities]
        (println (:event screen)) ; the InputEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/InputEvent.html
        (println (:input-x screen)) ; the x position of the finger
        (println (:input-y screen)) ; the y position of the finger
        (println (:count screen)) ; the number of taps
        (println (:button screen)) ; the mouse button that was pressed (see button-code)
        entities)
      :on-ui-zoom ; the user performed a pinch zoom gesture
      (fn [screen entities]
        (println (:event screen)) ; the InputEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/InputEvent.html
        (println (:initial-distance screen)) ; the start distance between fingers
        (println (:distance screen)) ; the end distance between fingers
        entities))"
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
  "Creates and displays a screen for the `game-object`, using one or more
`screen-objects` in the order they were provided.

    (set-screen! my-game main-screen text-screen)"
  [^Game game-object & screen-objects]
  (let [run-fn! (fn [k & args]
                  (doseq [screen screen-objects]
                    (apply (get screen k) args)))]
    (.setScreen game-object
      (reify Screen
        (show [this]
          (input! :set-input-processor (InputMultiplexer.))
          (run-fn! :show)
          (doseq [{:keys [screen]} screen-objects]
            (doseq [[_ listener] (:input-listeners @screen)]
              (add-input! listener))))
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
    (set-screen-wrapper! (fn [screen-atom screen-fn]
                           (screen-fn)))
    ; if there is an error, print it out and switch to a blank screen
    ; (this is useful because it makes error recovery easier in a REPL)
    (set-screen-wrapper! (fn [screen-atom screen-fn]
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

(defn run!
  "Runs a function defined in another screen. You may optionally pass a series
of key-value pairs, which will be given to the function via its screen map.

    (run! my-other-screen :on-show)
    (run! my-other-screen :on-change-color :color :blue)"
  [screen-object fn-name & options]
  (let [execute-fn! (-> screen-object :screen deref :execute-fn!)
        screen-fn (-> screen-object :options (get fn-name))]
    (apply execute-fn! screen-fn options)
    nil))
