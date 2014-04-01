(ns play-clj-doclet.html
  (:require [clojure.string :as string]
            [hiccup.core :refer :all]))

(defn sidebar
  [parsed-files]
  [:div {:class "sidebar"}
   (for [{:keys [ns groups] :as pf} parsed-files]
     (cons (when (> (count ns) 0)
             [:div {:class "ns"} ns])
           (for [g groups]
             [:div {:class "name"} (:name g)])))])

(defn param
  [[type-name param-name]]
  (html [:span {}
         [:span {:class "type-name"} "^" type-name]
         " "
         param-name]))

(defn item
  [{:keys [name text type args]}]
  [:div {:class "item"}
   [:span {:class "item-name"}
    (str name)]
   [:span {:class "item-args"}
    (string/join ", " (map param args))]
   (when text
     [:div {:class "item-doc"} text])])

(defn content
  [parsed-files]
  [:div {:class "content"}
   (for [{:keys [ns groups] :as pf} parsed-files]
     (for [{:keys [name docstring java] :as g} groups]
       (concat [[:div {:class "header"} name]
                [:div {:class "doc"} docstring]]
               (for [[item-name items] java]
                 (cons (when (not= name item-name)
                         [:div {:class "sub-header"} item-name])
                       (map item items))))))])

(defn create
  [parsed-files]
  (html [:head
         [:link {:rel "stylesheet" :type "text/css" :href "style.css"}]]
        [:body
         (sidebar parsed-files)
         (content parsed-files)]))
