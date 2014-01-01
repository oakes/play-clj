## Introduction

A Clojure library that provides a wrapper for [LibGDX](http://libgdx.badlogicgames.com/), allowing you to write Clojure games that run on desktops (Windows/OSX/Linux), Android, and iOS. It is in a very early stage of development, so for now there is no documentation beyond the source code itself.

## Installation

The recommended way to start using play-clj is to create a Clojure game project with [Nightcode](https://nightcode.info/). It will automatically generate three separate Leiningen projects for desktop, Android, and iOS, all pointing to the same source code and resources directories. You can build the projects with Nightcode itself or with Leiningen on the command line (if you choose the latter, you will need to set up [lein-droid](https://github.com/clojure-android/lein-droid) and [lein-fruit](https://github.com/oakes/lein-fruit) to build the Android and iOS projects respectively).

