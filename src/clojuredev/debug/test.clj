(binding [*ns* (create-ns 'user)] 
  (eval '((fn [_] (ns foo3) (def bar3 2)) 1)) nil)