(ns play-clj.g2d
  (:require [play-clj.utils :as u])
  (:import [com.badlogic.gdx.graphics Texture]
           [com.badlogic.gdx.graphics.g2d Animation BitmapFont TextureRegion]))

(defmacro bitmap-font
  "Returns a [BitmapFont](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g2d/BitmapFont.html)

    (bitmap-font)
    (bitmap-font file-handle region)
"
  [& options]
  `(BitmapFont. ~@options))

(defn texture*
  "The function version of `texture`"
  [arg]
  (u/create-entity
    (cond
      (string? arg)
      (-> ^String arg Texture. TextureRegion.)
      (map? arg)
      (TextureRegion. ^TextureRegion (u/get-obj arg :object))
      :else
      arg)))

(defmacro texture
  "Returns an entity based on [TextureRegion](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g2d/TextureRegion.html)

    (texture \"image.png\")
    (texture \"image.png\" :flip true false)
    (texture \"image.png\"
             :flip true false
             :set-region 0 0 100 100)
"
  [arg & options]
  `(let [entity# (texture* ~arg)]
     (u/calls! ^TextureRegion (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro texture!
  "Calls a single method on a `texture`

    (texture! entity :flip true false)
    (texture! entity :get-region-width)
"
  [entity k & options]
  `(u/call! ^TextureRegion (u/get-obj ~entity :object) ~k ~@options))

(defmacro play-mode
  "Returns a static field from [Animation](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g2d/Animation.html)

    (play-mode :loop)
    (play-mode :loop-pingpong)
    (play-mode :loop-random)
    (play-mode :loop-reversed)
    (play-mode :normal)
    (play-mode :reversed)
"
  [key]
  `(u/static-field-upper :graphics :g2d :Animation ~key))

(defn animation*
  "The function version of `animation`"
  [duration textures]
  (Animation. duration
              (u/gdx-array (map #(u/get-obj % :object) textures))
              (play-mode :normal)))

(defmacro animation
  "Returns an [Animation](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g2d/Animation.html)

    (animation 0.2
               [walk-1 walk-2 walk-3]
               :set-play-mode (play-mode :loop-pingpong))
"
  [duration textures & options]
  `(u/calls! ^Animation (animation* ~duration ~textures) ~@options))

(defmacro animation!
  "Calls a single method on an `animation`

    (animation! object :set-play-mode (play-mode :loop))
"
  [object k & options]
  `(u/call! ^Animation ~object ~k ~@options))

(defn animation->texture
  "Returns a `texture` entity with a frame from `animation` based on the total
time the `screen` has been showing

    (animation->texture screen anim)
"
  ([{:keys [total-time]} ^Animation animation]
    (texture* (.getKeyFrame animation total-time true)))
  ([{:keys [total-time]} ^Animation animation is-looping?]
    (texture* (.getKeyFrame animation total-time is-looping?))))
