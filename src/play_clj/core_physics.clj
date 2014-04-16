(in-ns 'play-clj.core)

(defmulti step!
  "Runs the physics simulations for a single frame and optionally returns the
`entities` with their positions updated."
  (fn [screen & [entities]] (-> screen (u/get-obj :world) class)))

(defmulti add-body!
  "Adds the `body` to the `screen` for physics simulations and returns it."
  (fn [screen body] (-> screen (u/get-obj :world) class)))

(defmulti body-position!
  "Changes the position of the body in `entity`."
  (fn [entity a1 a2 a3] (-> entity (u/get-obj :body) class)))

(defmulti body-x!
  "Changes the `x` of the body in `entity`."
  (fn [entity x] (-> entity (u/get-obj :body) class)))

(defmulti body-y!
  "Changes the `y` of the body in `entity`."
  (fn [entity y] (-> entity (u/get-obj :body) class)))

(defmulti body-z!
  "Changes the `z` of the body in `entity`."
  (fn [entity z] (-> entity (u/get-obj :body) class)))

(defmulti body-angle!
  "Changes the `angle` of the body in `entity`."
  (fn [entity angle] (-> entity (u/get-obj :body) class)))
