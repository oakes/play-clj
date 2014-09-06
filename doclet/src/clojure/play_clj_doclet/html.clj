(ns play-clj-doclet.html
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [hiccup.core :refer :all]))

(defn str->filename
  [ns s]
  (-> (subs ns (+ (.lastIndexOf ns ".") 1))
      (str "." s)
      (string/replace "?" "_q")
      (string/replace "->" "_")
      (string/replace ">" "_r")
      (string/replace "<" "_l")
      (str ".html")))

(defn sidebar
  [parsed-files]
  [:div {:class "sidebar"}
   (for [[ns groups] parsed-files]
     (cons [:div {:class "ns"} ns]
           (for [{:keys [name]} groups]
             [:div {:class "name"}
              [:a {:href (str->filename ns name)}
               name]])))])

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
  [{:keys [name docstring arglists java raw raw*]}]
  [:div {:class "content"}
   [:div {:class "item"}
    [:div {:class "clj"}
     (for [args arglists]
       [:div {:class "c-head"} (pr-str args)])
     [:div {:class "c-doc"} docstring]]
    (when (> (count java) 0)
      (list [:div {:class "c-head"} "Options"]
            (for [[item-name {:keys [text items]}] java]
              (list (when (> (count java) 1)
                      [:div {:class "j-text"} text])
                    [:div {:class "java"}
                     (map java-item items)]))))
    [:div {:class "c-head"} "Source"]
    [:div {:class "c-src"}
     (when raw* [:pre raw*])
     [:pre raw]]]])

(defn create-site-file
  [name sidebar content]
  (html [:html
         [:head
          [:title name]
          [:link {:rel "stylesheet" :href "highlight.css"}]
          [:link {:rel "stylesheet" :href "main.css"}]]
         [:body
          sidebar
          content
          [:script {:src "highlight.js"}]
          [:script {:src "main.js"}]]]))

(defn create-embed-file
  [content]
  (html [:html [:body content]]))

(defn copy-from-res
  [dir file-name]
  (spit (io/file dir file-name)
        (-> file-name io/resource slurp)))

(defn create-site!
  [dir parsed-files]
  (.mkdir (io/file dir))
  (copy-from-res dir "main.css")
  (copy-from-res dir "main.js")
  (copy-from-res dir "highlight.css")
  (copy-from-res dir "highlight.js")
  (doseq [[ns groups] parsed-files]
    (doseq [{:keys [name] :as group} groups]
      (spit (io/file dir (str->filename ns name))
            (create-site-file name (sidebar parsed-files) (content group)))))
  (spit (io/file dir "index.html")
        (create-site-file "play-clj docs" (sidebar parsed-files) nil))
  (println "Created" (str dir "/")))

(defn create-embed!
  [dir parsed-files]
  (.mkdir (io/file dir))
  (doseq [[ns groups] parsed-files]
    (doseq [{:keys [name] :as group} groups]
      (spit (io/file dir (str->filename ns name))
            (create-embed-file (content group)))))
  (spit (io/file dir "index.edn")
        (->> parsed-files
             (map (fn [[ns groups]]
                    (for [{:keys [name]} groups]
                      {:ns ns
                       :name name
                       :file (str->filename ns name)})))
             (apply concat)
             vec
             pr-str))
  (println "Created" (str dir "/")))
