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
  (when-not (str/blank? name) 
    (condp = renderer
      :html (format "<b>%s</b> %s" name (or ns ""))
      :text (format "%s %s" name (or ns "")))))

(defn- render-sections [renderer sections no-sections-text]
  (let [s (condp = renderer
            :html (when-let [sections (keep identity sections)]
                    (apply str (mapcat vector (repeat "<p>") sections (repeat "</p>"))))
            :text (str/join "\n\n" (keep identity sections)))]
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
  (when-not (str/blank? name)
    (render-lines renderer
                  [(render-identity renderer name ns)
                   (optional-meta m)])))

(defn doc-doc [renderer {:keys [doc]}]
  (when-not (str/blank? doc)
    (render-section renderer
                    "Documentation"
                    (str "<pre>" doc "</pre>"))))

(defn var-doc-info [renderer m]
  (let [sections [(header-doc renderer m)
                  (arglist-doc renderer m)
                  (doc-doc renderer m)]]
    (render-sections 
      renderer
      sections
      "no doc found")))

(defn var-doc-info-html [m]
  (var-doc-info :html m))

(defn var-doc-info-text [m]
  (var-doc-info :text m))

(defn safe-split-lines 
  "Same as clojure.string/split-lines but accepts a nil input and returns nil
   instead of throwing an exception."
  [s]
  (when s (str/split-lines s)))

(defn slim-doc 
  "Summary of the documentation (arglist + start of the doc) taking up to 3
   lines. Used e.g. for context information."
  [s]
  (let [lines (safe-split-lines s)
        nb-display-lines 2
        lines (if (> (count lines) nb-display-lines) 
                (concat (take (dec nb-display-lines) lines)
                        [(str (nth lines (dec nb-display-lines)) " ...")]) 
                lines)]
    (str/join \newline (map str/trim lines))))