(ns play-clj.g2d
  (:require [play-clj.entities]
            [play-clj.utils :as u])
  (:import [com.badlogic.gdx Files Gdx]
           [com.badlogic.gdx.files FileHandle]
           [com.badlogic.gdx.graphics Pixmap Texture]
           [com.badlogic.gdx.graphics.g2d Animation BitmapFont NinePatch Sprite
            ParticleEffect TextureAtlas TextureRegion]
           [play_clj.entities TextureEntity NinePatchEntity SpriteEntity
            ParticleEffectEntity]))

(defn bitmap-font*
  [^String path]
  (if (nil? path)
    (BitmapFont.)
    (or (u/load-asset path BitmapFont)
        (BitmapFont. (.internal (Gdx/files) path)))))

(defmacro bitmap-font
  "Returns a [BitmapFont](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g2d/BitmapFont.html).

    (bitmap-font)
    (bitmap-font \"default.fnt\")"
  [& [path & options]]
  `(let [^BitmapFont object# (bitmap-font* ~path)]
     (u/calls! object# ~@options)))

(defmacro bitmap-font!
  "Calls a single method on a `bitmap-font`."
  [object k & options]
  `(u/call! ^BitmapFont ~object ~k ~@options))

; texture

(defn texture*
  [arg]
  (TextureEntity.
    (cond
      (string? arg)
      (-> (or (u/load-asset arg Texture)
              (Texture. ^String arg))
          TextureRegion.)
      (instance? Pixmap arg)
      (-> ^Pixmap arg Texture. TextureRegion.)
      (instance? Texture arg)
      (-> ^Texture arg TextureRegion.)
      (instance? TextureEntity arg)
      (-> ^TextureRegion (:object arg) TextureRegion.)
      :else
      arg)))

(defmacro texture
  "Returns an entity based on [TextureRegion](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g2d/TextureRegion.html).

    ; load image.png
    (texture \"image.png\")
    ; load image.png, flip it, and only display the specified region
    (texture \"image.png\"
             :flip true false
             :set-region 0 0 100 100)
    ; create a new texture based on an existing one
    (texture (texture \"image.png\"))
    ; rotate a texture 45 degress
    (assoc (texture \"image.png\")
           :angle 45)"
  [arg & options]
  `(let [entity# (texture* ~arg)]
     (u/calls! ^TextureRegion (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro texture!
  "Calls a single method on a `texture`.

    (texture! entity :flip true false)
    (texture! entity :get-region-width)"
  [entity k & options]
  `(u/call! ^TextureRegion (u/get-obj ~entity :object) ~k ~@options))

(defn texture?
  "Returns true if `entity` is a `texture`."
  [entity]
  (instance? TextureRegion (u/get-obj entity :object)))

; sprite

(defn sprite*
  [arg]
  (SpriteEntity.
    (cond
      (string? arg)
      (-> (or (u/load-asset arg Texture)
              (Texture. ^String arg))
          Sprite.)
      (instance? Pixmap arg)
      (-> ^Pixmap arg Texture. Sprite.)
      (instance? Texture arg)
      (-> ^Texture arg Sprite.)
      (instance? SpriteEntity arg)
      (-> ^Sprite (:object arg) Sprite.)
      :else
      arg)))

(defmacro sprite
  [arg & options]
  `(let [entity# (sprite* ~arg)]
     (u/calls! ^Sprite (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro sprite!
  "Calls a single method on a `sprite`.

    (sprite! entity :flip true false)
    (sprite! entity :set-alpha 0.5)"
  [entity k & options]
  `(u/call! ^Sprite (u/get-obj ~entity :object) ~k ~@options))

(defn sprite?
  "Returns true if `entity` is a `sprite`."
  [entity]
  (instance? Sprite (u/get-obj entity :object)))

; nine-patch

(defn nine-patch*
  [arg]
  (NinePatchEntity.
    (cond
      (string? arg)
      (-> (or (u/load-asset arg Texture)
              (Texture. ^String arg))
          TextureRegion.
          NinePatch.)
      (instance? NinePatchEntity arg)
      (NinePatch. (:object arg))
      (map? arg)
      (let [{:keys [region left right top bottom]} arg]
        (NinePatch. region left right top bottom))
      :else
      arg)))

(defmacro nine-patch
  "Returns an entity based on [NinePatch](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g2d/NinePatch.html).

    (nine-patch \"image.png\")
    (nine-patch \"image.png\" :set-color (color :blue))"
  [arg & options]
  `(let [entity# (nine-patch* ~arg)]
     (u/calls! ^NinePatch (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro nine-patch!
  "Calls a single method on a `nine-patch`.

    (nine-patch! entity :set-color (color :blue))
    (nine-patch! entity :get-middle-width)"
  [entity k & options]
  `(u/call! ^NinePatch (u/get-obj ~entity :object) ~k ~@options))

(defn nine-patch?
  "Returns true if `entity` is a `nine-patch`."
  [entity]
  (instance? NinePatch (u/get-obj entity :object)))

; particle-effect

(defn particle-effect*
  [^String path]
  (ParticleEffectEntity.
    (if (nil? path)
      (ParticleEffect.)
      (or (u/load-asset path ParticleEffect)
          (let [^FileHandle fh (.internal (Gdx/files) path)]
            (doto (ParticleEffect.) (.load fh (.parent fh))))))))

(defmacro particle-effect
  "Returns an entity based on [ParticleEffect](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g2d/ParticleEffect.html).

    (particle-effect)
    (particle-effect \"particles/fire.p\")"
  [& [path & options]]
  `(let [entity# (particle-effect* ~path)]
     (u/calls! ^ParticleEffect (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro particle-effect!
  "Calls a single method on a `particle-effect`.

    (particle-effect! entity :set-position 10 10)"
  [entity k & options]
  `(u/call! ^ParticleEffect (u/get-obj ~entity :object) ~k ~@options))

(defn particle-effect?
  "Returns true if `entity` is a `particle-effect`."
  [entity]
  (instance? ParticleEffect (u/get-obj entity :object)))

; texture-atlas

(defn texture-atlas*
  [^String path]
  (or (u/load-asset path TextureAtlas)
      (TextureAtlas. path)))

(defmacro texture-atlas
  "Returns a [TextureAtlas](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g2d/TextureAtlas.html).

    (texture-atlas \"packed.txt\")"
  [path & options]
  `(let [^TextureAtlas object# (texture-atlas* ~path)]
     (u/calls! object# ~@options)
     object#))

(defmacro texture-atlas!
  "Calls a single method on a `texture-atlas`.

    (texture-atlas! object :create-patch \"test\")"
  [object k & options]
  `(u/call! ^TextureAtlas ~object ~k ~@options))

; animation

(defmacro play-mode
  "Returns a static field from [Animation.PlayMode](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g2d/Animation.PlayMode.html).

    (play-mode :loop)"
  [k]
  (u/gdx-field :graphics :g2d "Animation$PlayMode" (u/key->upper k)))

(defn animation*
  [duration textures]
  (Animation. duration
              (u/gdx-array (map #(u/get-obj % :object) textures))
              (play-mode :normal)))

(defmacro animation
  "Returns an [Animation](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g2d/Animation.html).

    (animation 0.2
               [walk-1 walk-2 walk-3]
               :set-play-mode (play-mode :loop-pingpong))"
  [duration textures & options]
  `(u/calls! ^Animation (animation* ~duration ~textures) ~@options))

(defmacro animation!
  "Calls a single method on an `animation`.

    (animation! object :set-play-mode (play-mode :loop))"
  [object k & options]
  `(u/call! ^Animation ~object ~k ~@options))

(defn animation->texture
  "Returns a `texture` entity with a frame from `animation` based on the total
time the `screen` has been showing.

    (animation->texture screen anim)"
  ([{:keys [total-time] :as screen} ^Animation animation]
   (texture* (.getKeyFrame animation total-time true)))
  ([{:keys [total-time] :as screen} ^Animation animation is-looping?]
   (texture* (.getKeyFrame animation total-time is-looping?))))
