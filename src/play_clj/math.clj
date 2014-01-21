(ns play-clj.math
  (:require [play-clj.utils :as u])
  (:import [com.badlogic.gdx.math Bezier Bresenham2 BSpline CatmullRomSpline
            Circle ConvexHull DelaunayTriangulator EarClippingTriangulator
            Ellipse FloatCounter Frustum GridPoint2 GridPoint3 Matrix3 Matrix4
            Plane Polygon Polyline Quaternion Rectangle Vector2 Vector3
            WindowedMean]))

; static methods/fields

(defmacro geometry!
  "Calls a single method on [GeometryUtils](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/GeometryUtils.html)"
  [k & options]
  `(~(u/static-symbol [:math :GeometryUtils k] u/key->camel) ~@options))

(defmacro interpolation
  "Returns a static class in [Interpolation](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Interpolation.html)

    (interpolation :bounce)"
  [k]
  `~(symbol (str u/main-package ".math.Interpolation$" (u/key->pascal k))))

(defmacro intersector!
  "Calls a single method on [Intersector](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Intersector.html)

    (intersector! :is-point-in-triangle 0 1 0 0 1 2 3 0)"
  [k & options]
  `(~(u/static-symbol [:math :Intersector k] u/key->camel) ~@options))

(defmacro math!
  "Calls a single method on [MathUtils](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/MathUtils.html)

    (math! :ceil 0.1)"
  [k & options]
  `(~(u/static-symbol [:math :MathUtils k] u/key->camel) ~@options))

(defmacro plane-side
  "Returns a static field in [Plane.PlaneSide](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Plane.PlaneSide.html)

    (plane-side :back)"
  [k]
  `~(symbol (str u/main-package ".math.Plane$PlaneSide/" (u/key->pascal k))))

; bezier

(defn bezier*
  "The function version of `bezier`"
  ([]
    (Bezier.))
  ([points]
    (Bezier. (into-array points) 0 (count points))))

(defmacro bezier
  "Returns a [Bezier](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Bezier.html)"
  [points & options]
  `(u/calls! ^Bezier (bezier* ~points) ~@options))

(defmacro bezier!
  "Calls a single method on a `bezier`"
  [object k & options]
  `(u/call! ^Bezier ~object ~k ~@options))

; bresenham2

(defn bresenham2*
  "The function version of `bresenham2`"
  []
  (Bresenham2.))

(defmacro bresenham2
  "Returns a [Bresenham2](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Bresenham2.html)"
  [& options]
  `(u/calls! ^Bresenham2 (bresenham2*) ~@options))

(defmacro bresenham2!
  "Calls a single method on a `bresenham2`"
  [object k & options]
  `(u/call! ^Bresenham2 ~object ~k ~@options))

; b-spline

(defn b-spline*
  "The function version of `b-spline`"
  ([]
    (BSpline.))
  ([points degree cont?]
    (BSpline. (into-array points) degree cont?)))

(defmacro b-spline
  "Returns a [BSpline](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/BSpline.html)"
  [points degree cont? & options]
  `(u/calls! ^BSpline (b-spline* ~points ~degree ~cont?) ~@options))

(defmacro b-spline!
  "Calls a single method on a `b-spline`"
  [object k & options]
  `(u/call! ^BSpline ~object ~k ~@options))

; catmull-rom-spline

(defn catmull-rom-spline*
  "The function version of `catmull-rom-spline`"
  ([]
    (CatmullRomSpline.))
  ([points cont?]
    (CatmullRomSpline. (into-array points) cont?)))

(defmacro catmull-rom-spline
  "Returns a [CatmullRomSpline](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/CatmullRomSpline.html)"
  [points cont? & options]
  `(u/calls! ^CatmullRomSpline (catmull-rom-spline* ~points ~cont?) ~@options))

(defmacro catmull-rom-spline!
  "Calls a single method on a `catmull-rom-spline`"
  [object k & options]
  `(u/call! ^CatmullRomSpline ~object ~k ~@options))

; circle

(defn circle*
  "The function version of `circle`"
  ([]
    (Circle.))
  ([x y radius]
    (Circle. x y radius)))

(defmacro circle
  "Returns a [Circle](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Circle.html)"
  [x y radius & options]
  `(u/calls! ^Circle (circle* ~x ~y ~radius) ~@options))

(defmacro circle!
  "Calls a single method on a `circle`"
  [object k & options]
  `(u/call! ^Circle ~object ~k ~@options))

; convex-hull

(defn convex-hull*
  "The function version of `convex-hull`"
  []
  (ConvexHull.))

(defmacro convex-hull
  "Returns a [ConvexHull](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/ConvexHull.html)"
  [& options]
  `(u/calls! ^ConvexHull (convex-hull*) ~@options))

(defmacro convex-hull!
  "Calls a single method on a `convex-hull`"
  [object k & options]
  `(u/call! ^ConvexHull ~object ~k ~@options))

; delaunay-triangulator

(defn delaunay-triangulator*
  "The function version of `delaunay-triangulator`"
  []
  (DelaunayTriangulator.))

(defmacro delaunay-triangulator
  "Returns a [DelaunayTriangulator](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/DelaunayTriangulator.html)"
  [& options]
  `(u/calls! ^DelaunayTriangulator (delaunay-triangulator*) ~@options))

(defmacro delaunay-triangulator!
  "Calls a single method on a `delaunay-triangulator`"
  [object k & options]
  `(u/call! ^DelaunayTriangulator ~object ~k ~@options))

; ear-clipping-triangulator

(defn ear-clipping-triangulator*
  "The function version of `ear-clipping-triangulator`"
  []
  (EarClippingTriangulator.))

(defmacro ear-clipping-triangulator
  "Returns an [EarClippingTriangulator](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/EarClippingTriangulator.html)"
  [& options]
  `(u/calls! ^EarClippingTriangulator (ear-clipping-triangulator*) ~@options))

(defmacro ear-clipping-triangulator!
  "Calls a single method on a `ear-clipping-triangulator`"
  [object k & options]
  `(u/call! ^EarClippingTriangulator ~object ~k ~@options))

; ellipse

(defn ellipse*
  "The function version of `ellipse`"
  ([]
    (Ellipse.))
  ([x y width height]
    (Ellipse. x y width height)))

(defmacro ellipse
  "Returns an [Ellipse](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Ellipse.html)"
  [x y width height & options]
  `(u/calls! ^Ellipse (ellipse* ~x ~y ~width ~height) ~@options))

(defmacro ellipse!
  "Calls a single method on an `ellipse`"
  [object k & options]
  `(u/call! ^Ellipse ~object ~k ~@options))

; float-counter

(defn float-counter*
  "The function version of `float-counter`"
  [window-size]
  (FloatCounter. window-size))

(defmacro float-counter
  "Returns a [FloatCounter](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/FloatCounter.html)"
  [window-size & options]
  `(u/calls! ^FloatCounter (float-counter* ~window-size) ~@options))

(defmacro float-counter!
  "Calls a single method on a `float-counter`"
  [object k & options]
  `(u/call! ^FloatCounter ~object ~k ~@options))

; frustum

(defn frustum*
  "The function version of `frustum`"
  []
  (Frustum.))

(defmacro frustum
  "Returns a [Frustum](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Frustum.html)"
  [& options]
  `(u/calls! ^Frustum (frustum*) ~@options))

(defmacro frustum!
  "Calls a single method on a `frustum`"
  [object k & options]
  `(u/call! ^Frustum ~object ~k ~@options))

; grid-point-2

(defn grid-point-2*
  "The function version of `grid-point-2`"
  [x y]
  (GridPoint2. x y))

(defmacro grid-point-2
  "Returns a [GridPoint2](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/GridPoint2.html)"
  [x y & options]
  `(u/calls! ^GridPoint2 (grid-point-2* ~x ~y) ~@options))

(defmacro grid-point-2!
  "Calls a single method on a `grid-point-2`"
  [object k & options]
  `(u/call! ^GridPoint2 ~object ~k ~@options))

; grid-point-3

(defn grid-point-3*
  "The function version of `grid-point-3`"
  [x y z]
  (GridPoint3. x y z))

(defmacro grid-point-3
  "Returns a [GridPoint3](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/GridPoint3.html)"
  [x y z & options]
  `(u/calls! ^GridPoint3 (grid-point-3* ~x ~y ~z) ~@options))

(defmacro grid-point-3!
  "Calls a single method on a `grid-point-3`"
  [object k & options]
  `(u/call! ^GridPoint3 ~object ~k ~@options))

; matrix-3

(defn matrix-3*
  "The function version of `matrix-3`"
  ([]
    (Matrix3.))
  ([values]
    (Matrix3. values)))

(defmacro matrix-3
  "Returns a [Matrix3](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Matrix3.html)"
  [values & options]
  `(u/calls! ^Matrix3 (matrix-3* ~values) ~@options))

(defmacro matrix-3!
  "Calls a single method on a `matrix-3`"
  [object k & options]
  `(u/call! ^Matrix3 ~object ~k ~@options))

; matrix-4

(defn matrix-4*
  "The function version of `matrix-4`"
  ([]
    (Matrix4.))
  ([^floats values]
    (Matrix4. values)))

(defmacro matrix-4
  "Returns a [Matrix4](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Matrix4.html)"
  [values & options]
  `(u/calls! ^Matrix4 (matrix-4* ~values) ~@options))

(defmacro matrix-4!
  "Calls a single method on a `matrix-4`"
  [object k & options]
  `(u/call! ^Matrix4 ~object ~k ~@options))

; plane

(defn plane*
  "The function version of `plane`"
  [^Vector3 normal ^double d]
  (Plane. normal d))

(defmacro plane
  "Returns a [Plane](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Plane.html)"
  [normal d & options]
  `(u/calls! ^Plane (plane* ~normal ~d) ~@options))

(defmacro plane!
  "Calls a single method on a `plane`"
  [object k & options]
  `(u/call! ^Plane ~object ~k ~@options))

; polygon

(defn polygon*
  "The function version of `polygon`"
  ([]
    (Polygon.))
  ([vertices]
    (Polygon. vertices)))

(defmacro polygon
  "Returns a [Polygon](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Polygon.html)"
  [vertices & options]
  `(u/calls! ^Polygon (polygon* ~vertices) ~@options))

(defmacro polygon!
  "Calls a single method on a `polygon`"
  [object k & options]
  `(u/call! ^Polygon ~object ~k ~@options))

; polyline

(defn polyline*
  "The function version of `polyline`"
  ([]
    (Polyline.))
  ([vertices]
    (Polyline. vertices)))

(defmacro polyline
  "Returns a [Polyline](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Polyline.html)"
  [vertices & options]
  `(u/calls! ^Polyline (polyline* ~vertices) ~@options))

(defmacro polyline!
  "Calls a single method on a `polyline`"
  [object k & options]
  `(u/call! ^Polyline ~object ~k ~@options))

; quaternion

(defn quaternion*
  "The function version of `quaternion`"
  ([]
    (Quaternion.))
  ([w x y z]
    (Quaternion. w x y z)))

(defmacro quaternion
  "Returns a [Quaternion](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Quaternion.html)"
  [w x y z & options]
  `(u/calls! ^Quaternion (quaternion* ~w ~x ~y ~z) ~@options))

(defmacro quaternion!
  "Calls a single method on a `quaternion`"
  [object k & options]
  `(u/call! ^Quaternion ~object ~k ~@options))

; rectangle

(defn rectangle*
  "The function version of `rectangle`"
  ([]
    (Rectangle.))
  ([x y width height]
    (Rectangle. x y width height)))

(defmacro rectangle
  "Returns a [Rectangle](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Rectangle.html)"
  [x y width height & options]
  `(u/calls! ^Rectangle (rectangle* ~x ~y ~width ~height) ~@options))

(defmacro rectangle!
  "Calls a single method on a `rectangle`"
  [object k & options]
  `(u/call! ^Rectangle ~object ~k ~@options))

; vector-2

(defn vector-2*
  "The function version of `vector-2`"
  ([]
    (Vector2.))
  ([x y]
    (Vector2. x y)))

(defmacro vector-2
  "Returns a [Vector2](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Vector2.html)"
  [x y & options]
  `(u/calls! ^Vector2 (vector-2* ~x ~y) ~@options))

(defmacro vector-2!
  "Calls a single method on a `vector-2`"
  [object k & options]
  `(u/call! ^Vector2 ~object ~k ~@options))

; vector-3

(defn vector-3*
  "The function version of `vector-3`"
  ([]
    (Vector3.))
  ([x y z]
    (Vector3. x y z)))

(defmacro vector-3
  "Returns a [Vector3](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/Vector3.html)"
  [x y z & options]
  `(u/calls! ^Vector3 (vector-3* ~x ~y ~z) ~@options))

(defmacro vector-3!
  "Calls a single method on a `vector-3`"
  [object k & options]
  `(u/call! ^Vector3 ~object ~k ~@options))

; windowed-mean

(defn windowed-mean*
  "The function version of `windowed-mean`"
  [window-size]
  (WindowedMean. window-size))

(defmacro windowed-mean
  "Returns a [WindowedMean](http://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/math/WindowedMean.html)"
  [window-size & options]
  `(u/calls! ^WindowedMean (windowed-mean* ~window-size) ~@options))

(defmacro windowed-mean!
  "Calls a single method on a `windowed-mean`"
  [object k & options]
  `(u/call! ^WindowedMean ~object ~k ~@options))
