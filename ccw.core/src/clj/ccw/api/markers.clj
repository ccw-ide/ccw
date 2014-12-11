(ns ccw.api.markers
  "API for managing problem markers in Eclipse."
  (:require [ccw.eclipse :as e]
            [ccw.bundle :as b])
  (:import [org.eclipse.ui.texteditor MarkerUtilities]
           [org.eclipse.core.resources IMarker IResource]))

(def severity
  "Standard severity values recognized by Eclipse."
  {:error   IMarker/SEVERITY_ERROR
   :warning IMarker/SEVERITY_WARNING
   :info    IMarker/SEVERITY_INFO})

(def keyword-to-attr-key
  "Standard marker attributes recoginzed by Eclipse.
   :line-number is mutually exclusive with :char-start + :char-end
   :line-number considers first line number is 1
   :char-start / :char-end consider first char is at index 0
   :message must be a string
   :severity must be a keyword from the severity map keys, or a String"
  {:line-number (fn [m v] (MarkerUtilities/setLineNumber m v))
   :char-start  (fn [m v] (MarkerUtilities/setCharStart m v))
   :char-end    (fn [m v] (MarkerUtilities/setCharEnd m v))
   :message     (fn [m v] (MarkerUtilities/setMessage m v))
   :severity    (fn [m v] (.put m IMarker/SEVERITY (severity v v)))})

(def ^:private type-map
  "Map containing standard eclipse marker types, as well as those provided
   by ccw by default."
  {:ccw ccw.builder.ClojureBuilder/CLOJURE_COMPILER_PROBLEM_MARKER_TYPE
   :marker IMarker/MARKER ; base marker
   :problem IMarker/PROBLEM
   :task IMarker/TASK
   :text IMarker/TEXT
   :bookmark IMarker/BOOKMARK})

(def depth-map
  "Used to declare how deep below a given resource (a project or a folder,
   or the workspace root) should a command apply.
   :zero apply the command only to the resource itself
   :one  apply the command to the resource itself, and its direct children
   :infinite apply the command to the resource itself, and all its children
             direct or indirect transitively"
  {:zero     IResource/DEPTH_ZERO
   :one      IResource/DEPTH_ONE
   :infinite IResource/DEPTH_INFINITE})

(defn create-marker!
  "Create a marker for the given resource-coercible.
   the marker map must contain keys :type, :severity, :message,
   either :line-number (starting at 1) or :char-start (starting at 0) + :char-end

   Example:
   (ma/create-marker!
     (e/workspace-resource \"ccw.core/src/clj/ccw/api/markers.clj\")
     {:type \"ccw-plugin-xyz\"
      :severity :error
      :char-start 0 :char-end 1
      :message \"Dynamically created, how cool is that?\"})"
  [resource marker-map]
  (let [type (type-map (:type marker-map)
               (if-not (.contains (:type marker-map) ".")
                 (str (.getSymbolicName (b/bundle "ccw.core")) "." (:type marker-map))
                 (:type marker-map)))
        attrs (java.util.HashMap.)]
     (doseq [[k v] (dissoc marker-map :type)]
       (let [f (keyword-to-attr-key k)]
         (f attrs v)))
     (MarkerUtilities/createMarker
       (e/resource resource)
       attrs
       type)))

(defn delete-markers!
  "Delete markers for the resource.
   resource: a resource or something coercible to a resource
   marker-type: the type of marker to delete. Optional, if not set or set to nil,
                all markers will be considered.
   include-subtypes: if true, all subtypes of marker-types will also be considered
   depth: for container markers (workspace root, project, folder), indicates how
          deep below it should the deletion broaden."
  ([resource]
    (delete-markers! resource nil))
  ([resource marker-type]
    (delete-markers! resource marker-type true))
  ([resource marker-type include-subtypes]
    (delete-markers! resource marker-type include-subtypes :infinite))
  ([resource marker-type include-subtypes depth]
    (.deleteMarkers
      (e/resource resource)
      (type-map marker-type marker-type)
      include-subtypes
      (depth-map depth depth))))

(defn- reset-marker-manager-cache!
  "The Eclipse Marker API does not dynamically update its marker type list when
   we dynamically register a new marker type extension.
   This function is a work-around that reinjects a newly created cache into
   the Eclipse Marker Manager."
  []
  (let [mm (.getMarkerManager (e/workspace))
         cache-field (.getDeclaredField org.eclipse.core.internal.resources.MarkerManager "cache")
         new-cache (org.eclipse.core.internal.resources.MarkerTypeDefinitionCache.)]
    (.setAccessible cache-field true)
    (.set cache-field mm new-cache)))

(defn- as-extension
  "Transforms a map defining a type into a String representing a valid Eclipse
   xml extension."
  [type-def]
  (b/xml-map->extension 
    {:tag "extension"
     :attrs {:point "org.eclipse.core.resources.markers"
             :id (:id type-def)
             :name (or (:name type-def) (:id type-def))}
     :content (into []
                (concat
                  [{:tag "persistent"
                    :attrs {:value (if (:persistent type-def)
                                     "true"
                                     "false")}}]
                  (map
                    (fn [parent]
                      {:tag "super"
                       :attrs {:type (type-map parent parent)}})
                    (or (seq (:parents type-def))
                      [:ccw]))))}))

(defn register-marker-type!
  "Given the type definition map, registers the marker type with Eclipse
   Registry. Uses bundle as the declaring namespace, or ccw.core if no bundle
   specified.
   type-def keys:
     :id - mandatory. The unique identifier for the marker type. Do not use dots in the id.
     :name - optional, by default equals to :id. Useful for debugging purposes.
     :persistent - optional, defaults to false.
                   If true, the markers of this type will be persisted across
                   Eclipse restarts.
     :parents - a sequence, optional, defaults to ccw markers (so that project > clean will
                always remove all ccw or ccw-plugin markers).

   Example 1: create a new marker type, non persistent across Eclipse restarts
   (register-marker-type! {:id \"ccw-plugin-foo.my-fun-id\"})

   Example 2: create a marker type deriving directly from the eclipse problem marker
               type (but not ccw type), and make it persistent across Eclipse restarts 
   (register-marker-type!
     {:id \"ccw-plugin-my-other-id\", :persistent true, :parents [:problem]})"
  ([type-def] (register-marker-type! (b/bundle "ccw.core") type-def))
  ([bundle type-def]
    (when (.contains (:id type-def) ".")
      (throw (RuntimeException. (str "id" (:id type-def) " must not contain dots"))))
    (b/remove-extension! (str (.getSymbolicName bundle) "." (:id type-def)))
    (let [ext-str (as-extension type-def)]
      (b/add-contribution!
        ext-str
        bundle
        true ; we must force persistence, or markers disappear when Eclipse restarts
        (b/master-token)))
    (reset-marker-manager-cache!)))

(defn find-marker-extensions-in-registry
  "Find all marker extensions whose unique name contain the string s.
   Useful for debuggin in the REPL."
  [s]
  (->> (.getExtensionPoint
         (org.eclipse.core.runtime.Platform/getExtensionRegistry)
         "org.eclipse.core.resources"
         "markers")
     .getExtensions
     (filter #(.contains (.getUniqueIdentifier %) s))
     (map #(.getUniqueIdentifier %))))

(comment
  
(require '[ccw.api.markers :refer (register-marker-type! create-marker!)])
(require '[ccw.eclipse     :refer (workspace-resource)])

(register-marker-type! {:id "ccw-plugin-clj-lint", :persistent true})

(create-marker!
  (workspace-resource "my-project/src/bar/foo.clj")
  {:type        "ccw-plugin-clj-lint"
   :severity    :warning
   :line-number 1
   :message     "The variable baz is unused."})

(ma/delete-markers! (e/workspace-resource "my-project"))

)

