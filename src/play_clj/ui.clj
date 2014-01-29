(ns play-clj.ui
  (:require [play-clj.g2d :as g2d]
            [play-clj.utils :as u])
  (:import [com.badlogic.gdx Files Gdx]
           [com.badlogic.gdx.graphics Color Texture]
           [com.badlogic.gdx.graphics.g2d TextureRegion]
           [com.badlogic.gdx.scenes.scene2d Actor Stage]
           [com.badlogic.gdx.scenes.scene2d.ui ButtonGroup CheckBox Dialog
            HorizontalGroup Image ImageButton ImageTextButton Label ScrollPane
            SelectBox Skin Slider Stack Table TextButton TextField Tree
            Tree$Node VerticalGroup WidgetGroup Window]
           [com.badlogic.gdx.scenes.scene2d.utils NinePatchDrawable
            SpriteDrawable TextureRegionDrawable TiledDrawable]
           [com.esotericsoftware.tablelayout Cell]))

(defmacro drawable
  "Returns a subclass of [BaseDrawable](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/utils/BaseDrawable.html)

    (drawable :texture-region)"
  [k & options]
  `(~(symbol (str u/main-package ".scenes.scene2d.utils."
                  (u/key->pascal k) "Drawable."))
     ~@options))

(defmacro style
  "Returns a style object whose class is determined by the keyword `k`

    (style :check-box)"
  [k & options]
  `(~(symbol (str u/main-package ".scenes.scene2d.ui."
                  (u/key->pascal k) "$"
                  (u/key->pascal k) "Style."))
     ~@options))

(defmacro skin
  "Returns a [Skin](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/Skin.html)
based on the file at `path`

    (skin \"uiskin.json\")"
  [path & options]
  `(u/calls! ^Skin (Skin. (.internal ^Files (Gdx/files) ~path)) ~@options))

(defmacro align
  "Returns a static field from [Align](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/utils/Align.html)

    (align :center)"
  [k]
  `(u/static-lower :scenes :scene2d :utils :Align ~k))

(defn cell!
  "Calls a single method on a [Cell](https://github.com/libgdx/libgdx/blob/master/gdx/src/com/esotericsoftware/tablelayout/Cell.java)
(this could probably be made into a macro...)"
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
  "Internal use only"
  #(-> % first :object type) :default WidgetGroup)

(defn ^:private create-tree-node
  "Internal use only"
  [child]
  {:object (Tree$Node. ^Actor (:object child))})

(defn ^:private add-tree-nodes
  "Internal use only"
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
  "Adds the entities in `children` to the `group` entity of type [WidgetGroup](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/WidgetGroup.html)"
  [group & children]
  (doseq [child children]
    (add-to-group! [group child]))
  group)

(defn ^:private create-group
  "Internal use only"
  [^WidgetGroup group children]
  (apply add! (u/create-entity group) children))

; check-box

(defn check-box*
  "The function version of `check-box`"
  [^String text arg]
  (u/create-entity (CheckBox. text arg)))

(defmacro check-box
  "Returns an entity based on [CheckBox](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/CheckBox.html)

    (check-box \"I'm a check box\" (style :check-box off on font color))
    (check-box \"I'm a check box\" (skin \"uiskin.json\"))"
  [text arg & options]
  `(let [entity# (check-box* ~text ~arg)]
     (u/calls! ^CheckBox (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro check-box!
  "Calls a single method on a `check-box`"
  [entity k & options]
  `(u/call! ^Checkbox (u/get-obj ~entity :object) ~k ~@options))

; dialog

(defn dialog*
  "The function version of `dialog`"
  [text arg]
  (u/create-entity (Dialog. text arg)))

(defmacro dialog
  "Returns an entity based on [Dialog](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/Dialog.html)

    (dialog \"I'm a dialog\" (style :window font font-color background))
    (dialog \"I'm a dialog\" (skin \"uiskin.json\"))"
  [text arg & options]
  `(let [entity# (dialog* ~text ~arg)]
     (u/calls! ^Dialog (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro dialog!
  "Calls a single method on a `dialog`"
  [entity k & options]
  `(u/call! ^Dialog (u/get-obj ~entity :object) ~k ~@options))

; horizontal

(defn horizontal*
  "The function version of `horizontal`"
  [children]
  (create-group (HorizontalGroup.) children))

(defmacro horizontal
  "Returns an entity based on [HorizontalGroup](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/HorizontalGroup.html)

    (horizontal [entity-1 entity-2])"
  [children & options]
  `(let [entity# (horizontal* ~children)]
     (u/calls! ^HorizontalGroup (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro horizontal!
  "Calls a single method on a `horizontal`"
  [entity k & options]
  `(u/call! ^HorizontalGroup (u/get-obj ~entity :object) ~k ~@options))

; image

(defn image*
  "The function version of `image`"
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
  "Returns an entity based on [Image](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/Image.html)

    (image \"image.png\")"
  [arg & options]
  `(let [entity# (image* ~arg)]
     (u/calls! ^Image (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro image!
  "Calls a single method on a `image`"
  [entity k & options]
  `(u/call! ^Image (u/get-obj ~entity :object) ~k ~@options))

; image-button

(defn image-button*
  "The function version of `image-button`"
  [arg]
  (u/create-entity (ImageButton. arg)))

(defmacro image-button
  "Returns an entity based on [ImageButton](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/ImageButton.html)

    (image-button (style :image-button up dn check img-up img-dn img-check))
    (image-button (skin \"uiskin.json\"))"
  [arg & options]
  `(let [entity# (image-button* ~arg)]
     (u/calls! ^ImageButton (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro image-button!
  "Calls a single method on a `image-button`"
  [entity k & options]
  `(u/call! ^ImageButton (u/get-obj ~entity :object) ~k ~@options))

; image-text-button

(defn image-text-button*
  "The function version of `image-text-button`"
  [^String text arg]
  (u/create-entity (ImageTextButton. text arg)))

(defmacro image-text-button
  "Returns an entity based on [ImageTextButton](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/ImageTextButton.html)

    (image-text-button \"I'm an image text button\"
                       (style :image-text-button up down checked font))
    (image-text-button \"I'm an image text button\" (skin \"uiskin.json\"))"
  [text arg & options]
  `(let [entity# (image-text-button* ~text ~arg)]
     (u/calls! ^ImageTextButton (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro image-text-button!
  "Calls a single method on a `image-text-button`"
  [entity k & options]
  `(u/call! ^ImageTextButton (u/get-obj ~entity :object) ~k ~@options))

; label

(defn label*
  "The function version of `label`"
  [^String text arg]
  (u/create-entity
    (if (isa? (type arg) Color)
      (Label. text (style :label (g2d/bitmap-font) arg))
      (Label. text arg))))

(defmacro label
  "Returns an entity based on [Label](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/Label.html)

    (label \"I'm a label\" (color :white))
    (label \"I'm a label\" (style :label (g2d/bitmap-font) (color :white)))
    (label \"I'm a label\" (skin \"uiskin.json\"))"
  [text arg & options]
  `(let [entity# (label* ~text ~arg)]
     (u/calls! ^Label (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro label!
  "Calls a single method on a `label`"
  [entity k & options]
  `(u/call! ^Label (u/get-obj ~entity :object) ~k ~@options))

; scroll-pane

(defn scroll-pane*
  "The function version of `scroll-pane`"
  [child arg]
  (u/create-entity (ScrollPane. (u/get-obj child :object) arg)))

(defmacro scroll-pane
  "Returns an entity based on [ScrollPane](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/ScrollPane.html)

    (scroll-pane table-entity (style :scroll-pane back h h-knob v v-knob))
    (scroll-pane table-entity (skin \"uiskin.json\"))"
  [child arg & options]
  `(let [entity# (scroll-pane* ~child ~arg)]
     (u/calls! ^ScrollPane (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro scroll-pane!
  "Calls a single method on a `scroll-pane`"
  [entity k & options]
  `(u/call! ^ScrollPane (u/get-obj ~entity :object) ~k ~@options))

; select-box

(defn select-box*
  "The function version of `select-box`"
  [items arg]
  (u/create-entity (SelectBox. (into-array items) arg)))

(defmacro select-box
  "Returns an entity based on [SelectBox](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/SelectBox.html)

    (select-box [\"Item 1\" \"Item 2\" \"Item 3\"]
                (style :select-box font color back scroll-style list-style))
    (select-box [\"Item 1\" \"Item 2\" \"Item 3\"] (skin \"uiskin.json\"))"
  [items arg & options]
  `(let [entity# (select-box* ~items ~arg)]
     (u/calls! ^SelectBox (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro select-box!
  "Calls a single method on a `select-box`"
  [entity k & options]
  `(u/call! ^SelectBox (u/get-obj ~entity :object) ~k ~@options))

; slider

(defn slider*
  "The function version of `slider`"
  [{:keys [min max step vertical?]
    :or {min 0 max 10 step 1 vertical? false}}
   arg]
  (u/create-entity
    (Slider. (float min) (float max) (float step) vertical? arg)))

(defmacro slider
  "Returns an entity based on [Slider](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/Slider.html)

    (slider {:min 0 :max 10 :step 1 :vertical? false} (style :slider back knob))
    (slider {:min 0 :max 10 :step 1 :vertical? false} (skin \"uiskin.json\"))"
  [attrs arg & options]
  `(let [entity# (slider* ~attrs ~arg)]
     (u/calls! ^Slider (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro slider!
  "Calls a single method on a `slider`"
  [entity k & options]
  `(u/call! ^Slider (u/get-obj ~entity :object) ~k ~@options))

; stack

(defn stack*
  "The function version of `stack`"
  [children]
  (create-group (Stack.) children))

(defmacro stack
  "Returns an entity based on [Stack](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/Stack.html)

    (stack [entity-1 entity-2])"
  [children & options]
  `(let [entity# (stack* ~children)]
     (u/calls! ^Stack (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro stack!
  "Calls a single method on a `stack`"
  [entity k & options]
  `(u/call! ^Stack (u/get-obj ~entity :object) ~k ~@options))

; table

(defn table*
  "The function version of `table`"
  [children]
  (create-group (Table.) children))

(defmacro table
  "Returns an entity based on [Table](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/Table.html)

    (table [entity-1 entity-2])"
  [children & options]
  `(let [entity# (table* ~children)]
     (u/calls! ^Table (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro table!
  "Calls a single method on a `table`"
  [entity k & options]
  `(u/call! ^Table (u/get-obj ~entity :object) ~k ~@options))

; text-button

(defn text-button*
  "The function version of `text-button`"
  [^String text arg]
  (u/create-entity (TextButton. text arg)))

(defmacro text-button
  "Returns an entity based on [TextButton](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/TextButton.html)

    (text-button \"I'm a text button\" (style :text-button up dn checked font))
    (text-button \"I'm a text button\" (skin \"uiskin.json\"))"
  [text arg & options]
  `(let [entity# (text-button* ~text ~arg)]
     (u/calls! ^TextButton (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro text-button!
  "Calls a single method on a `text-button`"
  [entity k & options]
  `(u/call! ^TextButton (u/get-obj ~entity :object) ~k ~@options))

; text-field

(defn text-field*
  "The function version of `text-field`"
  [^String text arg]
  (u/create-entity (TextField. text arg)))

(defmacro text-field
  "Returns an entity based on [TextField](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/TextField.html)

    (text-field \"I'm a text field\" (style :text-field font col cur sel back))
    (text-field \"I'm a text field\" (skin \"uiskin.json\"))"
  [text arg & options]
  `(let [entity# (text-field* ~text ~arg)]
     (u/calls! ^TextField (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro text-field!
  "Calls a single method on a `text-field`"
  [entity k & options]
  `(u/call! ^TextField (u/get-obj ~entity :object) ~k ~@options))

; tree

(defn tree*
  "The function version of `tree`"
  [children arg]
  (create-group (Tree. arg) children))

(defmacro tree
  "Returns an entity based on [Tree](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/Tree.html)

    (tree [entity-1 entity-2] (style :tree plus minus selection))
    (tree [entity-1 entity-2] (skin \"uiskin.json\"))"
  [children arg & options]
  `(let [entity# (tree* ~children ~arg)]
     (u/calls! ^Tree (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro tree!
  "Calls a single method on a `tree`"
  [entity k & options]
  `(u/call! ^Tree (u/get-obj ~entity :object) ~k ~@options))

; vertical

(defn vertical*
  "The function version of `vertical`"
  [children]
  (create-group (VerticalGroup.) children))

(defmacro vertical
  "Returns an entity based on [VerticalGroup](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/VerticalGroup.html)

    (vertical [entity-1 entity-2])"
  [children & options]
  `(let [entity# (vertical* ~children)]
     (u/calls! ^VerticalGroup (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro vertical!
  "Calls a single method on a `vertical`"
  [entity k & options]
  `(u/call! ^VerticalGroup (u/get-obj ~entity :object) ~k ~@options))

; window

(defn window*
  "The function version of `window`"
  [children ^String title arg]
  (create-group (Window. title arg) children))

(defmacro window
  "Returns an entity based on [Window](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/scenes/scene2d/ui/Window.html)

    (window [entity-1 entity-2] \"I'm a window\" (style :window title font col))
    (window [entity-1 entity-2] \"I'm a window\" (skin \"uiskin.json\"))"
  [children title arg & options]
  `(let [entity# (window* ~children ~title ~arg)]
     (u/calls! ^Window (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro window!
  "Calls a single method on a `window`"
  [entity k & options]
  `(u/call! ^Window (u/get-obj ~entity :object) ~k ~@options))
