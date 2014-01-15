(ns play-clj.math
  (:require [play-clj.utils :as u])
  (:import [com.badlogic.gdx.math Bezier Bresenham2 BSpline CatmullRomSpline
            Circle ConvexHull DelaunayTriangulator EarClippingTriangulator
            Ellipse FloatCounter Frustum Vector2 Vector3]))

; bezier

(defn bezier*
  ([]
    (Bezier.))
  ([points]
    (Bezier. (into-array points) 0 (count points))))

(defmacro bezier
  [points & options]
  `(u/calls! ^Bezier (bezier* ~points) ~@options))

(defmacro bezier!
  [object k & options]
  `(u/call! ^Bezier ~object ~k ~@options))

; bresenham2

(defn bresenham2*
  []
  (Bresenham2.))

(defmacro bresenham2
  [& options]
  `(u/calls! ^Bresenham2 (bresenham2*) ~@options))

(defmacro bresenham2!
  [object k & options]
  `(u/call! ^Bresenham2 ~object ~k ~@options))

; b-spline

(defn b-spline*
  ([]
    (BSpline.))
  ([points degree cont?]
    (BSpline. (into-array points) degree cont?)))

(defmacro b-spline
  [points degree cont? & options]
  `(u/calls! ^BSpline (b-spline* ~points ~degree ~cont?) ~@options))

(defmacro b-spline!
  [object k & options]
  `(u/call! ^BSpline ~object ~k ~@options))

; catmull-rom-spline

(defn catmull-rom-spline*
  ([]
    (CatmullRomSpline.))
  ([points cont?]
    (CatmullRomSpline. (into-array points) cont?)))

(defmacro catmull-rom-spline
  [points cont? & options]
  `(u/calls! ^CatmullRomSpline (catmull-rom-spline* ~points ~cont?) ~@options))

(defmacro catmull-rom-spline!
  [object k & options]
  `(u/call! ^BSpline ~object ~k ~@options))

; circle

(defn circle*
  ([]
    (Circle.))
  ([x y radius]
    (Circle. x y radius)))

(defmacro circle
  [x y radius & options]
  `(u/calls! ^Circle (circle* ~x ~y ~radius) ~@options))

(defmacro circle!
  [object k & options]
  `(u/call! ^Circle ~object ~k ~@options))

; convex-hull

(defn convex-hull*
  []
  (ConvexHull.))

(defmacro convex-hull
  [& options]
  `(u/calls! ^ConvexHull (convex-hull*) ~@options))

(defmacro convex-hull!
  [object k & options]
  `(u/call! ^ConvexHull ~object ~k ~@options))

; delaunay-triangulator

(defn delaunay-triangulator*
  []
  (DelaunayTriangulator.))

(defmacro delaunay-triangulator
  [& options]
  `(u/calls! ^DelaunayTriangulator (delaunay-triangulator*) ~@options))

(defmacro delaunay-triangulator!
  [object k & options]
  `(u/call! ^DelaunayTriangulator ~object ~k ~@options))

; ear-clipping-triangulator

(defn ear-clipping-triangulator*
  []
  (EarClippingTriangulator.))

(defmacro ear-clipping-triangulator
  [& options]
  `(u/calls! ^EarClippingTriangulator (ear-clipping-triangulator*) ~@options))

(defmacro ear-clipping-triangulator!
  [object k & options]
  `(u/call! ^EarClippingTriangulator ~object ~k ~@options))

; ellipse

(defn ellipse*
  ([]
    (Ellipse.))
  ([x y width height]
    (Ellipse. x y width height)))

(defmacro ellipse
  [x y width height & options]
  `(u/calls! ^Ellipse (ellipse* ~x ~y ~width ~height) ~@options))

(defmacro ellipse!
  [object k & options]
  `(u/call! ^Ellipse ~object ~k ~@options))

; float-counter

(defn float-counter*
  [window-size]
  (FloatCounter. window-size))

(defmacro float-counter
  [window-size & options]
  `(u/calls! ^FloatCounter (float-counter* ~window-size) ~@options))

(defmacro float-counter!
  [object k & options]
  `(u/call! ^FloatCounter ~object ~k ~@options))

; frustum

(defn frustum*
  []
  (Frustum.))

(defmacro frustum
  [& options]
  `(u/calls! ^Frustum (frustum*) ~@options))

(defmacro frustum!
  [object k & options]
  `(u/call! ^Frustum ~object ~k ~@options))

; vector-2

(defn vector-2*
  ([]
    (Vector2.))
  ([x y]
    (Vector2. x y)))

(defmacro vector-2
  [x y & options]
  `(u/calls! ^Vector2 (vector-2* ~x ~y) ~@options))

(defmacro vector-2!
  [object k & options]
  `(u/call! ^Vector2 ~object ~k ~@options))

; vector-3

(defn vector-3*
  ([]
    (Vector3.))
  ([x y z]
    (Vector3. x y z)))

(defmacro vector-3
  [x y z & options]
  `(u/calls! ^Vector3 (vector-3* ~x ~y ~z) ~@options))

(defmacro vector-3!
  [object k & options]
  `(u/call! ^Vector3 ~object ~k ~@options))
