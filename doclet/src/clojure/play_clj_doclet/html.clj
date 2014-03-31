(ns play-clj-doclet.html
  (:require [clojure.string :as string]
            [hiccup.core :refer :all]))

(defn camel->keyword
  [s]
  (->> (string/split (string/replace s "_" "-") #"(?<=[a-z])(?=[A-Z])")
       (map string/lower-case)
       (string/join "-")
       keyword))

(defn param
  [[type-name param-name]]
  (html [:span {}
         [:span {:class "type-name"} "^" type-name]
         " "
         (-> param-name camel->keyword name)]))

(defn item
  [[name text params]]
  [:div
   [:p
    [:b (str name)]
    " "
    (string/join ", " (map param params))]
   [:i text]])

(defn section
  [[clj-name items]]
  [:div
   [:h2 {} clj-name]
   (map item (sort-by first items))])

(defn create
  [doc-map]
  (html (map section doc-map)))
