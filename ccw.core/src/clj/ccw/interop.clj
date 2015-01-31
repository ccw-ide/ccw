(ns
  ^{:author "Andrea Richiardi"
    :doc "Bridge helpers between Clojure and Java."}
ccw.interop
  (:require [clojure.reflect :refer [reflect]]
            [clojure.string :refer [join]]))

(defn canonical-name
  "Return the class' canonical name of an instance, or nil if the input is nil."
  [^java.lang.Object instance]
  (some-> instance .getClass .getCanonicalName))

(defn simple-name
  "Return the class' simple name of an instance, or nil if the input is nil."
  [^java.lang.Object instance]
  (some-> instance .getClass .getSimpleName))

(defn arities
  "Returns the parameter count of each invoke method of the input f.
   This function does NOT detect variadic functions."
  [f]
  {:pre [f]}
  (let [invokes (filter
                  #(= "invoke" (.getName %1))
                  (-> f class .getDeclaredMethods))]
    (map #(alength (.getParameterTypes %1)) invokes)))

(defn methods-of [instance & string]
  "Returns the \"[...] public member methods of the class or interface represented by this Class
   object, including those declared by the class or interface and those inherited from superclasses
   and superinterfaces.\". See java.lang.Class/getMethods for details.

   If other arguments are provided as valid regex or string, the list is filtered according to it."
  {:pre [instance]}
  (set (filter
           #(re-find (re-pattern (or (join "|" string) #".*")) %)
           (map #(.getName %) (-> instance class .getMethods)))))

(defn- reified-param
  [param]
  (let [s (str param)
        i (.lastIndexOf s ".")]
    (clojure.string/lower-case (if (> i -1) (.substring s (inc i)) s))))

(defn- reified-method
  [method-member]
  (str "(" (:name method-member) " [this"
       (when-let [parameters (seq (:parameter-types method-member))]
         (str " " (clojure.string/join " " (map reified-param parameters)))) "]" \newline "  )" \newline \newline))

(defn reified-string
  "Return a string which will contain the reified version of the input type. This string is not supposed
   to be used directly as form (reify ...), it may contain duplicates (for instance, all the parameter names
   will be the same. It can be used as a template that needs some adjustment."
  [obj]
  {:pre [(not (nil? obj))]}
  (let [clazz (if (class? obj) obj (class obj))
        methods (->> (:members (reflect clazz)) (filter :return-type))]
    (str "(reify"
         \newline
         "  " (-> (.getSimpleName clazz))
         \newline
         "  "
         (clojure.string/join "  " (map reified-method methods)) ")")))
