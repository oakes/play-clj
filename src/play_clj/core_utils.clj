(in-ns 'play-clj.core)

(defmacro on-gl
  "Runs the macro body on the GL thread.

    (on-gl (set-screen! hello-world main-screen))"
  [& body]
  `(app! :post-runnable (fn [] ~@body)))

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

(defn ^:private loader-init
  [k]
  (cond
    (contains? #{:atlas-tmx-map :tmx-map} k)
    (u/gdx :maps :tiled (str (u/key->pascal k) "Loader."))
    (contains? #{:g3d-model :obj} k)
    (u/gdx :graphics :g3d :loader (str (u/key->pascal k) "Loader."))
    :else
    (u/gdx :assets :loaders (str (u/key->pascal k) "Loader."))))

(defmacro loader
  "Returns a subclass of [AsynchronousAssetLoader](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/assets/loaders/AsynchronousAssetLoader.html).

    (loader :atlas-tmx-map \"map.atlas\")
    (loader :tmx-map \"map.tmx\")
    (loader :obj \"model.obj\")
    (loader :g3d-model \"cube.g3dj\")
    (loader :bitmap-font \"arial.fnt\")
    (loader :music \"song.ogg\")
    (loader :pixmap \"background.png\")
    (loader :skin \"uiskin.json\")
    (loader :sound \"hit.ogg\")
    (loader :texture \"monster.png\")"
  [type resolver & options]
  `(let [object# (~(loader-init type) ~resolver)]
     (u/calls! object# ~@options)))

(defmacro loader!
  "Calls a single method in a subclass of [AsynchronousAssetLoader](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/assets/loaders/AsynchronousAssetLoader.html).

    (loader! object :load \"map.tmx\")"
  [object & options]
  `(u/call! ~object ~@options))

(defn asset-manager*
  ([]
    (AssetManager.))
  ([resolver]
    (AssetManager. resolver)))

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
