(in-ns 'play-clj.core)

(defn create-renderer!
  [{:keys [create-renderer-fn!]} & {:keys [] :as args}]
  (:renderer (create-renderer-fn! args)))

(defmulti renderer :type :default nil)

(defmethod renderer nil [opts])

(defmethod renderer :stage [_]
  (Stage.))

(defmacro label
  [& args]
  `(ui/label ~@args))
