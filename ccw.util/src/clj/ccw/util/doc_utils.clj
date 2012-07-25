(ns ccw.util.doc-utils
  (:require [clojure.string :as str]))

;	potential documentation tags:
; :since, :author, :private, :test, :tag, :file, :line, :ns, :name, 
; :macro, :arglists

(defn join 
  "Join c elements with s as of clojure.string/join, except nil elements
   are discarded"
  [s & c]
  (str/join s (keep identity c)))

(defn- arglists-seq [arglists]
  (let [arglists (read-string arglists)]
      (map pr-str arglists)))

(defn- render-lines [renderer lines]
  (condp = renderer
    :html (apply join "<br/>" lines)
    :text (apply join "\n" lines)))

(defn- render-section [renderer title body]
  (condp = renderer
    :html (format "<p><b>%s:</b><br/>%s" title body)
    :text (format "%s:\n%s" title body)))

(defn render-identity [renderer name ns]
  (condp = renderer
    :html (format "<b>%s</b> %s" name ns)
    :text (format "%s %s" name ns)))

(defn- render-sections [renderer sections no-sections-text]
  (let [s (condp = renderer
            :html (apply str (mapcat vector (repeat "<p>") sections (repeat "</p>")))
            :text (str/join "\n\n" sections))]
    (if (str/blank? s)
      (condp = renderer
        :html (format "<i>%s</i>" no-sections-text)
        :text no-sections-text)
      s)))

(defn- arglist-doc [renderer {:keys [arglists]}]
  (when-not (str/blank? arglists)
    (render-section renderer "Argument Lists"
                    (render-lines renderer (arglists-seq arglists)))))

(defn- optional-meta [{:keys [name macro private dynamic ns tag]}]
  (let [optional-meta (join ", " 
                            (when private "private")
                            (when macro "macro")
                            (when dynamic "dynamic")
                            tag)] 
    (when-not (str/blank? optional-meta)
      (str "(" optional-meta ")"))))

(defn header-doc [renderer {:keys [name ns] :as m}]
  (when name
    (render-lines renderer
                  [(render-identity renderer name ns)
                   (optional-meta m)])))

(defn doc-doc [renderer {:keys [doc]}]
  (when-not (str/blank? doc)
    (render-section renderer
                    "Documentation"
                    doc)))

(defn var-doc-info [renderer m]
  (render-sections 
    renderer
    [(header-doc renderer m)
     (arglist-doc renderer m)
     (doc-doc renderer m)]
    "no doc found"))

(defn var-doc-info-html [m]
  (var-doc-info :html m))

(defn var-doc-info-text [m]
  (var-doc-info :text m))