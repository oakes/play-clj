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

(defn java-param
  [[type-name param-name]]
  (html [:span {:class "j-arg"}
         [:span {:class "j-type"} "^" type-name]
         " "
         param-name]))

(defn java-item
  [{:keys [name text type args]}]
  [:div {:class "j-item"}
   [:span {:class "j-name"}
    (str name)]
   [:span {:class "j-args"}
    (string/join ", " (map java-param args))]
   (when text
     [:div {:class "j-doc"} text])])

(defn content
  [parsed-files]
  [:div {:class "content"}
   (for [{:keys [ns groups] :as pf} parsed-files]
     (for [{:keys [name docstring arglists java] :as g} groups]
       (list [:div {:class "clj"}
              (for [args arglists]
                [:div {:class "c-header"} (pr-str args)])
              [:div {:class "c-doc"} docstring]]
             [:div {:class "java"}
              (for [[item-name items] java]
                (cons (when (not= name item-name)
                        [:div {:class "j-header"} item-name])
                      (map java-item items)))])))])

(defn create
  [parsed-files]
  (html [:head
         [:link {:rel "stylesheet" :type "text/css" :href "style.css"}]]
        [:body
         (sidebar parsed-files)
         (content parsed-files)]))
