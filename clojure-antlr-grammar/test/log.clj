(def *time-counter* {})

(defmacro time-wrap [n-fn-symb]
  '(let [fn-symb n-fn-symb
          old# ~fn-symb]
     (def ~fn-symb
       (fn [ & args# ]
         (let [getTime# #(.. java.util.Calendar getInstance getTime getTime)
                initial# (getTime#)
                result# (apply old# args#)
                final# (getTime#)
                timeMillis# (- final# initial#)]
            (def *time-counter* 
                 (assoc *time-counter* 
                         (quote ~fn-symb) 
                         (+ (get *time-counter* (quote ~fn-symb) 0) 
                            timeMillis#)))
            result#)))))
