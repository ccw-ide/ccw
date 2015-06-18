(ns ccw.core.doc-utils
  (:import org.eclipse.jface.internal.text.html.HTMLPrinter
           ccw.editors.clojure.ClojureEditorMessages)
  (:require [clojure.string :as str]))

;	potential documentation tags:
; :since, :author, :private, :test, :tag, :file, :line, :ns, :name,
; :macro, :arglists(ccw.server)/:arglists-str(cider-nrepl)

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
    :html (format "<h4>%s:</h4>%s" title body)
    :text (format "%s:\n%s" title body)))

(defn render-identity [renderer name ns]
  (when-not (str/blank? name)
    (let [parsed-ns (if ns (str ns "/" name) "")]
      (condp = renderer
        :html (str "<h1>" name "</h1>" (when-not (str/blank? parsed-ns) (str " " parsed-ns)))
        :text (str name (when-not (str/blank? parsed-ns) (str "\n" parsed-ns)))))))

(defn- render-sections [renderer sections]
  "Renders the sections according to the renderer param, returning nil if sections where empty or
  some issue arises."
  (when-let [sections (keep identity sections)]
    (condp = renderer
      :html (apply str (mapcat vector (repeat "<p>") sections (repeat "</p>")))
      :text (str/join "\n\n" sections))))

(defn- arglist-doc [renderer {:keys [arglists arglists-str]}]
  (let [arglists (or arglists arglists-str)]
    (when-not (str/blank? arglists)
      (render-section renderer
                      ClojureEditorMessages/DocUtils_args_label
                      (let [lines (render-lines renderer (arglists-seq arglists))] 
                        (condp = renderer
                          :html (str "<pre>" lines "</pre>")
                          :text lines))))))

(defn- macro-expansion-doc [renderer {:keys [macro-source macro-expanded]}]
  (when-not (and (str/blank? macro-source) (str/blank? macro-expanded))
    (condp = renderer
      :html (str "<pre>" macro-source "</pre>"
                 "<p style=\"text-align: center;\">" ClojureEditorMessages/DocUtils_macro_label "</p>"
                 "<pre>" macro-expanded "</pre>")
      :text (str macro-source "\n" ClojureEditorMessages/DocUtils_macro_label "\n" macro-expanded))))

(defn- optional-meta [renderer {:keys [name macro private dynamic ns tag]}]
  (let [optional-meta (join ", "
                            (when private "private")
                            (when macro "macro")
                            (when dynamic "dynamic")
                            tag)]
    (when-not (str/blank? optional-meta)
      (condp = renderer
        :html (str "<i>" "(" optional-meta ")" "</i>")
        :text (str "(" optional-meta ")")))))

(defn header-doc [renderer {:keys [name ns] :as m}]
  (when-not (str/blank? name)
    (str (render-identity renderer name ns)
         (condp = renderer
           :html "&nbsp"
           :text " ")
         (optional-meta renderer m))))

(defn adjust-docstring-indent
  "docstrings Generally have their first line start at column 0, while
   the following lines may start at column 0, 1, 2, 3, etc.
   This function finds the minimum number of spaces of each line but the first,
   and removes this number of spaces from the head of each line, but the first."
  [d]
  (let [lines (str/split-lines d)]
    (if-let [paddings (seq (map #(- (count %) (count (str/triml %)))
                                (remove str/blank? (rest lines))))]
      (let [padding (apply min paddings)]
        (str/join "\n"
                  (concat
                    [(first lines)]
                    (map #(subs % (min padding (count %))) (rest lines)))))
      d)))

(defn doc-doc [renderer {:keys [doc]}]
  (when-not (str/blank? doc)
    (let [d (adjust-docstring-indent doc)
          body (condp = renderer
                 :html (str "<pre>" d "</pre>")
                 :text d)]
      (render-section renderer
                      ClojureEditorMessages/DocUtils_doc_label
                      body))))

(defn var-doc-info [renderer m]
  "Renders the info map according to the renderer param, returning nil if sections where empty or
  some issue arises."
  (when-not (empty? m)
    (let [header (header-doc renderer m)
          sections [(arglist-doc renderer m)
                    (doc-doc renderer m)
                    (macro-expansion-doc renderer m)]
          rendered-sections (render-sections
                             renderer
                             sections)
          info-string (str header
                           (when-not (str/blank? rendered-sections)
                             (str "\n\n" rendered-sections)))]
      (when-not (str/blank? info-string)
        info-string))))

(defn var-doc-info-html [m]
  "Renders the sections using html, returning nil if sections where empty or some issue arises."
  (var-doc-info :html m))

(defn var-doc-info-text [m]
  "Renders the sections using plain text, returning nil if sections where empty or some issue
  arises."
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
