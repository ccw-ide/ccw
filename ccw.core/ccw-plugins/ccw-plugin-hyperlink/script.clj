(require '[ccw.bundle :as b])
(b/expect-bundle "ccw.core" "0.33.1")

(ns ccw-plugin-test-hyperlinks
  (:require [ccw.api.hyperlink :as hyperlink]
            [ccw.eclipse       :as e]
            [ccw.editors.clojure.editor-common :as editor]))

(defn detect-hyperlinks
  [[offset length :as region] editor]
  (when-let [{:keys [offset text]} (editor/string-literal-body editor offset)]
    (let [make-link (fn [o] {:text (str "Open " o)
                             :region [offset (count text)]
                             :open #(e/open-editor o)}) 
          paths (->> e/file-find-strategies
                  (mapcat #(% text (e/resource editor)))
                  (keep identity)
                  distinct)]
      (mapv make-link paths))))

 (hyperlink/register-hyperlink-detector!
   {:detect-hyperlinks #'detect-hyperlinks})
