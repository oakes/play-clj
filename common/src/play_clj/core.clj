(ns play-clj.core
  (:import [com.badlogic.gdx Game Screen]))

(defn set-screen!
  [^Game game ^Screen screen]
  (.setScreen game screen))

(defn defscreen*
  [{:keys [on-show on-render on-dispose on-hide on-pause on-resize on-resume]}]
  (let [total-time (atom 0)
        on-show (or on-show (fn []))
        on-render (or on-render (fn [d t]))
        on-dispose (or on-dispose (fn []))
        on-hide (or on-hide (fn []))
        on-pause (or on-pause (fn []))
        on-resize (or on-resize (fn [w h]))
        on-resume (or on-resume (fn []))]
    (proxy [Screen] []
      (show [] (on-show))
      (render [delta-time]
        (swap! total-time + delta-time)
        (on-render delta-time @total-time))
      (dispose [] (on-dispose))
      (hide [] (on-hide))
      (pause [] (on-pause))
      (resize [w h] (on-resize w h))
      (resume [] (on-resume)))))

(defmacro defscreen
  [name & {:keys [] :as options}]
  `(def ~name (defscreen* ~options)))

(defn create-game
  [{:keys [start-screen]}]
  (proxy [Game] []
    (create [] (when start-screen (set-screen! this start-screen)))))
