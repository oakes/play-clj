(ns play-clj.g2d
  (:require [play-clj.utils :as u])
  (:import [com.badlogic.gdx.graphics Texture]
           [com.badlogic.gdx.graphics.g2d Animation BitmapFont TextureRegion]))

(defmacro bitmap-font
  [& options]
  `(BitmapFont. ~@options))

(defn texture*
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
  [arg & options]
  `(let [entity# (texture* ~arg)]
     (u/calls! ^TextureRegion (u/get-obj entity# :object) ~@options)
     entity#))

(defmacro texture!
  [entity k & options]
  `(u/call! ^TextureRegion (u/get-obj ~entity :object) ~k ~@options))

(defmacro play-mode
  [key]
  `(u/static-field-upper :graphics :g2d :Animation ~key))

(defn animation*
  [duration textures]
  (Animation. duration
              (u/gdx-array (map #(u/get-obj % :object) textures))
              (play-mode :normal)))

(defmacro animation
  [duration textures & options]
  `(u/calls! ^Animation (animation* ~duration ~textures) ~@options))

(defmacro animation!
  [object k & options]
  `(u/call! ^Animation ~object ~k ~@options))

(defn animation->texture
  ([{:keys [total-time]} ^Animation animation]
    (texture* (.getKeyFrame animation total-time true)))
  ([{:keys [total-time]} ^Animation animation is-looping?]
    (texture* (.getKeyFrame animation total-time is-looping?))))
