(ns ccw.schema.core
  "Additional schema types"
  (:require [schema.core :as s]
            [schema.macros :refer (validation-error)]))

(clojure.core/defrecord Deref [schema] 
  s/Schema 
  (walker [this] 
          (let [sub-walker (s/subschema-walker schema)] 
            (clojure.core/fn [x] 
              (if (instance? clojure.lang.IDeref x) 
                (sub-walker @x)
                (validation-error this x (list 'instance? clojure.lang.IDeref x)))))) 
  (s/explain [this] (list 'reference (s/explain schema))))

(clojure.core/defn deref
  "A value that must be dereferenced (delay, var, ref, atom, future, etc.)" 
  [schema] 
  (Deref. schema))
