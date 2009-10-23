(import '(java.util.concurrent Executors LinkedBlockingQueue))
(defn my-pmap [f coll]
  (let [nthreads (.. Runtime (getRuntime)
                     (availableProcessors))
        exec (. Executors (newFixedThreadPool nthreads))
        todo (ref (seq coll))
        out (ref 0)
        q (new LinkedBlockingQueue)
        produce #(let [job (dosync
                            (when @todo
                              (let [item (first @todo)]
                                (alter todo rest)
                                (commute out inc)
                                (list item))))]
                   (when job
                     (. q (put (f (first job))))
                     (recur)))
        tasks (doseq dnu (map #(. exec (submit %))
                              (replicate nthreads produce)))
        consume (fn thisfn []
                    (if (dosync (and (or @todo (pos? @out))
                                     (commute out dec)))
                      (fnseq (. q (take)) thisfn)
                      (do
                        (. exec (shutdown))
                        (doseq x tasks)
                        nil)))]
    (consume)))