(ns ccw.editors.clojure.hyperlink
  "Provides functions for creating text editors Hyperlinks."
  (:import  [org.eclipse.jface.text IRegion
                                    Region] 
            [org.eclipse.jface.text.hyperlink IHyperlink]
            [ccw.editors.clojure IClojureEditor
                                 AbstractHyperlinkDetector]))

(defn make
  "creates an Eclipse IHyperlink from a clojure map
   {:region [offset length]
    :open   no-arg-fn-which-will-open-the-hyperlink-target
   }"
  [{[offset length] :region, :keys [open label text]}]
  (reify org.eclipse.jface.text.hyperlink.IHyperlink
    (getHyperlinkRegion [this] (Region. offset length))
    (getTypeLabel [this] (or label "static hyperlink label"))
    (getHyperlinkText [this] (or text "static hyperlink text"))
    (open [this] (open))))

;; TODO IEditorCoercible in ccw.eclipse ?
(defn editor [^AbstractHyperlinkDetector this] (.getClassAdapter this IClojureEditor))

(def detectors
  "Set of detectors that have been registered by the application statically and dynamically"
  (atom #{}))

(defn factory
  "Factory creating IHyperlink detector
   hyperlink-detect-hyperlinks is a String representing a namespaced symbol of a
   var holding a function taking a region [offset length], which is the 'zone'
   to scan for hyperlinks, and the Clojure Editor to look into.
   hyperlink-detector must return a collection of hyperlinks.
   An hyperlink is a map with the following keys:
   {[:offset  Start offset of the hyperlink
     :length] length of the hyperlink
    :open     no-arg function to call if the hyperlink is clicked
   }"
  [ {:strs [detect-hyperlinks]} ]
  (require (-> detect-hyperlinks symbol namespace symbol))
  (let [detect-hyperlinks (resolve (symbol detect-hyperlinks))]
    (swap! detectors conj detect-hyperlinks)
    (proxy [AbstractHyperlinkDetector]
          []
     (detectHyperlinks [textViewer ^IRegion region canShowMultipleHyperlinks?]
       (when-let [editor (editor this)]
         (when-let [hyperlinks (detect-hyperlinks [(.getOffset region) (.getLength region)] editor)] 
          (into-array IHyperlink (map make hyperlinks))))))))

(defn detect-hyperlinks
  "Searches hyperlinks through all registered detectors, and concatenates the result"
  [region editor]
  (when editor
    (when-let [hyperlinks (mapcat (fn [detector] (detector region editor)) detectors)] 
     (into-array IHyperlink (map make hyperlinks)))))
