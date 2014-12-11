(ns ccw.api.markers
  "API for managing problem markers in Eclipse."
  (:require [ccw.eclipse :as e])
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
  {:ccw-compiler ccw.builder.ClojureBuilder/CLOJURE_COMPILER_PROBLEM_MARKER_TYPE
   ; ....
   })

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
     {:type :ccw-compiler
      :severity :error
      :char-start 0 :char-end 1
      :message \"Dynamically created, how cool is that?\"})"
  [resource marker-map]
  (let [type (type-map (:type marker-map) (:type marker-map))
        attrs (java.util.HashMap.)]
     (doseq [[k v] (dissoc marker-map :type)]
       (let [f (keyword-to-attr-key k)]
         (f attrs v)))
     (MarkerUtilities/createMarker
       (e/resource resource)
       attrs
       type)))

(defn delete-markers!
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
