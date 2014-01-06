(in-ns 'play-clj.core)

(defmacro color
  [& args]
  `~(if (keyword? (first args))
      `(Color. ^Color (utils/gdx-static-field :graphics :Color ~(first args)))
      `(Color. ~@args)))

(defn create-actor
  [actor]
  {:actor actor :x 0 :y 0})

(defn label
  [text color]
  (create-actor (Label. text (Label$LabelStyle. (BitmapFont.) color))))
