(in-ns 'play-clj.core)

; global

(defn ^:private input-processor
  [{:keys [on-key-down on-key-typed on-key-up on-mouse-moved
           on-scrolled on-touch-down on-touch-dragged on-touch-up]}
   execute-fn!]
  (reify InputProcessor
    (keyDown [this k]
      (execute-fn! on-key-down :keycode k)
      false)
    (keyTyped [this c]
      (execute-fn! on-key-typed :character c)
      false)
    (keyUp [this k]
      (execute-fn! on-key-up :keycode k)
      false)
    (mouseMoved [this sx sy]
      (execute-fn! on-mouse-moved :screen-x sx :screen-y sy)
      false)
    (scrolled [this a]
      (execute-fn! on-scrolled :amount a)
      false)
    (touchDown [this sx sy p b]
      (execute-fn! on-touch-down :screen-x sx :screen-y sy :pointer p :button b)
      false)
    (touchDragged [this sx sy p]
      (execute-fn! on-touch-dragged :screen-x sx :screen-y sy :pointer p)
      false)
    (touchUp [this sx sy p b]
      (execute-fn! on-touch-up :screen-x sx :screen-y sy :pointer p :button b)
      false)))

(defn ^:private gesture-listener
  [{:keys [on-fling on-long-press on-pan on-pan-stop on-pinch on-tap on-zoom]}
   execute-fn!]
  (reify GestureDetector$GestureListener
    (fling [this vx vy b]
      (execute-fn! on-fling :velocity-x vx :velocity-y vy :button b)
      false)
    (longPress [this x y]
      (execute-fn! on-long-press :x x :y y)
      false)
    (pan [this x y dx dy]
      (execute-fn! on-pan :x x :y y :delta-x dx :delta-y dy)
      false)
    (panStop [this x y p b]
      (execute-fn! on-pan-stop :x x :y y :pointer p :button b)
      false)
    (pinch [this ip1 ip2 p1 p2]
      (execute-fn! on-pinch
                   :initial-pointer-1 ip1 :initial-pointer-2 ip2
                   :pointer1 p1 :pointer2 p2)
      false)
    (tap [this x y c b]
      (execute-fn! on-tap :x x :y y :count c :button b)
      false)
    (touchDown [this x y p b]
      false)
    (zoom [this id d]
      (execute-fn! on-zoom :initial-distance id :distance d)
      false)))

(defn ^:private gesture-detector
  [options execute-fn!]
  (proxy [GestureDetector] [(gesture-listener options execute-fn!)]))

(defn ^:private global-listeners
  [options execute-fn!]
  [(input-processor options execute-fn!)
   (gesture-detector options execute-fn!)])

; ui

(defn ^:private actor-gesture-listener
  [{:keys [on-ui-fling on-ui-long-press on-ui-pan on-ui-pinch
           on-ui-tap on-ui-touch-down on-ui-touch-up on-ui-zoom]}
   execute-fn!]
  (proxy [ActorGestureListener] []
    (fling [e vx vy b]
      (execute-fn! on-ui-fling
                   :event e :velocity-x vx :velocity-y vy :button b))
    (longPress [a x y]
      (execute-fn! on-ui-long-press :actor a :x x :y y)
      false)
    (pan [e x y dx dy]
      (execute-fn! on-ui-pan :event e :x x :y y :delta-x dx :delta-y dy))
    (pinch [e ip1 ip2 p1 p2]
      (execute-fn! on-ui-pinch
                   :event e :initial-pointer-1 ip1 :initial-pointer-2 ip2
                   :pointer1 p1 :pointer2 p2))
    (tap [e x y p b]
      (execute-fn! on-ui-tap :event e :x x :y y :pointer p :button b))
    (touchDown [e x y p b]
      (execute-fn! on-ui-touch-down :event e :x x :y y :pointer p :button b))
    (touchUp [e x y p b]
      (execute-fn! on-ui-touch-up :event e :x x :y y :pointer p :button b))
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
      (execute-fn! on-ui-clicked :event e :x x :y y))
    (enter [e x y p a]
      (execute-fn! on-ui-enter :event e :x x :y y :pointer p :from-actor a))
    (exit [e x y p a]
      (execute-fn! on-ui-exit :event e :x x :y y :pointer p :to-actor a))
    (touchDown [e x y p b]
      (execute-fn! on-ui-touch-down :event e :x x :y y :pointer p :button b)
      false)
    (touchDragged [e x y p]
      (execute-fn! on-ui-touch-dragged :event e :x x :y y :pointer p))
    (touchUp [e x y p b]
      (execute-fn! on-ui-touch-up :event e :x x :y y :pointer p :button b))))

(defn ^:private drag-listener
  [{:keys [on-ui-drag on-ui-drag-start on-ui-drag-stop
           on-ui-touch-down on-ui-touch-dragged on-ui-touch-up]}
   execute-fn!]
  (proxy [DragListener] []
    (touchDown [e x y p b]
      (execute-fn! on-ui-touch-down :event e :x x :y y :pointer p :button b)
      false)
    (touchDragged [e x y p]
      (execute-fn! on-ui-touch-dragged :event e :x x :y y :pointer p))
    (touchUp [e x y p b]
      (execute-fn! on-ui-touch-up :event e :x x :y y :pointer p :button b))
    (drag [e x y p]
      (execute-fn! on-ui-drag :event e :x x :y y :pointer p))
    (dragStart [e x y p]
      (execute-fn! on-ui-drag-start :event e :x x :y y :pointer p))
    (dragStop [e x y p]
      (execute-fn! on-ui-drag-stop :event e :x x :y y :pointer p))))

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
  [(actor-gesture-listener options execute-fn!)
   (change-listener options execute-fn!)
   (click-listener options execute-fn!)
   (drag-listener options execute-fn!)
   (focus-listener options execute-fn!)])

; g2d-physics

(defn ^:private contact-listener
  [{:keys [on-begin-contact on-end-contact on-post-solve on-pre-solve]} execute-fn!]
  (reify ContactListener
    (beginContact [this c]
      (execute-fn! on-begin-contact :contact c))
    (endContact [this c]
      (execute-fn! on-end-contact :contact c))
    (postSolve [this c i]
      (execute-fn! on-post-solve :contact c :impulse i))
    (preSolve [this c m]
      (execute-fn! on-pre-solve :contact c :old-manifold m))))

; update functions

(defn ^:private update-stage!
  [{:keys [^Stage renderer ui-listeners]} entities]
  (doseq [^Actor a (.getActors renderer)]
    (.remove a))
  (doseq [{:keys [object]} entities]
    (when (isa? (type object) Actor)
      (.addActor renderer object)
      (doseq [listener ui-listeners]
        (.addListener ^Actor object listener))))
  (remove-input! renderer)
  (add-input! renderer))

(defn ^:private update-box-2d!
  [{:keys [^World world]} entities]
  (when-not (.isLocked world)
    (let [arr (u/gdx-array [])]
      ; remove bodies that no longer exist
      (.getBodies world arr)
      (doseq [body arr]
        (when-not (some #(= body (:body %)) entities)
          (.destroyBody world body)))
      ; remove joints whose bodies no longer exist
      (.getJoints world arr)
      (doseq [^Joint joint arr]
        (when (and (not (some #(= (.getBodyA joint) (:body %)) entities))
                   (not (some #(= (.getBodyB joint) (:body %)) entities)))
          (.destroyJoint world joint))))))

(defn ^:private update-screen!
  ([{:keys [world g2dp-listener]}]
    (when (isa? (type world) World)
      (.setContactListener ^World world g2dp-listener)))
  ([{:keys [renderer world] :as screen} entities]
    (when (isa? (type renderer) Stage)
      (update-stage! screen entities))
    (when (isa? (type world) World)
      (update-box-2d! screen entities))))
