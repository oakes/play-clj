## Introduction

A Clojure library that provides a wrapper for [LibGDX](http://libgdx.badlogicgames.com/), allowing you to write Clojure games that run on desktop OSes (Windows, OS X, and Linux) and mobile OSes (Android and iOS) with the same codebase.

## Justification

There is a lot of talk in the games industry about two particular things: concurrency and functional programming. Concurrency is important due to the proliferation of multi-core gaming hardware, while functional programming is important due to the unmaintainability of modern object-oriented game codebases.

## Installation

The recommended way to start using play-clj is to create a Clojure game project with [Nightcode](https://nightcode.info/). It will automatically generate three separate Leiningen projects for desktop, Android, and iOS, all pointing to the same source code and resources directories. You can build the projects with Nightcode itself or with Leiningen on the command line (if you choose the latter, you will need to set up [lein-droid](https://github.com/clojure-android/lein-droid) and [lein-fruit](https://github.com/oakes/lein-fruit) to build the Android and iOS projects respectively).

## Documentation

There are currently no tutorials or generated docs, because play-clj is changing rapidly. This will be resolved in the near future. For now, consider this commented example:

```clojure
(ns game-test.core
  (:require [play-clj.core :refer :all]))

; defines a screen, where all the action takes place
(defscreen main-screen
  ; all the screen functions get a map called "screen" containing various
  ; important values, and a list called "entities" for storing game objects
  
  ; the entities list is immutable, so in order to update it you must simply
  ; return a new list at the end of each screen function
  
  ; this function runs only once, when the screen is first shown
  :on-show
  (fn [screen entities]
    ; updates the screen map to hold a tiled map renderer and a camera
    (update! screen
             :renderer (orthogonal-tiled-map "level1.tmx" 8)
             :camera (orthographic-camera))
    (let [; loads a sprite sheet from your resources dir
          sheet (image "tiles.png")
          ; splits the sheet into 16x16 tiles
          ; the "image!" function lets you call TextureRegion methods directly
          tiles (image! sheet :split 16 16)
          ; gets the tile at row 6, col 0
          player-image (image (aget tiles 6 0))
          ; add position and size to the player-image map so it can be drawn
          player-image (assoc player-image :x 0 :y 0 :width 2 :height 2)]
      ; return a new entities list with player-image inside of it
      (conj entities player-image)))
  
  ; this function runs every time a frame must be drawn (about 60 times per sec)
  :on-render
  (fn [screen entities]
    ; make the screen completely black
    (clear!)
    ; render the tiled map
    (render! screen)
    ; draw the entities and return them
    (draw! screen entities))
  
  ; this function runs when the screen dimensions change
  :on-resize
  (fn [screen entities]
    ; make the camera 20 tiles high, and adjust the width appropriately
    (let [height 20
          width (* 20 (/ (game :width) (game :height)))]
      (resize-camera! screen width height))
    ; return the entities list unmodified
    entities))

; defines the game itself, and immediately hands off to the screen
(defgame game-test
  :on-create
  (fn [this]
    (set-screen! this main-screen)))
```

## Licensing

All source files that originate from this project are dedicated to the public domain. I would love pull requests, and will assume that they are also dedicated to the public domain.
