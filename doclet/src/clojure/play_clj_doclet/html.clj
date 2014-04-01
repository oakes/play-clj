(ns play-clj-doclet.html
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
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
     (for [{:keys [name docstring arglists java raw raw*] :as g} groups]
       [:div {:class "item"}
        [:div {:class "clj"}
         (for [args arglists]
           [:div {:class "c-head"} (pr-str args)])
         [:div {:class "c-doc"} docstring]]
        (for [[item-name items] java]
          (list (when (not= name item-name)
                  [:div {:class "j-head"} item-name])
                [:div {:class "java"}
                 (map java-item items)]))
        [:div {:class "c-src"}
         (when raw* [:pre raw*])
         [:pre raw]]]))])

(defn create
  [parsed-files]
  (html [:head
         [:link {:rel "stylesheet" :href "style.css"}]
         [:link {:rel "stylesheet" :href "styles/default.css"}]
         [:script (-> "highlight.pack.js" io/resource slurp)]
         [:script (-> "zepto.min.js" io/resource slurp)]
         [:script (-> "init.js" io/resource slurp)]]
        [:body
         (sidebar parsed-files)
         (content parsed-files)]))
