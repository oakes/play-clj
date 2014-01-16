(ns play-clj.math
  (:require [play-clj.utils :as u])
  (:import [com.badlogic.gdx.math Bezier Bresenham2 BSpline CatmullRomSpline
            Circle ConvexHull DelaunayTriangulator EarClippingTriangulator
            Ellipse FloatCounter Frustum GridPoint2 GridPoint3 Matrix3 Matrix4
            Plane Polygon Polyline Quaternion Rectangle Vector2 Vector3
            WindowedMean]))

; static methods/fields

(defmacro geometry!
  [k & options]
  `(~(u/static-symbol [:math :GeometryUtils k] u/key->camel) ~@options))

(defmacro interpolation
  [k]
  `~(symbol (str u/main-package ".math.Interpolation$" (u/key->pascal k))))

(defmacro intersector!
  [k & options]
  `(~(u/static-symbol [:math :Intersector k] u/key->camel) ~@options))

(defmacro math!
  [k & options]
  `(~(u/static-symbol [:math :MathUtils k] u/key->camel) ~@options))

(defmacro plane-side
  [k]
  `~(symbol (str u/main-package ".math.Plane$PlaneSide/" (u/key->pascal k))))

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

; grid-point-2

(defn grid-point-2*
  [x y]
  (GridPoint2. x y))

(defmacro grid-point-2
  [x y & options]
  `(u/calls! ^GridPoint2 (grid-point-2* ~x ~y) ~@options))

(defmacro grid-point-2!
  [object k & options]
  `(u/call! ^GridPoint2 ~object ~k ~@options))

; grid-point-3

(defn grid-point-3*
  [x y z]
  (GridPoint3. x y z))

(defmacro grid-point-3
  [x y z & options]
  `(u/calls! ^GridPoint3 (grid-point-3* ~x ~y ~z) ~@options))

(defmacro grid-point-3!
  [object k & options]
  `(u/call! ^GridPoint3 ~object ~k ~@options))

; matrix-3

(defn matrix-3*
  ([]
    (Matrix3.))
  ([values]
    (Matrix3. values)))

(defmacro matrix-3
  [values & options]
  `(u/calls! ^Matrix3 (matrix-3* ~values) ~@options))

(defmacro matrix-3!
  [object k & options]
  `(u/call! ^Matrix3 ~object ~k ~@options))

; matrix-4

(defn matrix-4*
  ([]
    (Matrix4.))
  ([values]
    (Matrix4. values)))

(defmacro matrix-4
  [values & options]
  `(u/calls! ^Matrix4 (matrix-4* ~values) ~@options))

(defmacro matrix-4!
  [object k & options]
  `(u/call! ^Matrix4 ~object ~k ~@options))

; plane

(defn plane*
  [normal ^double d]
  (Plane. normal d))

(defmacro plane
  [normal d & options]
  `(u/calls! ^Plane (plane* ~normal ~d) ~@options))

(defmacro plane!
  [object k & options]
  `(u/call! ^Plane ~object ~k ~@options))

; polygon

(defn polygon*
  ([]
    (Polygon.))
  ([vertices]
    (Polygon. vertices)))

(defmacro polygon
  [vertices & options]
  `(u/calls! ^Polygon (polygon* ~vertices) ~@options))

(defmacro polygon!
  [object k & options]
  `(u/call! ^Polygon ~object ~k ~@options))

; polyline

(defn polyline*
  ([]
    (Polyline.))
  ([vertices]
    (Polyline. vertices)))

(defmacro polyline
  [vertices & options]
  `(u/calls! ^Polyline (polyline* ~vertices) ~@options))

(defmacro polyline!
  [object k & options]
  `(u/call! ^Polyline ~object ~k ~@options))

; quaternion

(defn quaternion*
  ([]
    (Quaternion.))
  ([w x y z]
    (Quaternion. w x y z)))

(defmacro quaternion
  [w x y z & options]
  `(u/calls! ^Quaternion (quaternion* ~w ~x ~y ~z) ~@options))

(defmacro quaternion!
  [object k & options]
  `(u/call! ^Quaternion ~object ~k ~@options))

; rectangle

(defn rectangle*
  ([]
    (Rectangle.))
  ([x y width height]
    (Rectangle. x y width height)))

(defmacro rectangle
  [x y width height & options]
  `(u/calls! ^Rectangle (rectangle* ~x ~y ~width ~height) ~@options))

(defmacro rectangle!
  [object k & options]
  `(u/call! ^Rectangle ~object ~k ~@options))

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

; windowed-mean

(defn windowed-mean*
  [window-size]
  (WindowedMean. window-size))

(defmacro windowed-mean
  [window-size & options]
  `(u/calls! ^WindowedMean (windowed-mean* ~window-size) ~@options))

(defmacro windowed-mean!
  [object k & options]
  `(u/call! ^WindowedMean ~object ~k ~@options))
