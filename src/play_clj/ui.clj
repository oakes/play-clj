(ns play-clj.ui
  (:require [play-clj.entities]
            [play-clj.g2d :as g2d]
            [play-clj.utils :as u])
  (:import [com.badlogic.gdx Files Gdx]
           [com.badlogic.gdx.graphics Color Texture]
           [com.badlogic.gdx.graphics.g2d TextureRegion]
           [com.badlogic.gdx.scenes.scene2d Actor Stage]
           [com.badlogic.gdx.scenes.scene2d.ui ButtonGroup Cell CheckBox
            Container Dialog HorizontalGroup Image ImageButton ImageTextButton
            Label ScrollPane SelectBox Skin Slider Stack Table TextButton
            TextField Tree Tree$Node VerticalGroup WidgetGroup Window]
           [com.badlogic.gdx.scenes.scene2d.utils ActorGestureListener
            ChangeListener ClickListener DragListener FocusListener
            NinePatchDrawable SpriteDrawable TextureRegionDrawable
            TiledDrawable]
           [play_clj.entities ActorEntity]))

(defmacro drawable
  "Returns a subclass of [BaseDrawable](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/utils/BaseDrawable.html).

    (drawable :texture-region)"
  [k & options]
  `(~(u/gdx :scenes :scene2d :utils
            (str (u/key->pascal k) "Drawable."))
     ~@options))

(defmacro style
  "Returns a style object whose class is determined by the keyword `k`.

    (style :check-box)"
  [k & options]
  `(~(u/gdx-class :scenes :scene2d :ui
                  (u/key->pascal k)
                  (str (u/key->pascal k) "Style."))
     ~@options))

(defn skin*
  [^String path]
  (or (u/load-asset path Skin)
      (Skin. (.internal ^Files (Gdx/files) path))))

(defmacro skin
  "Returns a [Skin](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/Skin.html)
based on the file at `path`.

    (skin \"uiskin.json\")"
  [path & options]
  `(u/calls! ^Skin (skin* ~path) ~@options))

(defmacro skin!
  "Calls a single method on a `skin`."
  [object k & options]
  `(u/call! ^Skin ~object ~k ~@options))

(defmacro align
  "Returns a static field from [Align](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/utils/Align.html).

    (align :center)"
  [k]
  (u/gdx-field :utils :Align (u/key->camel k)))

(defn cell!
  "Calls methods on a [Cell](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/Cell.html)."
  [^Cell cell & args]
  (let [method (first args)
        [[a1 a2 a3 a4] rest-args] (split-with #(not (keyword? %)) (rest args))]
    (when method
      (case method
        :width (.width cell ^double a1)
        :height (.height cell ^double a1)
        :size (.size cell ^double a1 ^double a2)
        :min-width (.minWidth cell ^double a1)
        :min-height (.minHeight cell ^double a1)
        :min-size (.minSize cell ^double a1 ^double a2)
        :max-width (.maxWidth cell ^double a1)
        :max-height (.maxHeight cell ^double a1)
        :max-size (.minSize cell ^double a1 ^double a2)
        :space (.space cell ^double a1 ^double a2 ^double a3 ^double a4)
        :space-top (.spaceTop cell ^double a1)
        :space-left (.spaceLeft cell ^double a1)
        :space-bottom (.spaceBottom cell ^double a1)
        :space-right (.spaceRight cell ^double a1)
        :pad (.pad cell ^double a1 ^double a2 ^double a3 ^double a4)
        :pad-top (.padTop cell ^double a1)
        :pad-left (.padLeft cell ^double a1)
        :pad-bottom (.padBottom cell ^double a1)
        :pad-right (.padRight cell ^double a1)
        :fill (.fill cell ^boolean a1 ^boolean a2)
        :fill-x (.fillX cell)
        :fill-y (.fillY cell)
        :align (.align cell (int a1))
        :center (.center cell)
        :top (.top cell)
        :left (.left cell)
        :bottom (.bottom cell)
        :right (.right cell)
        :expand (.expand cell ^boolean a1 ^boolean a2)
        :expand-x (.expandX cell)
        :expand-y (.expandY cell)
        :ignore (.ignore cell ^boolean a1)
        :colspan (.colspan cell (int a1))
        :uniform (.uniform cell ^boolean a1 ^boolean a2)
        :uniform-x (.uniformX cell)
        :uniform-y (.uniformY cell)
        :row (.row cell)
        (u/throw-key-not-found method))
      (apply cell! cell rest-args))
    cell))

(defmulti add-to-group!
  #(-> % first :object type) :default WidgetGroup)

(defn ^:private create-tree-node
  [child]
  {:object (Tree$Node. ^Actor (:object child))})

(defn ^:private add-tree-nodes
  [parent children]
  (when-let [node (add-to-group! [parent (first children)])]
    (doseq [child (rest children)]
      (add-to-group! [node child]))
    node))

(defmethod add-to-group! WidgetGroup
  [[parent child]]
  (.addActor ^WidgetGroup (:object parent) ^Actor (:object child)))

(defmethod add-to-group! Table
  [[parent child]]
  (cond
    (map? child)
    (.add ^Table (:object parent) ^Actor (:object child))
    (coll? child)
    (apply cell! (add-to-group! [parent (first child)]) (rest child))
    (keyword? child)
    (case child
      :row (.row ^Table (:object parent))
      (u/throw-key-not-found child))))

(defmethod add-to-group! Tree
  [[parent child]]
  (cond
    (map? child)
    (let [node (create-tree-node child)]
      (.add ^Tree (:object parent) ^Tree$Node (:object node))
      node)
    (coll? child)
    (add-tree-nodes parent child)))

(defmethod add-to-group! Tree$Node
  [[parent child]]
  (cond
    (map? child)
    (let [node (create-tree-node child)]
      (.add ^Tree$Node (:object parent) ^Tree$Node (:object node))
      node)
    (coll? child)
    (add-tree-nodes parent child)))

(defn add!
  "Adds the entities in `children` to the `group` entity of type [WidgetGroup](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/WidgetGroup.html)."
  [group & children]
  (doseq [child children]
    (add-to-group! [group child]))
  group)

(defn ^:private create-group
  [^WidgetGroup group children]
  (apply add! (ActorEntity. group) children))

; actor

(defmacro actor!
  "Calls a single method on an actor."
  [entity k & options]
  `(u/call! ^Actor (u/get-obj ~entity :object) ~k ~@options))

(defn actor?
  "Returns true if `entity` is an actor."
  [entity]
  (instance? Actor (u/get-obj entity :object)))

; check-box

(defn check-box*
  [^String text arg]
  (ActorEntity. (CheckBox. text arg)))

(defmacro check-box
  "Returns an entity based on [CheckBox](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/CheckBox.html).

    (check-box \"I'm a check box\" (style :check-box off on font color))
    (check-box \"I'm a check box\" (style :check-box nil nil (bitmap-font) (color :white)))
    (check-box \"I'm a check box\" (skin \"uiskin.json\"))"
  [text arg & options]
  `(let [entity# (check-box* ~text ~arg)]
     (u/calls! ^CheckBox (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro check-box!
  "Calls a single method on a `check-box`."
  [entity k & options]
  `(u/call! ^CheckBox (u/get-obj ~entity :object) ~k ~@options))

(defn check-box?
  "Returns true if `entity` is a `check-box`."
  [entity]
  (instance? CheckBox (u/get-obj entity :object)))

; container

(defn container*
  [child]
  (ActorEntity. (Container. (u/get-obj child :object))))

(defmacro container
  "Returns an entity based on [Container](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/Container.html).

    (container entity)"
  [child & options]
  `(let [entity# (container* ~child)]
     (u/calls! ^Container (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro container!
  "Calls a single method on a `container`."
  [entity k & options]
  `(u/call! ^Container (u/get-obj ~entity :object) ~k ~@options))

(defn container?
  "Returns true if `entity` is a `container`."
  [entity]
  (instance? Container (u/get-obj entity :object)))

; dialog

(defn dialog*
  [text arg]
  (ActorEntity. (Dialog. text arg)))

(defmacro dialog
  "Returns an entity based on [Dialog](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/Dialog.html).

    (dialog \"I'm a dialog\" (style :window font font-color background))
    (dialog \"I'm a dialog\" (style :window (bitmap-font) (color :white) nil))
    (dialog \"I'm a dialog\" (skin \"uiskin.json\"))"
  [text arg & options]
  `(let [entity# (dialog* ~text ~arg)]
     (u/calls! ^Dialog (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro dialog!
  "Calls a single method on a `dialog`."
  [entity k & options]
  `(u/call! ^Dialog (u/get-obj ~entity :object) ~k ~@options))

(defn dialog?
  "Returns true if `entity` is a `dialog`."
  [entity]
  (instance? Dialog (u/get-obj entity :object)))

; horizontal

(defn horizontal*
  [children]
  (create-group (HorizontalGroup.) children))

(defmacro horizontal
  "Returns an entity based on [HorizontalGroup](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/HorizontalGroup.html).

    (horizontal [entity-1 entity-2])"
  [children & options]
  `(let [entity# (horizontal* ~children)]
     (u/calls! ^HorizontalGroup (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro horizontal!
  "Calls a single method on a `horizontal`."
  [entity k & options]
  `(u/call! ^HorizontalGroup (u/get-obj ~entity :object) ~k ~@options))

(defn horizontal?
  "Returns true if `entity` is a `horizontal`."
  [entity]
  (instance? HorizontalGroup (u/get-obj entity :object)))

; image

(defn image*
  [arg]
  (ActorEntity.
    (cond
      (map? arg)
      (Image. ^TextureRegion (:object arg))
      (string? arg)
      (Image. (or (u/load-asset arg Texture)
                  (Texture. ^String arg)))
      :else
      (Image. arg))))

(defmacro image
  "Returns an entity based on [Image](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/Image.html).

    (image \"image.png\")"
  [arg & options]
  `(let [entity# (image* ~arg)]
     (u/calls! ^Image (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro image!
  "Calls a single method on a `image`."
  [entity k & options]
  `(u/call! ^Image (u/get-obj ~entity :object) ~k ~@options))

(defn image?
  "Returns true if `entity` is an `image`."
  [entity]
  (instance? Image (u/get-obj entity :object)))

; image-button

(defn image-button*
  [arg]
  (ActorEntity. (ImageButton. arg)))

(defmacro image-button
  "Returns an entity based on [ImageButton](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/ImageButton.html).

    (image-button (style :image-button up dn check img-up img-dn img-check))
    (image-button (style :image-button nil nil nil nil nil nil))
    (image-button (skin \"uiskin.json\"))"
  [arg & options]
  `(let [entity# (image-button* ~arg)]
     (u/calls! ^ImageButton (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro image-button!
  "Calls a single method on a `image-button`."
  [entity k & options]
  `(u/call! ^ImageButton (u/get-obj ~entity :object) ~k ~@options))

(defn image-button?
  "Returns true if `entity` is an `image-button`."
  [entity]
  (instance? ImageButton (u/get-obj entity :object)))

; image-text-button

(defn image-text-button*
  [^String text arg]
  (ActorEntity. (ImageTextButton. text arg)))

(defmacro image-text-button
  "Returns an entity based on [ImageTextButton](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/ImageTextButton.html).

    (image-text-button \"I'm an image text button\"
                       (style :image-text-button up down checked font))
    (image-text-button \"I'm an image text button\"
                       (style :image-text-button nil nil nil (bitmap-font)))
    (image-text-button \"I'm an image text button\" (skin \"uiskin.json\"))"
  [text arg & options]
  `(let [entity# (image-text-button* ~text ~arg)]
     (u/calls! ^ImageTextButton (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro image-text-button!
  "Calls a single method on a `image-text-button`."
  [entity k & options]
  `(u/call! ^ImageTextButton (u/get-obj ~entity :object) ~k ~@options))

(defn image-text-button?
  "Returns true if `entity` is a `image-text-button`."
  [entity]
  (instance? ImageTextButton (u/get-obj entity :object)))

; label

(defn label*
  [^String text arg]
  (ActorEntity.
    (if (instance? Color arg)
      (Label. text (style :label (g2d/bitmap-font) arg))
      (Label. text arg))))

(defmacro label
  "Returns an entity based on [Label](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/Label.html).

    (label \"I'm a label\" (color :white))
    (label \"I'm a label\" (style :label (bitmap-font) (color :white)))
    (label \"I'm a label\" (skin \"uiskin.json\"))"
  [text arg & options]
  `(let [entity# (label* ~text ~arg)]
     (u/calls! ^Label (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro label!
  "Calls a single method on a `label`."
  [entity k & options]
  `(u/call! ^Label (u/get-obj ~entity :object) ~k ~@options))

(defn label?
  "Returns true if `entity` is a `label`."
  [entity]
  (instance? Label (u/get-obj entity :object)))

; scroll-pane

(defn scroll-pane*
  [child arg]
  (ActorEntity. (ScrollPane. (u/get-obj child :object) arg)))

(defmacro scroll-pane
  "Returns an entity based on [ScrollPane](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/ScrollPane.html).

    (scroll-pane table-entity (style :scroll-pane back h h-knob v v-knob))
    (scroll-pane table-entity (style :scroll-pane nil nil nil nil nil))
    (scroll-pane table-entity (skin \"uiskin.json\"))"
  [child arg & options]
  `(let [entity# (scroll-pane* ~child ~arg)]
     (u/calls! ^ScrollPane (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro scroll-pane!
  "Calls a single method on a `scroll-pane`."
  [entity k & options]
  `(u/call! ^ScrollPane (u/get-obj ~entity :object) ~k ~@options))

(defn scroll-pane?
  "Returns true if `entity` is a `scroll-pane`."
  [entity]
  (instance? ScrollPane (u/get-obj entity :object)))

; select-box

(defn select-box*
  [arg]
  (ActorEntity. (SelectBox. arg)))

(defmacro select-box
  "Returns an entity based on [SelectBox](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/SelectBox.html).

    (select-box (style :select-box font color back scroll-style list-style)
                :set-items (into-array [\"Item 1\" \"Item 2\" \"Item 3\"]))
    (select-box (style :select-box (bitmap-font) (color :white) nil
                       (style :scroll-pane nil nil nil nil nil)
                       (style :list (bitmap-font) (color :white) (color :black) nil))
                :set-items (into-array [\"Item 1\" \"Item 2\" \"Item 3\"]))
    (select-box (skin \"uiskin.json\")
                :set-items (into-array [\"Item 1\" \"Item 2\" \"Item 3\"]))"
  [arg & options]
  `(let [entity# (select-box* ~arg)]
     (u/calls! ^SelectBox (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro select-box!
  "Calls a single method on a `select-box`."
  [entity k & options]
  `(u/call! ^SelectBox (u/get-obj ~entity :object) ~k ~@options))

(defn select-box?
  "Returns true if `entity` is a `select-box`."
  [entity]
  (instance? SelectBox (u/get-obj entity :object)))

; slider

(defn slider*
  [{:keys [min max step vertical?]
    :or {min 0 max 10 step 1 vertical? false}}
   arg]
  (ActorEntity.
    (Slider. (float min) (float max) (float step) vertical? arg)))

(defmacro slider
  "Returns an entity based on [Slider](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/Slider.html).

    (slider {:min 0 :max 10 :step 1 :vertical? false} (style :slider back knob))
    (slider {:min 0 :max 10 :step 1 :vertical? false} (style :slider nil nil))
    (slider {:min 0 :max 10 :step 1 :vertical? false} (skin \"uiskin.json\"))"
  [attrs arg & options]
  `(let [entity# (slider* ~attrs ~arg)]
     (u/calls! ^Slider (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro slider!
  "Calls a single method on a `slider`."
  [entity k & options]
  `(u/call! ^Slider (u/get-obj ~entity :object) ~k ~@options))

(defn slider?
  "Returns true if `entity` is a `slider`."
  [entity]
  (instance? Slider (u/get-obj entity :object)))

; stack

(defn stack*
  [children]
  (create-group (Stack.) children))

(defmacro stack
  "Returns an entity based on [Stack](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/Stack.html).

    (stack [entity-1 entity-2])"
  [children & options]
  `(let [entity# (stack* ~children)]
     (u/calls! ^Stack (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro stack!
  "Calls a single method on a `stack`."
  [entity k & options]
  `(u/call! ^Stack (u/get-obj ~entity :object) ~k ~@options))

(defn stack?
  "Returns true if `entity` is a `stack`."
  [entity]
  (instance? Stack (u/get-obj entity :object)))

; table

(defn table*
  [children]
  (create-group (Table.) children))

(defmacro table
  "Returns an entity based on [Table](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/Table.html).

    (table [entity-1 entity-2])"
  [children & options]
  `(let [entity# (table* ~children)]
     (u/calls! ^Table (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro table!
  "Calls a single method on a `table`."
  [entity k & options]
  `(u/call! ^Table (u/get-obj ~entity :object) ~k ~@options))

(defn table?
  "Returns true if `entity` is a `table`."
  [entity]
  (instance? Table (u/get-obj entity :object)))

; text-button

(defn text-button*
  [^String text arg]
  (ActorEntity. (TextButton. text arg)))

(defmacro text-button
  "Returns an entity based on [TextButton](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/TextButton.html).

    (text-button \"I'm a text button\" (style :text-button up dn checked font))
    (text-button \"I'm a text button\" (style :text-button nil nil nil (bitmap-font)))
    (text-button \"I'm a text button\" (skin \"uiskin.json\"))"
  [text arg & options]
  `(let [entity# (text-button* ~text ~arg)]
     (u/calls! ^TextButton (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro text-button!
  "Calls a single method on a `text-button`."
  [entity k & options]
  `(u/call! ^TextButton (u/get-obj ~entity :object) ~k ~@options))

(defn text-button?
  "Returns true if `entity` is a `text-button`."
  [entity]
  (instance? TextButton (u/get-obj entity :object)))

; text-field

(defn text-field*
  [^String text arg]
  (ActorEntity. (TextField. text arg)))

(defmacro text-field
  "Returns an entity based on [TextField](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/TextField.html).

    (text-field \"I'm a text field\" (style :text-field font color cur sel back))
    (text-field \"I'm a text field\" (style :text-field (bitmap-font) (color :white) nil nil nil))
    (text-field \"I'm a text field\" (skin \"uiskin.json\"))"
  [text arg & options]
  `(let [entity# (text-field* ~text ~arg)]
     (u/calls! ^TextField (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro text-field!
  "Calls a single method on a `text-field`."
  [entity k & options]
  `(u/call! ^TextField (u/get-obj ~entity :object) ~k ~@options))

(defn text-field?
  "Returns true if `entity` is a `text-field`."
  [entity]
  (instance? TextField (u/get-obj entity :object)))

; tree

(defn tree*
  [children arg]
  (create-group (Tree. arg) children))

(defmacro tree
  "Returns an entity based on [Tree](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/Tree.html).

    (tree [entity-1 entity-2] (style :tree plus minus selection))
    (tree [entity-1 entity-2] (style :tree nil nil nil))
    (tree [entity-1 entity-2] (skin \"uiskin.json\"))"
  [children arg & options]
  `(let [entity# (tree* ~children ~arg)]
     (u/calls! ^Tree (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro tree!
  "Calls a single method on a `tree`."
  [entity k & options]
  `(u/call! ^Tree (u/get-obj ~entity :object) ~k ~@options))

(defn tree?
  "Returns true if `entity` is a `tree`."
  [entity]
  (instance? Tree (u/get-obj entity :object)))

; vertical

(defn vertical*
  [children]
  (create-group (VerticalGroup.) children))

(defmacro vertical
  "Returns an entity based on [VerticalGroup](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/VerticalGroup.html).

    (vertical [entity-1 entity-2])"
  [children & options]
  `(let [entity# (vertical* ~children)]
     (u/calls! ^VerticalGroup (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro vertical!
  "Calls a single method on a `vertical`."
  [entity k & options]
  `(u/call! ^VerticalGroup (u/get-obj ~entity :object) ~k ~@options))

(defn vertical?
  "Returns true if `entity` is a `vertical`."
  [entity]
  (instance? VerticalGroup (u/get-obj entity :object)))

; window

(defn window*
  [children ^String title arg]
  (create-group (Window. title arg) children))

(defmacro window
  "Returns an entity based on [Window](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/Window.html).

    (window [entity-1 entity-2] \"I'm a window\" (style :window font color background))
    (window [entity-1 entity-2] \"I'm a window\" (style :window (bitmap-font) (color :white) nil))
    (window [entity-1 entity-2] \"I'm a window\" (skin \"uiskin.json\"))"
  [children title arg & options]
  `(let [entity# (window* ~children ~title ~arg)]
     (u/calls! ^Window (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro window!
  "Calls a single method on a `window`."
  [entity k & options]
  `(u/call! ^Window (u/get-obj ~entity :object) ~k ~@options))

(defn window?
  "Returns true if `entity` is a `window`."
  [entity]
  (instance? Window (u/get-obj entity :object)))

; listeners

(defmacro actor-gesture-listener!
  "Calls a single method on the [ActorGestureListener](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/utils/ActorGestureListener.html)
in the `screen`."
  [screen k & options]
  `(let [listeners# (u/get-obj ~screen :ui-listeners)
         ^ActorGestureListener object#
         (u/get-obj listeners# :actor-gesture-listener)]
     (u/call! object# ~k ~@options)))

(defmacro change-listener!
  "Calls a single method on the [ChangeListener](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/utils/ChangeListener.html)
in the `screen`."
  [screen k & options]
  `(let [listeners# (u/get-obj ~screen :ui-listeners)
         ^ChangeListener object# (u/get-obj listeners# :change-listener)]
     (u/call! object# ~k ~@options)))

(defmacro click-listener!
  "Calls a single method on the [ClickListener](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/utils/ClickListener.html)
in the `screen`."
  [screen k & options]
  `(let [listeners# (u/get-obj ~screen :ui-listeners)
         ^ClickListener object# (u/get-obj listeners# :click-listener)]
     (u/call! object# ~k ~@options)))

(defmacro drag-listener!
  "Calls a single method on the [DragListener](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/utils/DragListener.html)
in the `screen`."
  [screen k & options]
  `(let [listeners# (u/get-obj ~screen :ui-listeners)
         ^DragListener object# (u/get-obj listeners# :drag-listener)]
     (u/call! object# ~k ~@options)))

(defmacro focus-listener!
  "Calls a single method on the [FocusListener](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/utils/FocusListener.html)
in the `screen`."
  [screen k & options]
  `(let [listeners# (u/get-obj ~screen :ui-listeners)
         ^FocusListener object# (u/get-obj listeners# :focus-listener)]
     (u/call! object# ~k ~@options)))
