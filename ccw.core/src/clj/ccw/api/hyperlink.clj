;; TODO harmoniser pluriel ou singulier entre hyperlink et marker
(ns ccw.api.hyperlink
  "Provides functions for creating text editors Hyperlinks.

   Usage Example:
   
   (ns ccw-plugin-hyperlink-example
     (:require [ccw.api.hyperlink :as hyperlink]
               [ccw.eclipse       :as e]))
   
   (defn detect-hyperlinks
     [[offset length :as region] editor]
     [{:region region
       :open #(e/info-dialog \\\"Hyperlink Detected\\\"
                (str \\\"The following text might be an hyperlink:\\\"
                  (-> editor .getDocument (.get offset length)))))}])
   
   (hyperlink/register-hyperlink-detector! {:detect-hyperlinks #'detect-hyperlinks})"
  (:require [ccw.bundle :as b]
            [ccw.eclipse :as e])
  (:import  [org.eclipse.jface.text IRegion
                                    Region] 
            [org.eclipse.jface.text.hyperlink IHyperlink]
            [ccw.editors.clojure IClojureEditor
                                 AbstractHyperlinkDetector]))

(defn- make
  "creates an Eclipse IHyperlink from a clojure map
   {:region [offset length]
    :open   no-arg-fn-which-will-open-the-hyperlink-target
    :text   optional text which can help choose the right hyperlink if more than 1 are suggested
   }"
  [{[offset length] :region, :keys [open label text]}]
  (reify org.eclipse.jface.text.hyperlink.IHyperlink
    (getHyperlinkRegion [this] (Region. offset length))
    (getTypeLabel [this] (or label "static hyperlink label"))
    (getHyperlinkText [this] (or text "static hyperlink text"))
    (open [this] (open))))

(defn- editor [^AbstractHyperlinkDetector this] (.getClassAdapter this IClojureEditor))

(defn- factory
  "Factory creating IHyperlink detector
   hyperlink-detect-hyperlinks is a String representing a namespaced symbol of a
   var holding a function taking a region [offset length], which is the 'zone'
   to scan for hyperlinks, and the Clojure Editor to look into.
   hyperlink-detector must return a collection of hyperlinks.
   An hyperlink is a map with the following keys:
   {[:offset  Start offset of the hyperlink
     :length] length of the hyperlink
    :open     no-arg function to call if the hyperlink is clicked}"
  [ {:strs [detect-hyperlinks]} ]
  (require (-> detect-hyperlinks symbol namespace symbol))
  (let [detect-hyperlinks (resolve (symbol detect-hyperlinks))]
    (proxy [AbstractHyperlinkDetector]
          []
     (detectHyperlinks [textViewer ^IRegion region canShowMultipleHyperlinks?]
       (when-let [editor (editor this)]
         (when-let [hyperlinks (detect-hyperlinks [(.getOffset region) (.getLength region)] editor)] 
          (let [a (into-array IHyperlink (map make hyperlinks))]
            (when (< 0 (count a)) a))))))))

(def key->target-id
  "Map containing standard eclipe & clojure hyperlink target-ids for clojure, java, text editors"
  {:clojure "ccw.ui.clojureCode"
   :java    "org.eclipse.jdt.ui.javaCode"
   :text    "org.eclipse.ui.DefaultTextEditor"
   })

(defn- as-extension
  "Transforms a map defining an hyperlink detector into a String representing a valid Eclipse xml extension.
  {:detect-hyperlinks must point to a var containing a function of a text region (a vector of offset length) and an IClojureEditor,
                      and must return a coll of hyperlinks data in the form {:region [offset length] :open single-arg-fn-opening-the-hyperlink} 
   :id         Optional Id of the extension (if not specified, will be derived from current ns and target-id 
   :name       Optional Name of the extension (if not specified, will be derived from id, current ns, target-id, and will display :detect-hyperlink
   :target-id  Optional clojure keyword (looked up in key->target-id map, or direct String representation of the target editor for the hyperlink)
   }"
  [{:keys [target-id, id, name, detect-hyperlinks] :as hd-def}]
  (let [detect-hyperlinks (if (string? detect-hyperlinks) detect-hyperlinks (str (:ns (meta detect-hyperlinks)) "/" (:name (meta detect-hyperlinks))))
        target-id (or target-id :clojure)
        eclipse-target-id (key->target-id target-id target-id)
        id (or id (str "ccw.hyperlinkDetector." *ns* "." eclipse-target-id))
        name (str "Hyperlink Detector for " target-id " Editor. id: " id ", ns: " *ns* ", fn:" detect-hyperlinks)]
    {:tag "extension"
     :attrs {:point "org.eclipse.ui.workbench.texteditor.hyperlinkDetectors"
             :id id}
     :content [{:tag "hyperlinkDetector"
                :attrs {:activate true
                        :id id
                        :name name
                        :targetId eclipse-target-id}
                :content [{:tag "class"
                           :attrs {:class "ccw.util.GenericExecutableExtension"}
                           :content [{:tag "parameter"
                                      :attrs {:name "factory"
                                              :value "ccw.api.hyperlink/factory"}}
                                     {:tag "parameter"
                                      :attrs {:name "detect-hyperlinks"
                                              :value detect-hyperlinks}}]}]}]}))

(defn set-method-accessible!
  "Changes the visibility access of a class method.
   method-name is a String.
   Return the method handle for use with invoke-method"
  [class method-name & method-args]
  (let [m (.getDeclaredMethod class method-name (into-array Class method-args))]
    (.setAccessible m true)
    m))

(defn invoke-method
  "invoke m (a java.lang.reflect.Method) for object with method-args arguments"
  [m object & method-args]
  (.invoke m object (into-array Class method-args)))

(defn- reset-hyperlink-detector-caches!
  "The Eclipse Hyperlink API does not dynamically update its hyperlink detectors list when
   we dynamically register a new hyperlin k detector.
   This function is a work-around which reinitializes a private field of HyperlinkDetectorRegistry to nil."
  []
  (let [hyperlink-detector-descriptors-field (.getDeclaredField org.eclipse.ui.texteditor.HyperlinkDetectorRegistry "fHyperlinkDetectorDescriptors")
        hyperlink-registry (-> (org.eclipse.ui.internal.editors.text.EditorsPlugin/getDefault) .getHyperlinkDetectorRegistry)]
    (.setAccessible hyperlink-detector-descriptors-field true)
    (.set hyperlink-detector-descriptors-field hyperlink-registry nil)))

(defn- reset-open-editors-hyperlink-detectors!
  "open editors have a cache of hyperlink detectors. This function finds them, and resets their cache"
  []
  (let [get-source-viewer-configuration (set-method-accessible! org.eclipse.ui.texteditor.AbstractTextEditor "getSourceViewerConfiguration")
        get-source-viewer (set-method-accessible! org.eclipse.ui.texteditor.AbstractTextEditor "getSourceViewer")] 
  (let [editors (e/open-editors e/text-editor)]
    (doseq [e editors
            :let [e                  (e/text-editor e)
                  source-viewer-conf (invoke-method get-source-viewer-configuration e)
                  source-viewer      (invoke-method get-source-viewer e)
                  hyperlink-detectors (.getHyperlinkDetectors source-viewer-conf source-viewer)
                  event-state-mask    (.getHyperlinkStateMask source-viewer-conf source-viewer)]]
      ;; .setHyperlinkDetectors takes care of disposing the previous hyperlink detectors
      (.setHyperlinkDetectors source-viewer hyperlink-detectors event-state-mask)))))

(defn register-hyperlink-detector!
  "Registers an hyperlink detector with Eclipse given hd as the hyperlink detector definition map.
   See function as-extension for what might compose hd-def."
  ([hd-def] (register-hyperlink-detector! (b/bundle "ccw.core") hd-def))
  ([bundle hd-def]
    (let [ext-map (as-extension hd-def)
          ext-str (b/xml-map->extension ext-map)
          id (get-in ext-map [:content 0 :attrs :id])]
      (b/remove-extension! id)
      (b/add-contribution!
        (or (:name hd-def) (:id hd-def) id)
        ext-str
        bundle
        false)
      (reset-hyperlink-detector-caches!)
      (reset-open-editors-hyperlink-detectors!))))

(def get-source-viewer
  "accessible source viewer Method" 
  (set-method-accessible! org.eclipse.ui.texteditor.AbstractTextEditor "getSourceViewer"))

(defn source-viewer [e]
  (invoke-method get-source-viewer e))

(defn detect-hyperlinks
  "Searches hyperlinks through all registered detectors, and returns a list of the candidate.
   This function can be called programmatially, e.g. to react to a key press, etc.
   The returned hyperlinks are clojure maps, not the reified eclipse objects."
  [region editor]
  (when editor
    (.doOperation (source-viewer editor) (org.eclipse.jface.text.hyperlink.HyperlinkManager/OPEN_HYPERLINK))))
