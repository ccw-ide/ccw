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

(defn- reified-string-param
  [param]
  (let [s (str param)
        i (.lastIndexOf s ".")]
    (clojure.string/lower-case (if (> i -1) (.substring s (inc i)) s))))

(defn- reified-string-method
  [method-member]
  (str "(" (:name method-member) " [this"
       (when-let [parameters (seq (:parameter-types method-member))]
         (str " " (clojure.string/join " " (map reified-string-param parameters)))) "]" \newline "  )" \newline \newline))

(defn- reified-string-class
  [class-or-instance]
  {:pre [(not (nil? class-or-instance))]}
  (let [clazz (if (class? class-or-instance) class-or-instance (class class-or-instance))
        methods (->> (:members (reflect clazz)) (filter :return-type))]
    (str "  " (-> (.getSimpleName clazz))
         \newline
         "  "
         (clojure.string/join "  " (map reified-string-method methods)))))

(defn reified-string
  "Return a string which will contain the reified version of the input
  type. This string is not supposed to be used directly as form (reify
  ...) as it may contain duplicate parameters (because the parameter
  name is stringified from the type name). It can be used but it needs
  some adjustment."
  [& classes-or-instances]
  (str "(reify"
       \newline
       (clojure.string/join (map reified-string-class classes-or-instances))
       ")"))
