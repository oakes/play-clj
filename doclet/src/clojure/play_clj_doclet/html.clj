(ns play-clj-doclet.html
  (:require [hiccup.core :refer :all]))

(defn create-item
  [[name text params]]
  [:div
   [:h3 (str name)]
   [:i text]])

(defn create-section
  [[clj-name items]]
  [:div
   [:h2 {} clj-name]
   (map create-item items)])

(defn create-html
  [doc-map]
  (html (map create-section doc-map)))
