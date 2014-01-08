(in-ns 'play-clj.core)

(defmacro bitmap-font
  [& options]
  `(BitmapFont. ~@options))

(defmacro style
  [type & options]
  `(~(symbol (str utils/gdx-package "scenes.scene2d.ui."
                  (utils/key->class type) "$"
                  (utils/key->class type) "Style."))
     ~@options))

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
