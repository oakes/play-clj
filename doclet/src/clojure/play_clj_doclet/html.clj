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
   (when (> (count text) 0)
     [:i text])])

(defn create-from-file
  [parsed-file]
  (for [group (:groups parsed-file)]
    [:div
     [:h1 {} (:name group)]
     (for [[name items] (:java group)]
       [:div
        (when (not= (:name group) name)
          [:h3 name])
        (map item (sort-by first items))])]))

(defn create
  [parsed-files]
  (html (map create-from-file parsed-files)))
