(ccw.util.bundle/set-bundle-classloader! "ccw.core")
(require '[ccw.util.eclipse :as e])
;(require '[ccw.leiningen.launch :as launch])

(defn hello-world [ _ ]
  (e/info-dialog "Test" "Hello, world 7"))

#_(e/define-command! {
  :id :hello1
  :name "hello1"
  :handler #'hello-world})

(require '[ccw.util.bundle :as b])
(b/add-command! 
  (b/bundle "ccw.core")
  {"id" "hello7" "name" "HELLO 7"})

(e/define-handler! {
  :command-id :hello7
  :fn #'hello-world})

(e/ui
  (e/save-key-binding! 
    (e/key-binding {
      :key-sequence "COMMAND+D"
      :command :hello7
      :context-id :ccw.ui.clojureEditorScope})))

#_(defn launch-lein [e]
  (e/info-dialog "Launching leiningen" "Launching lein2")
  #_(launch/lein "paredit.clj" "classpath"))

#_(e/define-command! {
  :id :lein, 
  :name "Run arbitrary Leiningen command"
  :handler #'launch-lein})

