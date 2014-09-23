(in-ns 'play-clj.core)

(defn find-first
  "Returns the first entity in `entities` for which `match-fn` returns true.

    (find-first :player? entities)
    (find-first #(= :menu (:id %)) entities)"
  [match-fn entities]
  (some #(when (match-fn %) %) entities))

(defmacro on-gl
  "Runs the macro body on the GL thread.

    (on-gl (set-screen! my-game main-screen))"
  [& body]
  `(app! :post-runnable (fn [] ~@body)))

(defn bundle
  "Returns an entity containing other entities. This is a useful way to keep
related entities together. They will be drawn in the order they appear in the
internal :entities vector. Any keys in the bundle, such as :x and :y, will
be passed down to all the internal entities unless they already have those keys.

    (bundle (shape :filled) (shape :line))
    (assoc (bundle (shape :filled) (shape :line))
           :x 100 :y 100)"
  [& entities]
  (BundleEntity. (vec entities)))

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

(defn rewind!
  "Returns the most recent entities vector saved in the timeline after removing
the last `steps` from it. Start recording to the timeline by calling
`(update! screen :timeline [])`.

If there are 100 items saved in the timeline and `(rewind! screen 1)` is called,
it will remove the last one and return the entities vector from the 99th item.
If `steps` is invalid, nil is returned.

If you want to do something more complex with the timeline than a simple rewind,
you can directly access it via `(:timeline screen)`. Each item inside it is a
vector containing a timestamp (seconds since the screen was first shown) and an
entities vector."
  [{:keys [timeline update-fn!] :as screen} steps]
  (when-let [[total-time entities] (get timeline (- (count timeline) steps 1))]
    (update-fn! update-in [:timeline] subvec 0 (- (count timeline) steps 1))
    entities))

; static fields

(defmacro scaling
  "Returns a static field from [Scaling](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/utils/Scaling.html).

    (scaling :fill)"
  [k]
  (u/gdx-field :utils :Scaling k))

(defmacro usage
  "Returns a static field in [VertexAttributes.Usage](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/VertexAttributes.Usage.html)."
  [k]
  (u/gdx-field :graphics "VertexAttributes$Usage" (u/key->pascal k)))

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
  ; remove timer if it already exists
  (when-let [old-timer (get-in screen [:timers id])]
    (.stop old-timer)
    (some-> u/*timers* (swap! disj old-timer)))
  ; create timer, add to screen map, and return it
  (let [new-timer (timer*)]
    (update-fn! assoc-in [:timers id] new-timer)
    (some-> u/*timers* (swap! conj new-timer))
    new-timer))

(defn add-timer!
  "Returns a [Timer](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/utils/Timer.html)
which will fire the :on-timer function one or more times (depending on the given
arguments). In the :on-timer function, the id will be passed in the screen map.
If a timer with that id already exists in the screen, it will be stopped and
replaced with a new timer.

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
    (update-fn! update-in [:timers] dissoc id)
    timer))

; assets

(defn ^:private set-loaders!
  ([am]
    (set-loaders! am (InternalFileHandleResolver.)))
  ([^AssetManager am res]
    (.setLoader am TiledMap (TmxMapLoader. res))
    (.setLoader am ParticleEffect (proxy [ParticleEffectLoader] [res]
                                    (load [am file-name fh param]
                                      (doto (ParticleEffect.)
                                        (.load fh (.parent fh))))))))

(defn asset-manager*
  ([]
    (doto (AssetManager.) set-loaders!))
  ([resolver]
    (doto (AssetManager. resolver) (set-loaders! resolver))))

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
