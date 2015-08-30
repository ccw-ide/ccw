(ns ccw.schema.core
  "Additional schema types"
  (:require [schema.core :as s]
            [schema.macros :refer (validation-error)]))

(clojure.core/defrecord Reference [schema] 
  s/Schema 
  (walker [this] 
          (let [sub-walker (s/subschema-walker schema)] 
            (clojure.core/fn [x] 
              (if (instance? clojure.lang.IRef x) 
                (sub-walker @x)
                (validation-error this x (list 'instance? clojure.lang.IRef x)))))) 
  (s/explain [this] (list 'reference (s/explain schema))))

(clojure.core/defn reference 
  "A value that must be a reference value (var, ref, atom, future, etc.)" 
  [schema] 
  (Reference. schema))
