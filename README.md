## Note: I am focusing my efforts on [play-cljc](https://github.com/oakes/play-cljc), a library for games that run on both the desktop and the web.

## Introduction

A Clojure library that provides a wrapper for [libGDX](http://libgdx.badlogicgames.com/), allowing you to write 2D and 3D games that run on desktop OSes (Windows, OS X, and Linux) and Android with the same Clojure codebase.

## Getting Started

There are a few ways to create a project:

* [Leiningen](https://github.com/technomancy/leiningen): `lein new play-clj hello-world`
* [Nightmod](https://sekao.net/nightmod/): An IDE for play-clj

## Justification

The best thing about making a game in Clojure is that you can modify it in a REPL while it's running. By simply reloading a namespace, your code will be injected into the game, uninhibited by the restrictions posed by tools like HotSwap. Additionally, a REPL lets you read and modify the _state_ of your game at runtime, so you can quickly experiment and diagnose problems.

Clojure also brings the benefits of functional programming. This is becoming a big topic of discussion in gamedev circles, including by [John Carmack](http://gamasutra.com/view/news/169296/Indepth_Functional_programming_in_C.php). Part of this is due to the prevalence of multi-core hardware, making concurrency more important. Additionally, there is a general difficulty of maintaining object-oriented game codebases as they grow, due to complicated class hierarchies and state mutations.

## Documentation

* Check out [the example games](https://github.com/oakes/play-clj-examples)
* Read [the tutorial](TUTORIAL.md)
* Read [the generated docs](http://oakes.github.io/play-clj)
* Join the discussion on [/r/playclj](http://www.reddit.com/r/playclj/)
* Look at this commented example:

```clojure
(ns game-test.core
  (:require [play-clj.core :refer :all]
            [play-clj.g2d :refer :all]))

; define a screen, where all the action takes place
(defscreen main-screen
  ; all the screen functions get a map called "screen" containing various
  ; important values, and a vector called "entities" for storing game objects
  
  ; the entities vector is immutable, so in order to update it you must simply
  ; return a new vector at the end of each screen function
  
  ; this function runs only once, when the screen is first shown
  :on-show
  (fn [screen entities]
    ; update the screen map to hold a tiled map renderer and a camera
    (update! screen
             :renderer (orthogonal-tiled-map "level1.tmx" (/ 1 8))
             :camera (orthographic))
    (let [; load a sprite sheet from your resources dir
          sheet (texture "tiles.png")
          ; split the sheet into 16x16 tiles
          ; (the texture! macro lets you call TextureRegion methods directly)
          tiles (texture! sheet :split 16 16)
          ; get the tile at row 6, col 0
          player-image (texture (aget tiles 6 0))
          ; add position and size to the player-image map so it can be drawn
          player-image (assoc player-image :x 0 :y 0 :width 2 :height 2)]
      ; return a new entities vector with player-image inside of it
      [player-image]))
  
  ; this function runs every time a frame must be drawn (about 60 times per sec)
  :on-render
  (fn [screen entities]
    ; make the screen completely black
    (clear!)
    ; render the tiled map, draw the entities and return them
    (render! screen entities))
  
  ; this function runs when the screen dimensions change
  :on-resize
  (fn [screen entities]
    ; make the camera 20 tiles high, while maintaining the aspect ratio
    (height! screen 20)
    ; you can return nil if you didn't change any entities
    nil))

; define the game itself, and immediately hand off to the screen
(defgame game-test
  :on-create
  (fn [this]
    (set-screen! this main-screen)))
```

## Licensing

All files that originate from this project are dedicated to the public domain. I would love pull requests, and will assume that they are also dedicated to the public domain.
