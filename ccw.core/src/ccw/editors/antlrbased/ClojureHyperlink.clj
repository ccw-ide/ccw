(ns ccw.editors.antlrbased.ClojureHyperlink
  (:import [org.eclipse.jface.text IRegion]
           [org.eclipse.jface.text.hyperlink IHyperlink])
  (:gen-class
    :implements [org.eclipse.jface.text.hyperlink.IHyperlink]
   ; :constructors {[]} ;:constructors {[param-types] [super-param-types], ...}
   ; :init init 
;   :init name
;
;  If supplied, names a function that will be called with the arguments
;  to the constructor. Must return [ [superclass-constructor-args] state] 
;  If not supplied, the constructor args are passed directly to
;  the superclass constructor and the state will be nil
    :state state))

#_(defn -init
  [] [[] nil])

(defn ^IRegion -getHyperlinkRegion
  [this])

(defn ^String -getHyperlinkText
  [this])

(defn ^String -getTypeLabel
  [this])

(defn -open
  [this])
