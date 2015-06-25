(ns ccw.wizards.new-namespace)

(defonce
  ^{:doc "known extensions that are clojure editor extensions and treated specially by the editor:
    if the last segment of the ns typed by the user is this extension, then the segment is removed
    from the namespace name and used as the file extension."}
  clojure-editor-extensions
  #{"clj", "cljs", "cljc", "cljx", "clja", "dtm", "edn"})

(defonce ^{:doc "map of clojure file extension to function creating the content.
   content-fn takes 2 arguments: the namespace, and the extension"}
  extension->content-fn
  {})

(defonce default-content-fn
  (fn [namespace extension]
    (str "(ns " namespace ")\n\n")))

(defn content
  "Create the content of the file given its namespace and extension"
  [namespace extension]
  ((extension->content-fn extension default-content-fn)
    namespace
    extension))
