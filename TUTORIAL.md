## Getting Started

You can easily get started with play-clj by creating a new project with [Nightcode](https://nightcode.info/) and choosing the Clojure game option. You may also create a project on the command line with [Leiningen](https://github.com/technomancy/leiningen):

    lein new play-clj hello-world

Either way, you'll get three separate projects for desktop, Android, and iOS, all pointing to the same directories for source code and resources. You can build the projects using Nightcode or Leiningen.

## Project Structure

After making a game called `hello-world`, you'll see three sub-folders: `android`, `desktop`, and `ios`. You'll be spending most of your time in the `desktop` folder, because it's easier to develop your game on your computer and build it for mobile devices later.

Your actual game code will be in the `desktop/src-common` folder, and all the images and sound files will be in the `desktop/resources` folder. When you're ready to build an Android and iOS version, they will read from both of these folders, so you don't have to duplicate any files.

## Your First Run

This tutorial will assume that you are using Nightcode, but it should be easy to follow without it as well. To kick things off, navigate to the main file at `desktop/src-common/hello_world/core.clj` by double-clicking each folder in the sidebar on the left. Once you click on `core.clj`, you should see its contents appear in the editor pane on the right.

As long as the selection in the sidebar is somewhere inside the `desktop` folder, the build pane at the bottom will apply to that version of your game. So, try clicking _Run_ and wait for your game to appear. If all goes well, you should see a window with nothing but a small label on the bottom left that says "Hello world!".

## Game Structure

Let's look at the basic structure of your game. It starts out with a call to `defgame`, which creates the basic game object for you and contains a single function called `:on-create` that runs when your game starts. The only thing it does is hand off to your screen, where all the action takes place.

In `defscreen`, you'll find that a few simple functions are defined: `:on-show` and `:on-render`. The first only runs when the screen is first shown, and the second is run every single time your game wants to draw on the screen (which is typically 60 times per second).

There are many other functions you can put inside `defscreen`, each letting you run code when certain important events happen. For now, we'll stick to the two we started with, because they are the most fundamental, but you can read the documentation to learn about the others.

## Entity System

Most games need some way to keep track of all the things displayed within them. We call these things "entities". Normally, we need to remember attributes such as their position, size, and possibly other values like health and damage. In play-clj, these entities are simply maps, so you can store whatever you want inside of them.

Often, games will store these entities in a list, and in their render function they will loop over the list, perform whatever changes are necessary on the entities (such as moving them), and then call a function to render them. This, of course, would not be idiomatic in Clojure, and leads to more complicated software.

In play-clj, the entities list is stored behind the scenes and is given to you in each function within `defscreen`. It's a normal Clojure list, so you can't directly change it. Instead, you must return a new entities list at the end of each `defscreen` function, which will then be provided to all other functions when they run.

## Loading a Texture

Right now, you're using the `play-clj.ui` library to display a label. This library is useful for typical UI needs such as a title screen, but not very useful for the game itself. Let's get rid of it for now, and instead use the `play-clj.g2d` library, which contains the basic functions for 2D games. Try changing the `ns` declaration to look like this:

```clojure
(ns hello-world.core
  (:require [play-clj.core :refer :all]
            [play-clj.g2d :refer :all]))
```

Now let's find an image to use as a texture in the game. Find one you'd like to use, such as [this Clojure logo](http://upload.wikimedia.org/wikipedia/commons/thumb/a/a3/Clojure_Programming_Language_Logo_Icon_SVG.svg/200px-Clojure_Programming_Language_Logo_Icon_SVG.svg.png), and save it to the `desktop/resources` folder. Next, simply change the line where the label entity is being created, so it creates a texture from that file instead:

```clojure
    (conj entities (texture "clojure.png"))
```

## Size and Position

If you run the code now, you'll see the image in the bottom-left corner. As mentioned, entities such as the one created by `texture` are simply Clojure maps. By default, our entity will look like this:

```clojure
{:type :texture
 :object #<TextureRegion com.badlogic.gdx.graphics.g2d.TextureRegion@207bfdc3>}
```

A `texture` contains the underlying Java object. By default, it will be drawn at the bottom-left corner with the size of the image itself. You can change the position and size by simply using `assoc`:

```clojure
    (conj entities (assoc (texture "clojure.png")
                          :x 50 :y 50 :width 100 :height 100))
```

## Input

Let's add a new function at the end of `defscreen` called `:on-key-down`, which runs when a key is pressed:

```clojure
  :on-key-down
  (fn [screen entities]
    )
```

If takes the same form as the other functions, expecting a new entities list to be returned at the end. The first argument, `screen`, which we haven't talked about yet, is a Clojure map containing various important values. In the `:on-key-down` function, it will contain a `:keycode` which is a number referring to the key which was pressed.

You can reference the [LibGDX documentation](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/Input.Keys.html) to see all the possible keys. To get a key's number, just pass the name as a keyword into the `key-code` function. For example, `(key-code :PAGE_DOWN)` will return the number associated with that key. You can also write the keyword in a more Clojuresque way, using lower-case with hyphens, like this: `(key-code :page-down)`.

Let's write a conditional statement that prints out which arrow key you pressed. Note that if a `defscreen` function returns `nil`, it leaves the entities list unchanged, so the code below won't wipe out the entities list.

```clojure
  :on-key-down
  (fn [screen entities]
    (cond
      (= (:keycode screen) (key-code :dpad-down))
      (println "down")
      (= (:keycode screen) (key-code :dpad-up))
      (println "up")
      (= (:keycode screen) (key-code :dpad-right))
      (println "right")
      (= (:keycode screen) (key-code :dpad-left))
      (println "left")))
```

Now, what about mobile devices? We don't have a keyboard, so let's create an `:on-touch-down` function:

```clojure
  :on-touch-down
  (fn [screen entities]
    )
```

In this case, the screen map will contain an `:x` and `:y` for the point on the screen that was touched. We can simply check to see what part of the screen this point is by using `game` to get the overall game's width and height.

```clojure
  :on-touch-down
  (fn [screen entities]
    (cond
      (> (:y screen) (* (game :height) (/ 2 3)))
      (println "down")
      (< (:y screen) (/ (game :height) 3))
      (println "up")
      (> (:x screen) (* (game :width) (/ 2 3)))
      (println "right")
      (< (:x screen) (/ (game :width) 3))
      (println "left")))
```

Conveniently, the `:on-touch-down` function also runs when a mouse is clicked on the screen, so we are adding mouse support to the game as well!

## Movement

We already know how to change an entity's position, so let's leverage that to make our image move when we hit the keys. Make a new function above `defscreen` that takes the entity and a keyword, and returns the entity with an updated position:

```clojure
(defn move
  [entity direction]
  (case direction
    :down (assoc entity :y (dec (:y entity)))
    :up (assoc entity :y (inc (:y entity)))
    :right (assoc entity :x (inc (:x entity)))
    :left (assoc entity :x (dec (:x entity)))
    nil))
```

Now we can update out `:on-key-down` and `:on-touch-down` functions to move the entity. Note that we are technically returning a single entity rather than an entities list, but play-clj will turn it back into a list automatically.

```clojure
  :on-key-down
  (fn [screen entities]
    (cond
      (= (:keycode screen) (key-code :dpad-down))
      (move (first entities) :down)
      (= (:keycode screen) (key-code :dpad-up))
      (move (first entities) :up)
      (= (:keycode screen) (key-code :dpad-right))
      (move (first entities) :right)
      (= (:keycode screen) (key-code :dpad-left))
      (move (first entities) :left)))
  :on-touch-down
  (fn [screen entities]
    (cond
      (> (:y screen) (* (game :height) (/ 2 3)))
      (move (first entities) :down)
      (< (:y screen) (/ (game :height) 3))
      (move (first entities) :up)
      (> (:x screen) (* (game :width) (/ 2 3)))
      (move (first entities) :right)
      (< (:x screen) (/ (game :width) 3))
      (move (first entities) :left)))
```

## Camera

You'll notice that when you resize your game's window, the image looks stretched. That's because the game still thinks it's 800x600 pixels in size, so it adjusts accordingly. To make your game adjust dynamically to different screen sizes, you need to use a camera to adjust the size of the screen.

First, you need to create a camera and add it to the screen map in the `:on-show` function, like this:

```clojure
    (update! screen :renderer (stage) :camera (orthographic))
````

Orthographic cameras are for 2D games, so that's what we're using. Now, we need to create a new `defscreen` function called `:on-resize`, which will run whenever the screen resizes:

```clojure
  :on-resize
  (fn [screen entities]
    )
```

Lastly, you'll need to make either the width or height of the screen a constant value, so the other dimension can adjust to keep a constant ratio. We'll make the screen's height a constant 600 units in size using the `height!` function, which returns `nil` so the entities list won't be changed.

```clojure
  :on-resize
  (fn [screen entities]
    (height! screen 600))
```

Now, when you resize your game, the image is no longer stretched!

## Building for Android

1. Make sure you have JDK 7 installed (for Windows/OSX, you can get it from [Orcle](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html), and for Linux you can get it from [apt-get](http://openjdk.java.net/install/).
2. Download [the Android SDK](http://developer.android.com/sdk/index.html). However, don't bother getting the "ADT Bundle", which includes a full IDE, because you'll be using Nightcode. Instead, click "Use an Existing IDE" click the button that appears.
3. Extract the file anywhere you want.
4. Run the executable called "android" which is located in the "tools" folder of that archive. This executable will display the SDK Manager with several things checked by default. We want to at least support Ice Cream Sandwich, so check the box next to _Android 4.0.3 (API 15)_ and click _Install_.
5. In Nightcode, click on the `android` folder for your project in the sidebar. You should see a red-colored button called _Android SDK_. Click that, and find the folder you extracted the SDK to.
6. Connect your Android device to your computer and make sure USB debugging is enabled.
7. Click _Run_ and wait for the app to be built and installed on your device.

## Building for iOS

1. Get a computer running OS X.
2. Make sure you have JDK 7 installed (you can get it from [Oracle](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html).
3. Install Xcode from the Mac App Store.
4. Download and extract [the latest RoboVM](http://download.robovm.org/).
5. In Nightcode, click on the 'ios' folder for your project in the sidebar. You should see a red-colored button called _RoboVM_. Click that, and find the folder you extracted the SDK to.
6. Click _Run_ and wait for the app to be built and run in the iOS simulator (the _Build_ button will send it to your device, but you need the certificates set up for that and may need to edit `ios/project.clj` to pass the appropriate values to RoboVM).
