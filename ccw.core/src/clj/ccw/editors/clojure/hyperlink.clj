(ns ccw.editors.clojure.hyperlink)

(defn make [region, open]
  (reify org.eclipse.jface.text.hyperlink.IHyperlink
    (getHyperlinkRegion [this] region)
    (getTypeLabel [this] "static hyperlink label")
    (getHyperlinkText [this] "static hyperlink text")
    (open [this] (open))))