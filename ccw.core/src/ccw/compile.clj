(ns ccw.compile)

(defn all []
  (dorun   
    (map
      compile
      ['ccw.ClojureProjectNature
       'ccw.debug.clientrepl
       'ccw.debug.serverrepl
       'ccw.editors.antlrbased.PareditAutoEditStrategy])))

