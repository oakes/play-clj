(in-ns 'play-clj.core)

(defmacro on-gl
  "Runs the macro body on the GL thread.

    (on-gl (set-screen! my-game main-screen))"
  [& body]
  `(app! :post-runnable (fn [] ~@body)))

(defn bundle
  "Returns an entity containing other entities. This is a useful way to keep
related entities together. They will be drawn in the order they appear in the
internal :entities vector. Any keys in the bundle, such as :x and :y, will
override the equivalent keys in each entity when being drawn.

    (bundle (shape :filled) (shape :line))
    (assoc (bundle (shape :filled) (shape :line))
           :x 100 :y 100)"
  [& entities]
  (BundleEntity. entities))

(defn bundle?
  "Returns true if `entity` is a `bundle`."
  [entity]
  (isa? (type entity) BundleEntity))

(defn screenshot!
  "Captures a screenshot and either returns it as a `pixmap` or saves it to the
specified path.

    (screenshot!)
    (screenshot! \"out.png\")
    (screenshot! (files! :external \"out.png\"))"
  ([]
    (let [^Pixmap$Format pic-f (Pixmap$Format/RGBA8888)
          ^Pixmap pic (Pixmap. ^long (game :width) ^long (game :height) pic-f)
          pixel-data (ScreenUtils/getFrameBufferPixels true)
          pixels (.getPixels pic)]
      (doto pixels
        (.clear)
        (.put pixel-data)
        (.position 0))
      pic))
  ([path]
    (let [pic (screenshot!)
          handle (if (string? path)
                   (files! :local path)
                   path)]
      (PixmapIO/writePNG handle pic)
      (pixmap! pic :dispose))))

(defmacro pref!
  "Retrieves and stores preferences. The `name` should be a unique string.

    ; define the name we'll be using
    (def ^:const pref-name \"my-game.settings\")
    ; store a single preference
    (pref! pref-name :put-float \"player-health\" 40)
    ; store multiple preferences
    (pref! pref-name :put {\"player-health\" 40
                           \"player-x\" 20
                           \"player-y\" 50})
    ; save the changes to the disk
    (pref! pref-name :flush)
    ; retrieve a single preference
    (pref! pref-name :get-float \"player-health\")"
  [name k & options]
  `(let [^Preferences p# (if (string? ~name)
                           (app! :get-preferences ~name)
                           ~name)]
     (u/call! p# ~k ~@options)))

; static fields

(defmacro scaling
  "Returns a static field from [Scaling](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/utils/Scaling.html).

    (scaling :fill)"
  [k]
  `~(u/gdx-field :utils :Scaling k))

(defmacro usage
  "Returns a static field in [VertexAttributes.Usage](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/VertexAttributes.Usage.html)."
  [k]
  `~(u/gdx-field :graphics "VertexAttributes$Usage" (u/key->pascal k)))

; timers

(defn ^:private task*
  [{:keys [execute-fn! on-timer]} id]
  (proxy [Timer$Task] []
    (run []
      (execute-fn! on-timer :id id))))

(defn ^:private timer*
  []
  (some-> (Class/forName "com.badlogic.gdx.utils.Timer")
          (try (catch Exception _))
          .newInstance))

(defn ^:private create-and-add-timer!
  [{:keys [update-fn!] :as screen} id]
  (when-let [timer (timer*)]
    (update-fn! assoc-in [[:timers id] timer])
    timer))

(defn add-timer!
  "Returns a [Timer](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/utils/Timer.html)
that runs the :on-timer function according to the given arguments.

    ; wait 2 seconds and run once
    (add-timer! screen :spawn-enemy 2)
    ; wait 2 seconds and run forever at 10 second intervals
    (add-timer! screen :spawn-enemy 2 10)
    ; wait 2 seconds, run once, and then run 3 more times at 10 second intervals
    (add-timer! screen :spawn-enemy 2 10 3)"
  ([screen id delay]
    (doto (create-and-add-timer! screen id)
      (.scheduleTask (task* screen id) delay)))
  ([screen id delay interval]
    (doto (create-and-add-timer! screen id)
      (.scheduleTask (task* screen id) delay interval)))
  ([screen id delay interval repeat]
    (doto (create-and-add-timer! screen id)
      (.scheduleTask (task* screen id) delay interval repeat))))

(defn remove-timer!
  "Stops and removes the timer associated with `id`, returning it or nil if not
found."
  [{:keys [update-fn!] :as screen} id]
  (when-let [timer (get-in screen [:timers id])]
    (.stop timer)
    (update-fn! update-in [[:timers] dissoc id])
    timer))

; assets

(defn ^:private loader-class
  [k]
  (cond
    (contains? #{:atlas-tmx-map :tmx-map} k)
    (u/gdx :maps :tiled (str (u/key->pascal k) "Loader"))
    (contains? #{:g3d-model :obj} k)
    (u/gdx :graphics :g3d :loader (str (u/key->pascal k) "Loader"))
    :else
    (u/gdx :assets :loaders (str (u/key->pascal k) "Loader"))))

(defmacro loader
  "Returns a subclass of [AssetLoader](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/assets/loaders/AssetLoader.html).

    (loader :atlas-tmx-map (resolver :internal-file-handle))
    (loader :bitmap-font (resolver :internal-file-handle))
    (loader :g3d-model (resolver :internal-file-handle))
    (loader :model (resolver :internal-file-handle))
    (loader :music (resolver :internal-file-handle))
    (loader :obj (resolver :internal-file-handle))
    (loader :pixmap (resolver :internal-file-handle))
    (loader :skin (resolver :internal-file-handle))
    (loader :sound (resolver :internal-file-handle))
    (loader :texture (resolver :internal-file-handle))
    (loader :tmx-map (resolver :internal-file-handle))
    (loader :tmx-map
            (resolver :internal-file-handle)
            (load [file-name] nil))"
  [type resolver & options]
  `(proxy [~(loader-class type)] [~resolver] ~@options))

(defmacro loader!
  "Calls a single method in a subclass of [AsynchronousAssetLoader](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/assets/loaders/AsynchronousAssetLoader.html).

    (loader! object :tmx-map :load \"map.tmx\")"
  [object type & options]
  `(let [^AsynchronousAssetLoader object# ~object]
     (u/call! object# ~@options)))

(defn ^:private resolver-class
  [k]
  (u/gdx :assets :loaders :resolvers (str (u/key->pascal k) "Resolver")))

(defmacro resolver
  "Returns an implementation of [FileHandleResolver](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/assets/loaders/FileHandleResolver.html).

    (resolver :internal-file-handle)
    (resolver :internal-file-handle
              (resolve [file-name]
                (files! :internal file-name)))"
  [type & options]
  `(proxy [~(resolver-class type)] [] ~@options))

(defn ^:private set-loaders!
  [^AssetManager am]
  (->> (loader :tmx-map (resolver :internal-file-handle))
       (.setLoader am TiledMap))
  (->> (loader :particle-effect
               (resolver :internal-file-handle)
               (load [am file-name fh param]
                     (doto (ParticleEffect.)
                       (.load fh (.parent fh)))))
       (.setLoader am ParticleEffect)))

(defn asset-manager*
  ([]
    (doto (AssetManager.) set-loaders!))
  ([resolver]
    (doto (AssetManager. resolver) set-loaders!)))

(defmacro asset-manager
  "Returns an [AssetManager](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/assets/AssetManager.html).

    (asset-manager)"
  [& options]
  `(let [^AssetManager object# (asset-manager*)]
     (u/calls! object# ~@options)))

(defmacro asset-manager!
  "Calls a single method in an `asset-manager`.

    (asset-manager! object :clear)"
  [object k & options]
  `(let [^AssetManager object# ~object]
     (u/call! object# ~k ~@options)))

(defn set-asset-manager!
  "Sets a global asset manager, which will keep track of objects that need to
be manually disposed, such as `texture` entities and `pixmap` objects. The
asset manager will then allow you to dispose them all at once.

    ; create an asset manager
    (defonce manager (asset-manager))
    ; set it to be used by play-clj
    (set-asset-manager! manager)
    ; dispose all assets at once
    (asset-manager! manager :clear)"
  [am]
  (intern 'play-clj.utils '*asset-manager* am))
