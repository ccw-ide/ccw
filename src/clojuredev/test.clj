;; test

(defn sum [ & more ]
  ((fn [total other]
     (if other
       (recur (+ total (first other)) (rest(other)))
       total))
   0 more))