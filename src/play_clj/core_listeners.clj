(in-ns 'play-clj.core)

; global

(defn ^:private input-processor
  [{:keys [on-key-down on-key-typed on-key-up on-mouse-moved
           on-scrolled on-touch-down on-touch-dragged on-touch-up]}
   execute-fn!]
  (reify InputProcessor
    (keyDown [this k]
      (execute-fn! on-key-down :key k)
      false)
    (keyTyped [this c]
      (execute-fn! on-key-typed :character c)
      false)
    (keyUp [this k]
      (execute-fn! on-key-up :key k)
      false)
    (mouseMoved [this sx sy]
      (execute-fn! on-mouse-moved :input-x sx :input-y sy)
      false)
    (scrolled [this a]
      (execute-fn! on-scrolled :amount a)
      false)
    (touchDown [this sx sy p b]
      (execute-fn! on-touch-down :input-x sx :input-y sy :pointer p :button b)
      false)
    (touchDragged [this sx sy p]
      (execute-fn! on-touch-dragged :input-x sx :input-y sy :pointer p)
      false)
    (touchUp [this sx sy p b]
      (execute-fn! on-touch-up :input-x sx :input-y sy :pointer p :button b)
      false)))

(defmacro input-processor!
  "Calls a single method on the [InputProcessor](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/InputProcessor.html)
in the `screen`."
  [screen k & options]
  `(let [listeners# (u/get-obj ~screen :input-listeners)
         ^InputProcessor object# (u/get-obj listeners# :input-processor)]
     (u/call! object# ~k ~@options)))

(defn ^:private gesture-listener
  [{:keys [on-fling on-long-press on-pan on-pan-stop on-pinch on-tap on-zoom]}
   execute-fn!]
  (reify GestureDetector$GestureListener
    (fling [this vx vy b]
      (execute-fn! on-fling :velocity-x vx :velocity-y vy :button b)
      (some? on-fling))
    (longPress [this x y]
      (execute-fn! on-long-press :input-x x :input-y y)
      (some? on-long-press))
    (pan [this x y dx dy]
      (execute-fn! on-pan :input-x x :input-y y :delta-x dx :delta-y dy)
      (some? on-pan))
    (panStop [this x y p b]
      (execute-fn! on-pan-stop :input-x x :input-y y :pointer p :button b)
      (some? on-pan-stop))
    (pinch [this ip1 ip2 p1 p2]
      (execute-fn! on-pinch
                   :initial-pointer-1 ip1 :initial-pointer-2 ip2
                   :pointer-1 p1 :pointer-2 p2)
      (some? on-pinch))
    (tap [this x y c b]
      (execute-fn! on-tap :input-x x :input-y y :count c :button b)
      (some? on-tap))
    (touchDown [this x y p b]
      false)
    (zoom [this id d]
      (execute-fn! on-zoom :initial-distance id :distance d)
      (some? on-zoom))))

(defn ^:private gesture-detector
  [options execute-fn!]
  (proxy [GestureDetector] [(gesture-listener options execute-fn!)]))

(defmacro gesture-detector!
  "Calls a single method on the [GestureDetector](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/input/GestureDetector.html)
in the `screen`."
  [screen k & options]
  `(let [listeners# (u/get-obj ~screen :input-listeners)
         ^GestureDetector object# (u/get-obj listeners# :gesture-detector)]
     (u/call! object# ~k ~@options)))

(defn ^:private input-listeners
  [options execute-fn!]
  {:input-processor (input-processor options execute-fn!)
   :gesture-detector (gesture-detector options execute-fn!)})

; ui

(defn ^:private actor-gesture-listener
  [{:keys [on-ui-fling on-ui-long-press on-ui-pan on-ui-pinch
           on-ui-tap on-ui-zoom]}
   execute-fn!]
  (proxy [ActorGestureListener] []
    (fling [e vx vy b]
      (execute-fn! on-ui-fling
                   :event e :velocity-x vx :velocity-y vy :button b))
    (longPress [a x y]
      (execute-fn! on-ui-long-press :actor a :input-x x :input-y y)
      (some? on-ui-long-press))
    (pan [e x y dx dy]
      (execute-fn! on-ui-pan
                   :event e :input-x x :input-y y :delta-x dx :delta-y dy))
    (pinch [e ip1 ip2 p1 p2]
      (execute-fn! on-ui-pinch
                   :event e :initial-pointer-1 ip1 :initial-pointer-2 ip2
                   :pointer1 p1 :pointer2 p2))
    (tap [e x y p b]
      (execute-fn! on-ui-tap
                   :event e :input-x x :input-y y :pointer p :button b))
    ;(touchDown [e x y p b])
    ;(touchUp [e x y p b])
    (zoom [e id d]
      (execute-fn! on-ui-zoom :event e :initial-distance id :distance d))))

(defn ^:private change-listener
  [{:keys [on-ui-changed]} execute-fn!]
  (proxy [ChangeListener] []
    (changed [e a]
      (execute-fn! on-ui-changed :event e :actor a))))

(defn ^:private click-listener
  [{:keys [on-ui-clicked on-ui-enter on-ui-exit
           on-ui-touch-down on-ui-touch-dragged on-ui-touch-up]}
   execute-fn!]
  (proxy [ClickListener] []
    (clicked [e x y]
      (execute-fn! on-ui-clicked :event e :input-x x :input-y y))
    (enter [e x y p a]
      (execute-fn! on-ui-enter
                   :event e :input-x x :input-y y :pointer p :actor a))
    (exit [e x y p a]
      (execute-fn! on-ui-exit
                   :event e :input-x x :input-y y :pointer p :actor a))
    (touchDown [e x y p b]
      (execute-fn! on-ui-touch-down
                   :event e :input-x x :input-y y :pointer p :button b)
      (some? on-ui-touch-down))
    (touchDragged [e x y p]
      (execute-fn! on-ui-touch-dragged
                   :event e :input-x x :input-y y :pointer p))
    (touchUp [e x y p b]
      (execute-fn! on-ui-touch-up
                   :event e :input-x x :input-y y :pointer p :button b))))

(defn ^:private drag-listener
  [{:keys [on-ui-drag on-ui-drag-start on-ui-drag-stop]}
   execute-fn!]
  (proxy [DragListener] []
    ;(touchDown [e x y p b])
    ;(touchDragged [e x y p])
    ;(touchUp [e x y p b])
    (drag [e x y p]
      (execute-fn! on-ui-drag :event e :input-x x :input-y y :pointer p))
    (dragStart [e x y p]
      (execute-fn! on-ui-drag-start :event e :input-x x :input-y y :pointer p))
    (dragStop [e x y p]
      (execute-fn! on-ui-drag-stop :event e :input-x x :input-y y :pointer p))))

(defn ^:private focus-listener
  [{:keys [on-ui-keyboard-focus-changed on-ui-scroll-focus-changed]}
   execute-fn!]
  (proxy [FocusListener] []
    (keyboardFocusChanged [e a f]
      (execute-fn! on-ui-keyboard-focus-changed :event e :actor a :focused? f))
    (scrollFocusChanged [e a f]
      (execute-fn! on-ui-scroll-focus-changed :event e :actor a :focused? f))))

(defn ^:private ui-listeners
  [options execute-fn!]
  {:actor-gesture-listener (actor-gesture-listener options execute-fn!)
   :change-listener (change-listener options execute-fn!)
   :click-listener (click-listener options execute-fn!)
   :drag-listener (drag-listener options execute-fn!)
   :focus-listener (focus-listener options execute-fn!)})

(defmulti contact-listener
  (fn [screen options execute-fn!] (some-> screen :world class .getName))
  :default nil)

(defmethod contact-listener nil [_ _ _])

; update functions

(defn ^:private update-stage!
  ([{:keys [^Stage renderer ^Camera camera] :as screen}]
   (when camera
     (doto (.getViewport renderer)
       (.setCamera camera)
       (.setWorldSize (. camera viewportWidth) (. camera viewportHeight))
       (.update (game :width) (game :height) true))))
  ([{:keys [^Stage renderer ui-listeners]} entities]
   (let [bundle? #(instance? BundleEntity %)
         current (->> entities
                      (mapcat #(tree-seq bundle? :entities %))
                      (map :object)
                      (filter #(instance? Actor %))
                      (into #{}))
           ;; realize this before removing from it, since mutable
         previous (set (.getActors renderer))
         entering (clojure.set/difference current previous)
         exiting (clojure.set/difference previous current)]
     (doseq [^Actor a exiting]
       (.remove a))
     (doseq [^Actor a entering]
       (.addActor renderer a)
       (doseq [[_ listener] ui-listeners]
         (.addListener a listener))))))

(defmulti update-physics!
  (fn [screen & [entities]] (some-> screen :world class .getName))
  :default nil)

(defmethod update-physics! nil [_ & _])

(defn ^:private update-screen!
  ([{:keys [renderer world] :as screen}]
   (when (instance? Stage renderer)
     (update-stage! screen))
   (update-physics! screen))
  ([{:keys [renderer world] :as screen} entities]
   (when (instance? Stage renderer)
     (update-stage! screen entities))
   (update-physics! screen entities)
   entities))
