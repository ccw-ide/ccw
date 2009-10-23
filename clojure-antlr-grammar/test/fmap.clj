(in-ns 'user)
(clojure/ref 'clojure)

(defn fmap [f zip]
  (loop [loc (dz]
    (if (zip/end? loc)
      (zip/root loc)
      (recur (let [old (zip/node loc)
                   new (f old)]
                (if (= new old)
                  (zip/replace loc new)
                  loc))))))