(ns play-clj.core
  (:require [clojure.set]
            [play-clj.entities :as e]
            [play-clj.math :as m]
            [play-clj.utils :as u])
  (:import [com.badlogic.gdx Application Audio Files Game Gdx Graphics Input
            InputMultiplexer InputProcessor Net Preferences Screen]
           [com.badlogic.gdx.audio Sound Music]
           [com.badlogic.gdx.assets AssetManager]
           [com.badlogic.gdx.assets.loaders AsynchronousAssetLoader
            ParticleEffectLoader]
           [com.badlogic.gdx.assets.loaders.resolvers
            InternalFileHandleResolver]
           [com.badlogic.gdx.files FileHandle]
           [com.badlogic.gdx.graphics Camera Color GL20 OrthographicCamera
            PerspectiveCamera Pixmap Pixmap$Format PixmapIO Texture
            VertexAttributes$Usage]
           [com.badlogic.gdx.graphics.g2d Batch ParticleEffect]
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

(defn ^:private normalize
  [entities]
  (some->> entities
           list
           flatten
           (remove nil?)
           vec))

(defn ^:private wrapper
  [screen-atom screen-fn]
  (screen-fn))

(defn ^:private reset-changed!
  [e-atom e-old e-new]
  (when (and (not= e-old e-new)
             (compare-and-set! e-atom e-old e-new))
    e-new))

(defn ^:private add-to-timeline!
  [screen-atom entities]
  (let [screen @screen-atom]
    (when (:timeline screen)
      (swap! screen-atom
             update-in
             [:timeline]
             conj
             [(:total-time screen) entities]))))

(defn defscreen*
  [screen entities
   {:keys [on-show on-render on-hide on-pause on-resize on-resume on-timer]
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
        execute-fn-on-gl! (fn [& args]
                            (on-gl (apply execute-fn! args)))
        update-fn! (fn [func & args]
                     (apply swap! screen func args))]
    {:screen screen
     :entities entities
     :execute-fn! execute-fn!
     :execute-fn-on-gl! execute-fn-on-gl!
     :update-fn! update-fn!
     :options options
     :show (fn []
             ; if using a physics engine in a REPL, we need to forcibly dispose
             ; the world so it is cleaned up properly
             (some-> @screen :world :object .dispose)
             ; set the initial values in the screen map
             (update-fn! assoc
                         :execute-fn! execute-fn!
                         :execute-fn-on-gl! execute-fn-on-gl!
                         :update-fn! update-fn!
                         :options options
                         :on-timer on-timer
                         :layers nil
                         :input-listeners (input-listeners options execute-fn!)
                         :ui-listeners (ui-listeners options execute-fn!))
             ; run :on-show
             (execute-fn! on-show)
             ; update the physics contact listener if a :world was created
             (some->> (contact-listener @screen options execute-fn!)
                      (update-fn! assoc :contact-listener)
                      update-screen!))
     :render (fn [d]
               (swap! screen update-in [:total-time] #(+ (or %1 0) %2) d)
               (some->> (execute-fn! on-render :delta-time d)
                        (add-to-timeline! screen)))
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
      ; the screen first shows
      :on-show
      (fn [screen entities]
        entities)
      ; the screen must be rendered (many times per second)
      :on-render
      (fn [screen entities]
        (println (:delta-time screen)) ; time (ms) elapsed since last frame
        (println (:total-time screen)) ; time (ms) elapsed since first :on-show
        entities)
      ; the screen was replaced
      :on-hide
      (fn [screen entities]
        entities)
      ; the screen was resized
      :on-resize
      (fn [screen entities]
        (println (:width screen)) ; the new width of the screen
        (println (:height screen)) ; the new height of the screen
        entities)
      ; the screen resumed from a paused state
      :on-resume
      (fn [screen entities]
        entities)
      ; the screen paused
      :on-pause
      (fn [screen entities]
        entities)
      ; a timer created with add-timer! executed
      :on-timer
      (fn [screen entities]
        (println (:id screen)) ; the id supplied when the timer was created
        entities))

    ; input functions
    ; Tip: convert :input-x and :input-y to screen coordinates with input->screen
    (defscreen my-screen
      ; a key was pressed
      :on-key-down
      (fn [screen entities]
        (println (:key screen)) ; the key that was pressed (see key-code)
        entities)
      ; a key was typed
      :on-key-typed
      (fn [screen entities]
        (println (:character screen)) ; the character that was pressed
        entities)
      ; a key was released
      :on-key-up
      (fn [screen entities]
        (println (:key screen)) ; the key that was released (see key-code)
        entities)
      ; the mouse was moved without pressing any buttons
      :on-mouse-moved
      (fn [screen entities]
        (println (:input-x screen)) ; the x position of the mouse
        (println (:input-y screen)) ; the y position of the mouse
        entities)
      ; the mouse wheel was scrolled
      :on-scrolled
      (fn [screen entities]
        (println (:amount screen)) ; the amount scrolled
        entities)
      ; the screen was touched or a mouse button was pressed
      :on-touch-down
      (fn [screen entities]
        (println (:input-x screen)) ; the x position of the finger/mouse
        (println (:input-y screen)) ; the y position of the finger/mouse
        (println (:pointer screen)) ; the pointer for the event
        (println (:button screen)) ; the mouse button that was pressed (see button-code)
        entities)
      ; a finger or the mouse was dragged
      :on-touch-dragged
      (fn [screen entities]
        (println (:input-x screen)) ; the x position of the finger/mouse
        (println (:input-y screen)) ; the y position of the finger/mouse
        (println (:pointer screen)) ; the pointer for the event
        entities)
      ; a finger was lifted or a mouse button was released
      :on-touch-up
      (fn [screen entities]
        (println (:input-x screen)) ; the x position of the finger/mouse
        (println (:input-y screen)) ; the y position of the finger/mouse
        (println (:pointer screen)) ; the pointer for the event
        (println (:button screen)) ; the mouse button that was released (see button-code)
        entities))

    ; gesture functions
    ; Tip: use gesture-detector! to configure these functions
    (defscreen my-screen
      ; the user dragged over the screen and lifted
      :on-fling
      (fn [screen entities]
        (println (:velocity-x screen)) ; the x-axis velocity (s)
        (println (:velocity-y screen)) ; the y-axis velocity (s)
        (println (:button screen)) ; the mouse button that was pressed (see button-code)
        entities)
      ; the user pressed for a long time
      :on-long-press
      (fn [screen entities]
        (println (:input-x screen)) ; the x position of the finger/mouse
        (println (:input-y screen)) ; the y position of the finger/mouse
        entities)
      ; the user dragged a finger over the screen
      :on-pan
      (fn [screen entities]
        (println (:input-x screen)) ; the x position of the finger/mouse
        (println (:input-y screen)) ; the y position of the finger/mouse
        (println (:delta-x screen)) ; the x-axis distance moved
        (println (:delta-y screen)) ; the y-axis distance moved
        entities)
      ; the user is no longer panning
      :on-pan-stop
      (fn [screen entities]
        (println (:input-x screen)) ; the x position of the finger/mouse
        (println (:input-y screen)) ; the y position of the finger/mouse
        (println (:pointer screen)) ; the pointer for the event
        (println (:button screen)) ; the mouse button that was pressed (see button-code)
        entities)
      ; the user performed a pinch zoom gesture
      :on-pinch
      (fn [screen entities]
        (println (:initial-pointer-1 screen)) ; the start position of finger 1 (see the x and y functions)
        (println (:initial-pointer-2 screen)) ; the start position of finger 2 (see the x and y functions)
        (println (:pointer-1 screen)) ; the end position of finger 1 (see the x and y functions)
        (println (:pointer-2 screen)) ; the end position of finger 2 (see the x and y functions)
        entities)
      ; the user tapped
      :on-tap
      (fn [screen entities]
        (println (:input-x screen)) ; the x position of the finger/mouse
        (println (:input-y screen)) ; the y position of the finger/mouse
        (println (:count screen)) ; the number of taps
        (println (:button screen)) ; the mouse button that was pressed (see button-code)
        entities)
      ; the user performed a pinch zoom gesture
      :on-zoom
      (fn [screen entities]
        (println (:initial-distance screen)) ; the start distance between fingers
        (println (:distance screen)) ; the end distance between fingers
        entities))

    ; 2D physics contact (for play-clj.g2d-physics)
    ; Tip: use first-entity and second-entity to get the entities that are contacting
    (defscreen my-screen
      ; two bodies began to touch
      :on-begin-contact
      (fn [screen entities]
        (println (:contact screen)) ; the Contact - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/Contact.html
        entities)
      ; two bodies ceased to touch
      :on-end-contact
      (fn [screen entities]
        (println (:contact screen)) ; the Contact - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/Contact.html
        entities)
      ; called between each use of `step!` before the collision is processed
      :on-pre-solve
      (fn [screen entities]
        (println (:contact screen)) ; the Contact - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/Contact.html
        (println (:impulse screen)) ; the ContactImpulse - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/ContactImpulse.html
        entities)
      ; called between each use of `step!` after the collision is processed
      :on-post-solve
      (fn [screen entities]
        (println (:contact screen)) ; the Contact - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/Contact.html
        (println (:old-manifold screen)) ; the Manifold - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/physics/box2d/Manifold.html
        entities))

    ; 3D physics contact (for play-clj.g3d-physics)
    ; Tip: use first-entity and second-entity to get the entities that are contacting
    (defscreen my-screen
      ; two bodies began to touch
      :on-begin-contact
      (fn [screen entities]
        (println (:first-body screen)) ; the first btCollisionObject - http://bulletphysics.org/Bullet/BulletFull/classbtCollisionObject.html
        (println (:second-body screen)) ; the second btCollisionObject - http://bulletphysics.org/Bullet/BulletFull/classbtCollisionObject.html
        entities)
      ; two bodies ceased to touch
      :on-end-contact
      (fn [screen entities]
        (println (:first-body screen)) ; the first btCollisionObject - http://bulletphysics.org/Bullet/BulletFull/classbtCollisionObject.html
        (println (:second-body screen)) ; the second btCollisionObject - http://bulletphysics.org/Bullet/BulletFull/classbtCollisionObject.html
        entities))

    ; ui input functions (for play-clj.ui)
    ; Tip: use click-listener! to configure these functions
    (defscreen my-screen
      ; the ui entity was changed
      :on-ui-changed
      (fn [screen entities]
        (println (:event screen)) ; the ChangeListener.ChangeEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/utils/ChangeListener.ChangeEvent.html
        (println (:actor screen)) ; the Actor - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/Actor.html
        entities)
      ; the ui entity was clicked
      :on-ui-clicked
      (fn [screen entities]
        (println (:event screen)) ; the InputEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/InputEvent.html
        (println (:input-x screen)) ; the x position of the finger/mouse
        (println (:input-y screen)) ; the y position of the finger/mouse
        entities)
      ; the finger/mouse moved over the ui entity
      :on-ui-enter
      (fn [screen entities]
        (println (:event screen)) ; the InputEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/InputEvent.html
        (println (:actor screen)) ; the Actor - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/Actor.html
        (println (:input-x screen)) ; the x position of the finger/mouse
        (println (:input-y screen)) ; the y position of the finger/mouse
        (println (:pointer screen)) ; the pointer for the event
        entities)
      ; the finger/mouse moved out of the ui entity
      :on-ui-exit
      (fn [screen entities]
        (println (:event screen)) ; the InputEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/InputEvent.html
        (println (:actor screen)) ; the Actor - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/Actor.html
        (println (:input-x screen)) ; the x position of the finger/mouse
        (println (:input-y screen)) ; the y position of the finger/mouse
        (println (:pointer screen)) ; the pointer for the event
        entities)
      ; the finger/mouse went down on the ui entity
      :on-ui-touch-down
      (fn [screen entities]
        (println (:event screen)) ; the InputEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/InputEvent.html
        (println (:input-x screen)) ; the x position of the finger/mouse
        (println (:input-y screen)) ; the y position of the finger/mouse
        (println (:pointer screen)) ; the pointer for the event
        (println (:button screen)) ; the mouse button that was pressed (see button-code)
        entities)
      ; the finger/mouse moved anywhere
      :on-ui-touch-dragged
      (fn [screen entities]
        (println (:event screen)) ; the InputEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/InputEvent.html
        (println (:input-x screen)) ; the x position of the finger/mouse
        (println (:input-y screen)) ; the y position of the finger/mouse
        (println (:pointer screen)) ; the pointer for the event
        entities)
      ; the finger/mouse went up anywhere
      :on-ui-touch-up
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
      ; the user dragged a finger over the screen and lifted it
      :on-ui-fling
      (fn [screen entities]
        (println (:event screen)) ; the InputEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/InputEvent.html
        (println (:velocity-x screen)) ; the x-axis velocity (s)
        (println (:velocity-y screen)) ; the y-axis velocity (s)
        (println (:button screen)) ; the mouse button that was pressed (see button-code)
        entities)
      ; the user pressed
      :on-ui-long-press
      (fn [screen entities]
        (println (:actor screen)) ; the Actor - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/Actor.html
        (println (:input-x screen)) ; the x position of the finger
        (println (:input-y screen)) ; the y position of the finger
        entities)
      ; the user dragged a finger over the screen
      :on-ui-pan
      (fn [screen entities]
        (println (:event screen)) ; the InputEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/InputEvent.html
        (println (:input-x screen)) ; the x position of the finger
        (println (:input-y screen)) ; the y position of the finger
        (println (:delta-x screen)) ; the x-axis distance moved
        (println (:delta-y screen)) ; the y-axis distance moved
        entities)
      ; the user is no longer panning
      :on-ui-pan-stop
      (fn [screen entities]
        (println (:event screen)) ; the InputEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/InputEvent.html
        (println (:input-x screen)) ; the x position of the finger
        (println (:input-y screen)) ; the y position of the finger
        (println (:pointer screen)) ; the pointer for the event
        (println (:button screen)) ; the mouse button that was pressed (see button-code)
        entities)
      ; the user performed a pinch zoom gesture
      :on-ui-pinch
      (fn [screen entities]
        (println (:event screen)) ; the InputEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/InputEvent.html
        (println (:initial-pointer-1 screen)) ; the start position of finger 1 (see the x and y functions)
        (println (:initial-pointer-2 screen)) ; the start position of finger 2 (see the x and y functions)
        (println (:pointer-1 screen)) ; the end position of finger 1 (see the x and y functions)
        (println (:pointer-2 screen)) ; the end position of finger 2 (see the x and y functions)
        entities)
      ; the user tapped
      :on-ui-tap
      (fn [screen entities]
        (println (:event screen)) ; the InputEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/InputEvent.html
        (println (:input-x screen)) ; the x position of the finger
        (println (:input-y screen)) ; the y position of the finger
        (println (:count screen)) ; the number of taps
        (println (:button screen)) ; the mouse button that was pressed (see button-code)
        entities)
      ; the user performed a pinch zoom gesture
      :on-ui-zoom
      (fn [screen entities]
        (println (:event screen)) ; the InputEvent - http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/InputEvent.html
        (println (:initial-distance screen)) ; the start distance between fingers
        (println (:distance screen)) ; the end distance between fingers
        entities))"
  [n & options]
  (let [s (format "Unexpected value in (defscreen %s). You need to give it
keywords and functions in pairs."
                  (str n))]
    (assert (even? (count options)) s)
    (assert (= 0 (count (remove keyword? (keys (apply hash-map options))))) s))
  `(let [fn-syms# (->> (for [[k# v#] ~(apply hash-map options)]
                         [k# (intern *ns* (symbol (str '~n "-" (name k#))) v#)])
                       (into {}))
         map-sym# (symbol (str '~n "-map"))
         screen# (deref (or (resolve map-sym#)
                            (intern *ns* map-sym# (atom {}))))
         entities-sym# (symbol (str '~n "-entities"))
         entities# (deref (or (resolve entities-sym#)
                              (intern *ns* entities-sym# (atom []))))]
     (def ~n (defscreen* screen# entities# fn-syms#))))

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
  [screen & args]
  (doto (apply (:update-fn! screen) assoc args)
    update-screen!))

(defn screen!
  "Runs a function defined in another screen. You may optionally pass a series
of key-value pairs, which will be given to the function via its screen map.

    (screen! my-other-screen :on-show)
    (screen! my-other-screen :on-change-color :color :blue)"
  [screen-object fn-name & options]
  (let [execute-fn! (:execute-fn! screen-object)
        screen-fn (-> screen-object :options (get fn-name))]
    (apply execute-fn! screen-fn options)
    nil))
