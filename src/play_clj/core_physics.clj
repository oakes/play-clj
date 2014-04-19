(in-ns 'play-clj.core)

(defmulti step!
  "Runs the physics simulations for a single frame and optionally returns the
`entities` with their positions updated."
  (fn [screen & [entities]] (-> screen (u/get-obj :world) class)))

(defmulti add-body!
  "Adds the `body` to the `screen` for physics simulations and returns it. For
2D physics, `body` should be a `body-def`, whereas for 3D physics it should be a
`basic-body` or a `rigid-body`."
  (fn [screen body] (-> screen (u/get-obj :world) class)))

(defmulti add-joint!
  "Adds the `joint` to the `screen` for physics simulations and returns it. For
2D physics, `joint` should be a `joint-def`."
  (fn [screen joint] (-> screen (u/get-obj :world) class)))

(defmulti body-position!
  "Changes the position of the body in `entity`. For 2D physics, the arguments
should be x, y, and angle, whereas for 3D physics they should be x, y, and z."
  (fn [entity a1 a2 a3] (-> entity (u/get-obj :body) class)))

(defmulti body-x!
  "Changes the `x` of the body in `entity`."
  (fn [entity x] (-> entity (u/get-obj :body) class)))

(defmulti body-y!
  "Changes the `y` of the body in `entity`."
  (fn [entity y] (-> entity (u/get-obj :body) class)))

(defmulti body-z!
  "Changes the `z` of the body in `entity`. Only works with 3D physics."
  (fn [entity z] (-> entity (u/get-obj :body) class)))

(defmulti body-angle!
  "Changes the `angle` of the body in `entity`. Only works with 2D physics."
  (fn [entity angle] (-> entity (u/get-obj :body) class)))

(defmulti first-entity
  "Returns the first entity in a contact. May only be used in contact functions,
such as :on-begin-contact."
  (fn [screen entities] (-> screen (u/get-obj :world) class)))

(defmulti second-entity
  "Returns the second entity in a contact. May only be used in contact functions,
such as :on-begin-contact."
  (fn [screen entities] (-> screen (u/get-obj :world) class)))
