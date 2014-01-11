(in-ns 'play-clj.core)

(defmacro style
  [type & options]
  `(~(symbol (str utils/gdx-package ".scenes.scene2d.ui."
                  (utils/key->class type) "$"
                  (utils/key->class type) "Style."))
     ~@options))

(defmacro drawable
  [type & options]
  `(~(symbol (str utils/gdx-package ".scenes.scene2d.utils."
                  (utils/key->class type) "Drawable."))
     ~@options))

(defmacro skin
  [path & options]
  `(utils/calls! ^Skin (Skin. (files! :internal ~path)) ~@options))

; widgets

(defn check-box*
  [^String text arg]
  (CheckBox. text arg))

(defmacro check-box
  [text arg & options]
  `(create-entity (utils/calls! ^CheckBox (check-box* ~text ~arg) ~@options)))

(defn image-button*
  [arg]
  (ImageButton. arg))

(defmacro image-button
  [arg & options]
  `(create-entity (utils/calls! ^ImageButton (image-button* ~arg) ~@options)))

(defn image-text-button*
  [^String text arg]
  (ImageTextButton. text arg))

(defmacro image-text-button
  [text arg & options]
  `(create-entity
     (utils/calls! ^ImageTextButton (image-text-button* ~text ~arg) ~@options)))

(defn label*
  [^String text arg]
  (if (isa? (type arg) Color)
    (Label. text (style :label (bitmap-font) arg))
    (Label. text arg)))

(defmacro label
  [text arg & options]
  `(create-entity (utils/calls! ^Label (label* ~text ~arg) ~@options)))

(defn slider*
  [min max step is-vert? arg]
  (Slider. (float min) (float max) (float step) is-vert? arg))

(defmacro slider
  [min max step is-vert? arg & options]
  `(create-entity
     (utils/calls! ^Slider (slider* ~min ~max ~step ~is-vert? ~arg) ~@options)))

(defn text-button*
  [^String text arg]
  (TextButton. text arg))

(defmacro text-button
  [text arg & options]
  `(create-entity
     (utils/calls! ^TextButton (text-button* ~text ~arg) ~@options)))

(defn text-field*
  [^String text arg]
  (TextField. text arg))

(defmacro text-field
  [text arg & options]
  `(create-entity (utils/calls! ^TextField (text-field* ~text ~arg) ~@options)))

(defn dialog*
  [text arg]
  (Dialog. text arg))

(defmacro dialog
  [text arg & options]
  `(create-entity (utils/calls! ^Dialog (dialog* ~text ~arg) ~@options)))

; listeners

(defn ui-listen!
  [{:keys [renderer ui-listeners] :as screen} entities]
  (assert (isa? (type renderer) Stage))
  (add-input! renderer)
  (stage! screen :clear)
  (doseq [{:keys [object]} entities]
    (when (isa? (type object) Actor)
      (stage! screen :add-actor object)
      (doseq [listener ui-listeners]
        (.addListener ^Actor object listener))))
  entities)

(defn- ui-gesture-listener
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
    ;(touchDown [e x y p b]
    ;  (execute-fn! on-ui-touch-down :event e :x x :y y :pointer p :button b))
    ;(touchUp [e x y p b]
    ;  (execute-fn! on-ui-touch-up :event e :x x :y y :pointer p :button b))
    (zoom [e id d]
      (execute-fn! on-ui-zoom :event e :initial-distance id :distance d))))

(defn- ui-change-listener
  [{:keys [on-ui-changed]} execute-fn!]
  (proxy [ChangeListener] []
    (changed [e a]
      (execute-fn! on-ui-changed :event e :actor a))))

(defn- ui-click-listener
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
      true)
    (touchDragged [e x y p]
      (execute-fn! on-ui-touch-dragged :event e :x x :y y :pointer p))
    (touchUp [e x y p b]
      (execute-fn! on-ui-touch-up :event e :x x :y y :pointer p :button b))))

(defn- ui-drag-listener
  [{:keys [on-ui-drag on-ui-drag-start on-ui-drag-stop
           on-ui-touch-down on-ui-touch-dragged on-ui-touch-up]}
   execute-fn!]
  (proxy [DragListener] []
    ;(touchDown [e x y p b]
    ;  (execute-fn! on-ui-touch-down :event e :x x :y y :pointer p :button b)
    ;  true)
    ;(touchDragged [e x y p]
    ;  (execute-fn! on-ui-touch-dragged :event e :x x :y y :pointer p))
    ;(touchUp [e x y p b]
    ;  (execute-fn! on-ui-touch-up :event e :x x :y y :pointer p :button b))
    (drag [e x y p]
      (execute-fn! on-ui-drag :event e :x x :y y :pointer p))
    (dragStart [e x y p]
      (execute-fn! on-ui-drag-start :event e :x x :y y :pointer p))
    (dragStop [e x y p]
      (execute-fn! on-ui-drag-stop :event e :x x :y y :pointer p))))

(defn- ui-focus-listener
  [{:keys [on-ui-keyboard-focus-changed on-ui-scroll-focus-changed]}
   execute-fn!]
  (proxy [FocusListener] []
    (keyboardFocusChanged [e a f]
      (execute-fn! on-ui-keyboard-focus-changed :event e :actor a :focused? f))
    (scrollFocusChanged [e a f]
      (execute-fn! on-ui-scroll-focus-changed :event e :actor a :focused? f))))
