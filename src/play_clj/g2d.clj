(ns play-clj.g2d
  (:require [play-clj.utils :as u])
  (:import [com.badlogic.gdx.graphics Texture]
           [com.badlogic.gdx.graphics.g2d Animation BitmapFont NinePatch
            ParticleEffect TextureRegion]))

(defmacro bitmap-font
  "Returns a [BitmapFont](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g2d/BitmapFont.html)

    (bitmap-font)
    (bitmap-font file-handle region)"
  [& options]
  `(BitmapFont. ~@options))

; texture

(defn texture*
  "The function version of `texture`"
  [arg]
  (u/create-entity
    (cond
      (string? arg)
      (-> ^String arg Texture. TextureRegion.)
      (:object arg)
      (TextureRegion. ^TextureRegion (:object arg))
      :else
      arg)))

(defmacro texture
  "Returns an entity based on [TextureRegion](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g2d/TextureRegion.html)

    (texture \"image.png\")
    (texture \"image.png\" :flip true false)
    (texture \"image.png\"
             :flip true false
             :set-region 0 0 100 100)"
  [arg & options]
  `(let [entity# (texture* ~arg)]
     (u/calls! ^TextureRegion (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro texture!
  "Calls a single method on a `texture`

    (texture! entity :flip true false)
    (texture! entity :get-region-width)"
  [entity k & options]
  `(u/call! ^TextureRegion (u/get-obj ~entity :object) ~k ~@options))

; nine-patch

(defn nine-patch*
  "The function version of `nine-patch`"
  [arg]
  (u/create-entity
    (cond
      (string? arg)
      (-> ^String arg Texture. TextureRegion. NinePatch.)
      (:object arg)
      (NinePatch. (:object arg))
      (map? arg)
      (let [{:keys [region left right top bottom]} arg]
        (assert (and region left right top bottom))
        (NinePatch. region left right top bottom))
      :else
      arg)))

(defmacro nine-patch
  "Returns an entity based on [NinePatch](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g2d/NinePatch.html)

    (nine-patch \"image.png\")
    (nine-patch \"image.png\" :set-color (color :blue))
    (nine-patch {:image \"image.png\" :left 10 :right 10 :top 10 :bottom 10})"
  [arg & options]
  `(let [entity# (texture* ~arg)]
     (u/calls! ^NinePatch (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro nine-patch!
  "Calls a single method on a `nine-patch`

    (nine-patch! entity :set-color (color :blue))
    (nine-patch! entity :get-middle-width)"
  [entity k & options]
  `(u/call! ^NinePatch (u/get-obj ~entity :object) ~k ~@options))

; particle-effect

(defn particle-effect*
  "The function version of `particle-effect`"
  []
  (u/create-entity (ParticleEffect.)))

(defmacro particle-effect
  "Returns an entity based on [ParticleEffect](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g2d/ParticleEffect.html)

    (particle-effect :load
                     (files! :internal \"fire.p\")
                     (files! :internal \"fire-images\"))"
  [& options]
  `(let [entity# (texture*)]
     (u/calls! ^ParticleEffect (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro particle-effect!
  "Calls a single method on a `particle-effect`

    (particle-effect! entity :set-position 10 10)"
  [entity k & options]
  `(u/call! ^ParticleEffect (u/get-obj ~entity :object) ~k ~@options))

; animation

(defmacro play-mode
  "Returns a static field from [Animation](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g2d/Animation.html)

    (play-mode :loop)
    (play-mode :loop-pingpong)
    (play-mode :loop-random)
    (play-mode :loop-reversed)
    (play-mode :normal)
    (play-mode :reversed)"
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
               :set-play-mode (play-mode :loop-pingpong))"
  [duration textures & options]
  `(u/calls! ^Animation (animation* ~duration ~textures) ~@options))

(defmacro animation!
  "Calls a single method on an `animation`

    (animation! object :set-play-mode (play-mode :loop))"
  [object k & options]
  `(u/call! ^Animation ~object ~k ~@options))

(defn animation->texture
  "Returns a `texture` entity with a frame from `animation` based on the total
time the `screen` has been showing

    (animation->texture screen anim)"
  ([{:keys [total-time]} ^Animation animation]
    (texture* (.getKeyFrame animation total-time true)))
  ([{:keys [total-time]} ^Animation animation is-looping?]
    (texture* (.getKeyFrame animation total-time is-looping?))))
