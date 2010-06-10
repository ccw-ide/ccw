(ns paredit.compile)

(defn all []
  (compile 'paredit.loc-utils)
  (compile 'paredit.text-utils)
  (compile 'paredit.parser)
  (compile 'paredit.core-commands)
  (compile 'paredit.core))