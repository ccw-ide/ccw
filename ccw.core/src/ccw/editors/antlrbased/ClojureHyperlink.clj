(ns ccw.editors.antlrbased.ClojureHyperlink
  (:import [org.eclipse.jface.text IRegion]
           [org.eclipse.jface.text.hyperlink IHyperlink])
  (:gen-class
    :implements [org.eclipse.jface.text.hyperlink.IHyperlink]
    :constructors {[org.eclipse.jface.text.IRegion Object] []}
    :init init 
    :state state))

(defn state-val [this] (-> this .state deref))

(defn -init
  [region f] [[] (ref {:region region :open f})])

(defn ^IRegion -getHyperlinkRegion
  [this] (:region (state-val this)))

(defn ^String -getHyperlinkText
  [this] "static hyperlink text")

(defn ^String -getTypeLabel
  [this] "static hyperlink label")

(defn -open
  [this] ((:open (state-val this))))
