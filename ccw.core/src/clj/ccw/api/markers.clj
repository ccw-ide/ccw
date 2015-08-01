(ns ccw.api.markers
  "API for managing problem markers in Eclipse.
   References:
   - https://www.eclipse.org/articles/Article-Mark%20My%20Words/mark-my-words.html"
  (:require [ccw.eclipse :as e]
            [ccw.bundle :as b]
            [ccw.core.trace :as t])
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

(def ^:private type-ids
  "Map containing standard eclipse marker types, as well as those provided
   by ccw by default."
  {:marker   IMarker/MARKER ; base marker
   :problem  IMarker/PROBLEM
   :task     IMarker/TASK
   :text     IMarker/TEXT
   :bookmark IMarker/BOOKMARK
   :ccw      ccw.builder.ClojureBuilder/CLOJURE_COMPILER_PROBLEM_MARKER_TYPE
   :all      nil})

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

(defn- eclipse-type-id
  "If type-id is nil then it represents all type-ids. Not all places where a type-id is expected accept nil.
   If type-id is a keyword, ccw.api.markers/type-ids will be used to resolve the eclipse-type-id.
   If type-id is a String, it will be returned as-is or with \"ccw.markers.\" preprended if it contains no dot (Eclipse work-around).
   In case type-id is a keyword missing from ccw.api.markers/type-ids or anything else, will throw an exception."
  [type-id]
  (let [r (letfn [(throw-ex [type-id] (throw (ex-info (str "unknown ccw.api.markers type id " type-id))))]
          (cond
            (nil? type-id)     nil
            (keyword? type-id) (if (contains? type-ids type-id)
                                 (type-ids type-id)
                                 (throw-ex type-id))
            (string? type-id)  (if (.contains type-id ".")
                                 type-id
                                 (str "ccw.markers." type-id))
            :else (throw-ex type-id)))]
    r))

(defn create-marker!
  "Create a marker for the given resource-coercible.
   the marker map must contain keys :type, :severity, :message,
   either :line-number (starting at 1) or :char-start (starting at 0) + :char-end

   Example:
   (ma/create-marker!
     (e/workspace-resource \"ccw.core/src/clj/ccw/api/markers.clj\")
     {:type-id \"ccw-plugin-xyz\"
      :severity :error
      :char-start 0 :char-end 1
      :message \"Dynamically created, how cool is that?\"})"
  [resource marker-map]
  (let [type-id (eclipse-type-id (:type-id marker-map))
        attrs (java.util.HashMap.)]
     (doseq [[k v] (dissoc marker-map :type-id)]
       (let [f (keyword-to-attr-key k)]
         (f attrs v)))
     (MarkerUtilities/createMarker
       (e/resource resource)
       attrs
       type-id)))

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
  ([resource type-id]
    (delete-markers! resource type-id true))
  ([resource type-id include-subtypes]
    (delete-markers! resource type-id include-subtypes :infinite))
  ([resource type-id include-subtypes depth]
    (.deleteMarkers
      (e/resource resource)
      (eclipse-type-id type-id) ; it is ok to call deleteMarkers with type-id set to nil (delete all markers)
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

(defn- reset-marker-MarkerTypesModel!
  "The Eclipse Marker API does not dynamically update its marker type list when
   we dynamically register a new marker type extension.
   This function is a work-around that reinjects a newly created cache into
   the Eclipse MarkerTypesModel."
  []
  (let [instance-field (.getDeclaredField org.eclipse.ui.views.markers.internal.MarkerTypesModel "instance")]
    (.setAccessible instance-field true)
    (.set instance-field nil nil)))

(defn- reset-marker-types!
  []
  (reset-marker-manager-cache!)
  (reset-marker-MarkerTypesModel!))

(defn- as-extension
  "Transforms a map defining a type into a String representing a valid Eclipse
   xml extension."
  [type-def]
  (b/xml-map->extension 
    {:tag "extension"
     :attrs {:point "org.eclipse.core.resources.markers"
             :id (eclipse-type-id (:type-id type-def))
             :name (or (:name type-def) (eclipse-type-id (:type-id type-def)) "<unknown>")}
     :content (into []
                (concat
                  [{:tag "persistent"
                    :attrs {:value (if (:persistent type-def)
                                     "true"
                                     "false")}}]
                  (map
                    (fn [parent-type-id]
                      {:tag "super"
                       :attrs {:type (eclipse-type-id parent-type-id)}})
                    (or (seq (:parents type-def))
                      [:ccw]))))}))

(defn- find-marker-extensions-in-registry
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
   (register-marker-type! {:id \"ccw-plugin-foo-my-fun-id\"})

   Example 2: create a marker type deriving directly from the eclipse problem marker
               type (but not ccw type), and make it persistent across Eclipse restarts 
   (register-marker-type!
     {:id \"ccw-plugin-my-other-id\", :persistent true, :parents [:problem]})"
  ([type-def] (register-marker-type! (b/bundle "ccw.core") type-def))
  ([bundle type-def]
    (b/remove-extension! (eclipse-type-id (:type-id type-def)))
    (let [ext-str (as-extension type-def)]
      (b/add-contribution!
        (or (:name type-def) (eclipse-type-id (:type-id type-def)))
        ext-str
        bundle
        true ; we must force persistence, or markers disappear when Eclipse restarts because the extension would not be reused
        (b/master-token)))
    (reset-marker-types!)))
