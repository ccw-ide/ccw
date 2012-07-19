(ns ccw.util.doc-utils
  (:require [clojure.string :as str]))

;	potential documentation tags:
; :since, :author, :private, :test, :tag, :file, :line, :ns, :name, 
; :macro, :arglists

(defn indent-str [s]
  (when s (let [offset (- (count s) (count (str/triml s)))]
            (.substring s 0 offset))))

(defn raw-doc-to-html
  "Formats correctly a raw docstring by adding real line breaks between
   2 line breaks, and rejoining lines separated by simple line breaks."
  [doc]
  (;println (str "doc:'" doc "'"))
  (let [lines (str/split-lines doc)
        ;_ (println (str "lines:" lines))
        indent-str (indent-str (second lines))
        reindented-rest (map #(str/replace % indent-str "") (rest lines))
        ;_ (println (str "reindented-reset:" (into [] reindented-rest)))
        lines (cons (first lines) reindented-rest)
        ;_ (println "lines:" (into [] lines))
        lines (map #(if (str/blank? %) "<br/>" %) lines)
        ;_ (println "final lines:" (into [] lines))
        ]
    (str/join lines)))

(defn join 
  "Join c elements with s as of clojure.string/join, except nil elements
   are discarded"
  [s & c]
  (str/join s (keep identity c)))

(defn- arglist-doc [{:keys [arglists]}]
  ;(println "type:" (type arglists))
  (when-not (str/blank? arglists)
    (let [arglists (read-string arglists)]
      (format "<p><b>Argument Lists:</b><br/>%s" 
              (str/join "<br/>" (map pr-str arglists))))))

(defn- optional-meta [{:keys [name macro private dynamic ns tag]}]
  (let [optional-meta (join ", " 
                            (when private "private")
                            (when macro "macro")
                            (when dynamic "dynamic")
                            tag)] 
    (when-not (str/blank? optional-meta)
      (str "(" optional-meta ")"))))

(defn header-doc [{:keys [name macro private dynamic ns] :as m}]
  (println "m:" m)
  (when name
    (join "<br/>"
          (format "<b>%s</b> %s" name ns)
          (optional-meta m))))

(defn doc-doc [{:keys [doc]}]
  (when-not (str/blank? doc)
    (format "<p><b>Documentation:</b><br/>%s</p>"
            (raw-doc-to-html doc))))

(defn var-doc-info-html [m]
  (if m
    (join "<br/>"
          (header-doc m)
          (arglist-doc m)
          (doc-doc m))
    "<i>no doc found</i>"))

(defn var-doc-info [var-meta]
  "var doc info coming soon")


;	
;	public static final Keyword KEYWORD_ARGLISTS = Keyword.intern(null, "arglists");
;	public static final Keyword KEYWORD_DOC = Keyword.intern(null, "doc");
;	
;	public static String getVarDocInfo(Object varObject) {
;		Map<?,?> element = (Map<?,?>) varObject;
;		
;		StringBuilder result = new StringBuilder();
;
;		String args = (String) ((Map<?,?>) element).get(KEYWORD_ARGLISTS);
;		if (args != null && !args.trim().equals("")) {
;			result.append("Arguments List(s):\n");
;			
;			String[] argsLines = args.split("\n");
;			boolean firstLine = true;
;			for (String line: argsLines) {
;				if (line.startsWith("("))
;					line = line.substring(1);
;				if (line.endsWith(")"))
;					line = line.substring(0, line.length() - 1);
;				if (firstLine) {
;					firstLine = false;
;				} else {
;					result.append("<br/>");
;				}
;				result.append(line);
;			}
;		}
;		String maybeDoc = (String) ((Map<?,?>) element).get(KEYWORD_DOC);
;		if (maybeDoc != null) {
;			if (result.length() > 0) {
;				result.append("\n\n");
;			}
;			result.append("Documentation:\n");
;			result.append(maybeDoc);
;		}
;
;		if (result.length() != 0) {
;			return result.toString();
;		} else {
;			return "no documentation information";
;		}
;	}
;	
