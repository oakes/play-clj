(ns play-clj.math
  (:require [play-clj.utils :as u])
  (:import [com.badlogic.gdx.math Bezier Bresenham2 BSpline CatmullRomSpline
            Circle ConvexHull DelaunayTriangulator EarClippingTriangulator
            Ellipse FloatCounter Frustum GridPoint2 GridPoint3 Matrix3 Matrix4
            Plane Polygon Polyline Quaternion Rectangle Vector2 Vector3
            WindowedMean]
           [com.badlogic.gdx.math.collision BoundingBox Ray Segment Sphere]))

; static methods/fields

(defmacro geometry!
  "Calls a single static method on [GeometryUtils](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/GeometryUtils.html)."
  [k & options]
  `(~(u/gdx-field :math :GeometryUtils (u/key->camel k)) ~@options))

(defmacro interpolation
  "Returns a static class in [Interpolation](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Interpolation.html).

    (interpolation :bounce)"
  [k]
  (u/gdx-class :math :Interpolation (u/key->pascal k)))

(defmacro intersector!
  "Calls a single static method on [Intersector](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Intersector.html).

    (intersector! :is-point-in-triangle 0 1 0 0 1 2 3 0)"
  [k & options]
  `(~(u/gdx-field :math :Intersector (u/key->camel k)) ~@options))

(defmacro math!
  "Calls a single static method on [MathUtils](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/MathUtils.html).

    (math! :ceil 0.1)"
  [k & options]
  `(~(u/gdx-field :math :MathUtils (u/key->camel k)) ~@options))

(defmacro plane-side
  "Returns a static field in [Plane.PlaneSide](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Plane.PlaneSide.html).

    (plane-side :back)"
  [k]
  (u/gdx-field :math "Plane$PlaneSide" (u/key->pascal k)))

; bezier

(defn bezier*
  ([]
   (Bezier.))
  ([points]
   (Bezier. (into-array points) 0 (count points))))

(defmacro bezier
  "Returns a [Bezier](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Bezier.html)."
  [points & options]
  `(u/calls! ^Bezier (bezier* ~points) ~@options))

(defmacro bezier!
  "Calls a single method on a `bezier`."
  [object k & options]
  `(u/call! ^Bezier ~object ~k ~@options))

; bresenham2

(defn bresenham-2*
  []
  (Bresenham2.))

(defmacro bresenham-2
  "Returns a [Bresenham2](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Bresenham2.html)."
  [& options]
  `(u/calls! ^Bresenham2 (bresenham-2*) ~@options))

(defmacro bresenham-2!
  "Calls a single method on a `bresenham-2`."
  [object k & options]
  `(u/call! ^Bresenham2 ~object ~k ~@options))

; b-spline

(defn b-spline*
  ([]
   (BSpline.))
  ([points degree cont?]
   (BSpline. (into-array points) degree cont?)))

(defmacro b-spline
  "Returns a [BSpline](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/BSpline.html)."
  [points degree cont? & options]
  `(u/calls! ^BSpline (b-spline* ~points ~degree ~cont?) ~@options))

(defmacro b-spline!
  "Calls a single method on a `b-spline`."
  [object k & options]
  `(u/call! ^BSpline ~object ~k ~@options))

; catmull-rom-spline

(defn catmull-rom-spline*
  ([]
   (CatmullRomSpline.))
  ([points cont?]
   (CatmullRomSpline. (into-array points) cont?)))

(defmacro catmull-rom-spline
  "Returns a [CatmullRomSpline](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/CatmullRomSpline.html)."
  [points cont? & options]
  `(u/calls! ^CatmullRomSpline (catmull-rom-spline* ~points ~cont?) ~@options))

(defmacro catmull-rom-spline!
  "Calls a single method on a `catmull-rom-spline`."
  [object k & options]
  `(u/call! ^CatmullRomSpline ~object ~k ~@options))

; circle

(defn circle*
  ([]
   (Circle.))
  ([x y radius]
   (Circle. x y radius)))

(defmacro circle
  "Returns a [Circle](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Circle.html)."
  [x y radius & options]
  `(u/calls! ^Circle (circle* ~x ~y ~radius) ~@options))

(defmacro circle!
  "Calls a single method on a `circle`."
  [object k & options]
  `(u/call! ^Circle ~object ~k ~@options))

; convex-hull

(defn convex-hull*
  []
  (ConvexHull.))

(defmacro convex-hull
  "Returns a [ConvexHull](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/ConvexHull.html)."
  [& options]
  `(u/calls! ^ConvexHull (convex-hull*) ~@options))

(defmacro convex-hull!
  "Calls a single method on a `convex-hull`."
  [object k & options]
  `(u/call! ^ConvexHull ~object ~k ~@options))

; delaunay-triangulator

(defn delaunay-triangulator*
  []
  (DelaunayTriangulator.))

(defmacro delaunay-triangulator
  "Returns a [DelaunayTriangulator](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/DelaunayTriangulator.html)."
  [& options]
  `(u/calls! ^DelaunayTriangulator (delaunay-triangulator*) ~@options))

(defmacro delaunay-triangulator!
  "Calls a single method on a `delaunay-triangulator`."
  [object k & options]
  `(u/call! ^DelaunayTriangulator ~object ~k ~@options))

; ear-clipping-triangulator

(defn ear-clipping-triangulator*
  []
  (EarClippingTriangulator.))

(defmacro ear-clipping-triangulator
  "Returns an [EarClippingTriangulator](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/EarClippingTriangulator.html)."
  [& options]
  `(u/calls! ^EarClippingTriangulator (ear-clipping-triangulator*) ~@options))

(defmacro ear-clipping-triangulator!
  "Calls a single method on a `ear-clipping-triangulator`."
  [object k & options]
  `(u/call! ^EarClippingTriangulator ~object ~k ~@options))

; ellipse

(defn ellipse*
  ([]
   (Ellipse.))
  ([x y width height]
   (Ellipse. x y width height)))

(defmacro ellipse
  "Returns an [Ellipse](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Ellipse.html)."
  [x y width height & options]
  `(u/calls! ^Ellipse (ellipse* ~x ~y ~width ~height) ~@options))

(defmacro ellipse!
  "Calls a single method on an `ellipse`."
  [object k & options]
  `(u/call! ^Ellipse ~object ~k ~@options))

; float-counter

(defn float-counter*
  [window-size]
  (FloatCounter. window-size))

(defmacro float-counter
  "Returns a [FloatCounter](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/FloatCounter.html)."
  [window-size & options]
  `(u/calls! ^FloatCounter (float-counter* ~window-size) ~@options))

(defmacro float-counter!
  "Calls a single method on a `float-counter`."
  [object k & options]
  `(u/call! ^FloatCounter ~object ~k ~@options))

; frustum

(defn frustum*
  []
  (Frustum.))

(defmacro frustum
  "Returns a [Frustum](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Frustum.html)."
  [& options]
  `(u/calls! ^Frustum (frustum*) ~@options))

(defmacro frustum!
  "Calls a single method on a `frustum`."
  [object k & options]
  `(u/call! ^Frustum ~object ~k ~@options))

; grid-point-2

(defn grid-point-2*
  [x y]
  (GridPoint2. x y))

(defmacro grid-point-2
  "Returns a [GridPoint2](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/GridPoint2.html)."
  [x y & options]
  `(u/calls! ^GridPoint2 (grid-point-2* ~x ~y) ~@options))

(defmacro grid-point-2!
  "Calls a single method on a `grid-point-2`."
  [object k & options]
  `(u/call! ^GridPoint2 ~object ~k ~@options))

; grid-point-3

(defn grid-point-3*
  [x y z]
  (GridPoint3. x y z))

(defmacro grid-point-3
  "Returns a [GridPoint3](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/GridPoint3.html)."
  [x y z & options]
  `(u/calls! ^GridPoint3 (grid-point-3* ~x ~y ~z) ~@options))

(defmacro grid-point-3!
  "Calls a single method on a `grid-point-3`."
  [object k & options]
  `(u/call! ^GridPoint3 ~object ~k ~@options))

; matrix-3

(defn matrix-3*
  ([]
   (Matrix3.))
  ([values]
   (Matrix3. values)))

(defmacro matrix-3
  "Returns a [Matrix3](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Matrix3.html)."
  [values & options]
  `(u/calls! ^Matrix3 (matrix-3* ~values) ~@options))

(defmacro matrix-3!
  "Calls a single method on a `matrix-3`."
  [object k & options]
  `(u/call! ^Matrix3 ~object ~k ~@options))

; matrix-4

(defn matrix-4*
  ([]
   (Matrix4.))
  ([^floats values]
   (Matrix4. values)))

(defmacro matrix-4
  "Returns a [Matrix4](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Matrix4.html)."
  [values & options]
  `(u/calls! ^Matrix4 (matrix-4* ~values) ~@options))

(defmacro matrix-4!
  "Calls a single method on a `matrix-4`."
  [object k & options]
  `(u/call! ^Matrix4 ~object ~k ~@options))

; plane

(defn plane*
  ([arg1 arg2]
   (Plane. arg1 arg2))
  ([^Vector3 p1 ^Vector3 p2 ^Vector3 p3]
   (Plane. p1 p2 p3)))

(defmacro plane
  "Returns a [Plane](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Plane.html)."
  [args & options]
  `(u/calls! ^Plane (apply plane* ~args) ~@options))

(defmacro plane!
  "Calls a single method on a `plane`."
  [object k & options]
  `(u/call! ^Plane ~object ~k ~@options))

; polygon

(defn polygon*
  ([]
   (Polygon.))
  ([vertices]
   (Polygon. vertices)))

(defmacro polygon
  "Returns a [Polygon](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Polygon.html)."
  [vertices & options]
  `(u/calls! ^Polygon (polygon* ~vertices) ~@options))

(defmacro polygon!
  "Calls a single method on a `polygon`."
  [object k & options]
  `(u/call! ^Polygon ~object ~k ~@options))

; polyline

(defn polyline*
  ([]
   (Polyline.))
  ([vertices]
   (Polyline. vertices)))

(defmacro polyline
  "Returns a [Polyline](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Polyline.html)."
  [vertices & options]
  `(u/calls! ^Polyline (polyline* ~vertices) ~@options))

(defmacro polyline!
  "Calls a single method on a `polyline`."
  [object k & options]
  `(u/call! ^Polyline ~object ~k ~@options))

; quaternion

(defn quaternion*
  ([]
   (Quaternion.))
  ([w x y z]
   (Quaternion. w x y z)))

(defmacro quaternion
  "Returns a [Quaternion](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Quaternion.html)."
  [w x y z & options]
  `(u/calls! ^Quaternion (quaternion* ~w ~x ~y ~z) ~@options))

(defmacro quaternion!
  "Calls a single method on a `quaternion`."
  [object k & options]
  `(u/call! ^Quaternion ~object ~k ~@options))

; rectangle

(defn rectangle*
  ([]
   (Rectangle.))
  ([x y width height]
   (Rectangle. x y width height)))

(defmacro rectangle
  "Returns a [Rectangle](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Rectangle.html)."
  [x y width height & options]
  `(u/calls! ^Rectangle (rectangle* ~x ~y ~width ~height) ~@options))

(defmacro rectangle!
  "Calls a single method on a `rectangle`."
  [object k & options]
  `(u/call! ^Rectangle ~object ~k ~@options))

; vector-2

(defn vector-2*
  ([]
   (Vector2.))
  ([x y]
   (Vector2. x y)))

(defmacro vector-2
  "Returns a [Vector2](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Vector2.html).
The values can be retrieved with `x` and `y`, and modified with `x!` and `y!`."
  [x y & options]
  `(u/calls! ^Vector2 (vector-2* ~x ~y) ~@options))

(defmacro vector-2!
  "Calls a single method on a `vector-2`. If you're trying to modify the values,
see `x!` and `y!`."
  [object k & options]
  `(u/call! ^Vector2 ~object ~k ~@options))

; vector-3

(defn vector-3*
  ([]
   (Vector3.))
  ([x y z]
   (Vector3. x y z)))

(defmacro vector-3
  "Returns a [Vector3](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Vector3.html).
The values can be retrieved with `x`, `y` and `z`, and modified with `x!`, `y!`,
and `z!`."
  [x y z & options]
  `(u/calls! ^Vector3 (vector-3* ~x ~y ~z) ~@options))

(defmacro vector-3!
  "Calls a single method on a `vector-3`. If you're trying to modify the values,
see `x!`, `y!`, and `z!`."
  [object k & options]
  `(u/call! ^Vector3 ~object ~k ~@options))

; windowed-mean

(defn windowed-mean*
  [window-size]
  (WindowedMean. window-size))

(defmacro windowed-mean
  "Returns a [WindowedMean](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/WindowedMean.html)."
  [window-size & options]
  `(u/calls! ^WindowedMean (windowed-mean* ~window-size) ~@options))

(defmacro windowed-mean!
  "Calls a single method on a `windowed-mean`."
  [object k & options]
  `(u/call! ^WindowedMean ~object ~k ~@options))

; bounding-box

(defn bounding-box*
  ([]
   (BoundingBox.))
  ([box]
   (BoundingBox. box))
  ([min max]
   (BoundingBox. min max)))

(defmacro bounding-box
  "Returns a [BoundingBox](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/collision/BoundingBox.html)."
  [min max & options]
  `(u/calls! ^BoundingBox (bounding-box* ~min ~max) ~@options))

(defmacro bounding-box!
  "Calls a single method on a `bounding-box`."
  [object k & options]
  `(u/call! ^BoundingBox ~object ~k ~@options))

; ray

(defn ray*
  [origin direction]
  (Ray. origin direction))

(defmacro ray
  "Returns a [Ray](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/collision/Ray.html)."
  [origin direction & options]
  `(u/calls! ^Ray (ray* ~origin ~direction) ~@options))

(defmacro ray!
  "Calls a single method on a `ray`."
  [object k & options]
  `(u/call! ^Ray ~object ~k ~@options))

; segment

(defn segment*
  ([a-x a-y a-z b-x b-y b-z]
   (Segment. a-x a-y a-z b-x b-y b-z))
  ([a b]
   (Segment. a b)))

(defmacro segment
  "Returns a [Segment](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/collision/Segment.html)."
  [a b & options]
  `(u/calls! ^Segment (segment* ~a ~b) ~@options))

(defmacro segment!
  "Calls a single method on a `segment`."
  [object k & options]
  `(u/call! ^Segment ~object ~k ~@options))

; sphere

(defn sphere*
  [center radius]
  (Sphere. center radius))

(defmacro sphere
  "Returns a [Sphere](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/collision/Sphere.html)."
  [center radius & options]
  `(u/calls! ^Sphere (sphere* ~center ~radius) ~@options))

(defmacro sphere!
  "Calls a single method on a `sphere`."
  [object k & options]
  `(u/call! ^Sphere ~object ~k ~@options))
