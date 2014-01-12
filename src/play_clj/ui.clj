(ns play-clj.ui
  (:require [play-clj.utils :as u])
  (:import [com.badlogic.gdx Files Gdx]
           [com.badlogic.gdx.graphics Color Texture]
           [com.badlogic.gdx.graphics.g2d BitmapFont TextureRegion]
           [com.badlogic.gdx.scenes.scene2d Actor Stage]
           [com.badlogic.gdx.scenes.scene2d.ui ButtonGroup CheckBox Dialog
            HorizontalGroup Image ImageButton ImageTextButton Label Skin Slider
            Table TextButton TextField VerticalGroup WidgetGroup]
           [com.badlogic.gdx.scenes.scene2d.utils ActorGestureListener Align
            ChangeListener ClickListener DragListener FocusListener
            NinePatchDrawable SpriteDrawable TextureRegionDrawable
            TiledDrawable]))

(defmacro drawable
  [type & options]
  `(~(symbol (str u/main-package ".scenes.scene2d.u."
                  (u/key->pascal type) "Drawable."))
     ~@options))

(defmacro bitmap-font
  [& options]
  `(BitmapFont. ~@options))

(defmacro style
  [type & options]
  `(~(symbol (str u/main-package ".scenes.scene2d.ui."
                  (u/key->pascal type) "$"
                  (u/key->pascal type) "Style."))
     ~@options))

(defmacro skin
  [path & options]
  `(u/calls! ^Skin (Skin. (.internal ^Files (Gdx/files) ~path)) ~@options))

(defmacro align
  [key]
  `(u/static-field-lower :scenes :scene2d :utils :Align ~key))

(defmulti add-to-group! #(-> % first :object type) :default WidgetGroup)

(defmethod add-to-group! WidgetGroup
  [[{:keys [^WidgetGroup object]} children]]
  (doseq [child children]
    (.addActor object ^Actor (:object child))))

(defmethod add-to-group! Table
  [[{:keys [^Table object]} children]]
  (doseq [child children]
    (cond
      (map? child)
      (.add object ^Actor (:object child))
      (keyword? child)
      (case child
        :row (.row object)
        nil))))

(defn add!
  [group children]
  (add-to-group! [group children])
  group)

(defn ^:private create-group
  [^WidgetGroup group children]
  (-> (doto group
        (.setFillParent true))
      u/create-entity
      (add! children)))

; widgets

(defn check-box*
  [^String text arg]
  (u/create-entity (CheckBox. text arg)))

(defmacro check-box
  [text arg & options]
  `(let [entity# (check-box* ~text ~arg)]
     (u/calls! ^CheckBox (:object entity#) ~@options)
     entity#))

(defmacro check-box!
  [entity k & options]
  `(u/call! ^Checkbox (:object ~entity) ~k ~@options))

(defn dialog*
  [text arg]
  (u/create-entity (Dialog. text arg)))

(defmacro dialog
  [text arg & options]
  `(let [entity# (dialog* ~text ~arg)]
     (u/calls! ^Dialog (:object entity#) ~@options)
     entity#))

(defmacro dialog!
  [entity k & options]
  `(u/call! ^Dialog (:object ~entity) ~k ~@options))

(defn horizontal*
  [children]
  (create-group (HorizontalGroup.) children))

(defmacro horizontal
  [children & options]
  `(let [entity# (horizontal* ~children)]
     (u/calls! ^HorizontalGroup (:object entity#) ~@options)
     entity#))

(defmacro horizontal!
  [entity k & options]
  `(u/call! ^HorizontalGroup (:object ~entity) ~k ~@options))

(defn image*
  [arg]
  (u/create-entity
    (cond
      (map? arg)
      (Image. ^TextureRegion (:object arg))
      (string? arg)
      (Image. (Texture. ^String arg))
      :else
      (Image. arg))))

(defmacro image
  [arg & options]
  `(let [entity# (image* ~arg)]
     (u/calls! ^Image (:object entity#) ~@options)
     entity#))

(defmacro image!
  [entity k & options]
  `(u/call! ^Image (:object ~entity) ~k ~@options))

(defn image-button*
  [arg]
  (u/create-entity (ImageButton. arg)))

(defmacro image-button
  [arg & options]
  `(let [entity# (image-button* ~arg)]
     (u/calls! ^ImageButton (:object entity#) ~@options)
     entity#))

(defmacro image-button!
  [entity k & options]
  `(u/call! ^ImageButton (:object ~entity) ~k ~@options))

(defn image-text-button*
  [^String text arg]
  (u/create-entity (ImageTextButton. text arg)))

(defmacro image-text-button
  [text arg & options]
  `(let [entity# (image-text-button* ~text ~arg)]
     (u/calls! ^ImageTextButton (:object entity#) ~@options)
     entity#))

(defmacro image-text-button!
  [entity k & options]
  `(u/call! ^ImageTextButton (:object ~entity) ~k ~@options))

(defn label*
  [^String text arg]
  (u/create-entity
    (if (isa? (type arg) Color)
      (Label. text (style :label (bitmap-font) arg))
      (Label. text arg))))

(defmacro label
  [text arg & options]
  `(let [entity# (label* ~text ~arg)]
     (u/calls! ^Label (:object entity#) ~@options)
     entity#))

(defmacro label!
  [entity k & options]
  `(u/call! ^Label (:object ~entity) ~k ~@options))

(defn slider*
  [{:keys [min max step vertical?]
    :or {min 0 max 10 step 1 vertical? false}}
   arg]
  (u/create-entity
    (Slider. (float min) (float max) (float step) vertical? arg)))

(defmacro slider
  [attrs arg & options]
  `(let [entity# (slider* ~attrs ~arg)]
     (u/calls! ^Slider (:object entity#) ~@options)
     entity#))

(defmacro slider!
  [entity k & options]
  `(u/call! ^Slider (:object ~entity) ~k ~@options))

(defn table*
  [children]
  (create-group (Table.) children))

(defmacro table
  [children & options]
  `(let [entity# (table* ~children)]
     (u/calls! ^Table (:object entity#) ~@options)
     entity#))

(defmacro table!
  [entity k & options]
  `(u/call! ^Table (:object ~entity) ~k ~@options))

(defn text-button*
  [^String text arg]
  (u/create-entity (TextButton. text arg)))

(defmacro text-button
  [text arg & options]
  `(let [entity# (text-button* ~text ~arg)]
     (u/calls! ^TextButton (:object entity#) ~@options)
     entity#))

(defmacro text-button!
  [entity k & options]
  `(u/call! ^TextButton (:object ~entity) ~k ~@options))

(defn text-field*
  [^String text arg]
  (u/create-entity (TextField. text arg)))

(defmacro text-field
  [text arg & options]
  `(let [entity# (text-field* ~text ~arg)]
     (u/calls! ^TextField (:object entity#) ~@options)
     entity#))

(defmacro text-field!
  [entity k & options]
  `(u/call! ^TextField (:object ~entity) ~k ~@options))

(defn vertical*
  [children]
  (create-group (VerticalGroup.) children))

(defmacro vertical
  [children & options]
  `(let [entity# (vertical* ~children)]
     (u/calls! ^VerticalGroup (:object entity#) ~@options)
     entity#))

(defmacro vertical!
  [entity k & options]
  `(u/call! ^VerticalGroup (:object ~entity) ~k ~@options))

; listeners

(defn ^:private gesture-listener
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

(defn create-listeners
  [options execute-fn!]
  [(gesture-listener options execute-fn!)
   (change-listener options execute-fn!)
   (click-listener options execute-fn!)
   (drag-listener options execute-fn!)
   (focus-listener options execute-fn!)])
