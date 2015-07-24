(ns ccw.swt
  (:refer-clojure :exlude (dosync))
  (:import ccw.util.DisplayUtil))

(import 'org.eclipse.swt.SWT)
(import 'org.eclipse.swt.layout.FormAttachment)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Layout: FormLayout
;; doc.: http://www.eclipse.org/articles/article.php?file=Article-Understanding-Layouts/index.html
(def alignments {:top SWT/TOP, :bottom SWT/BOTTOM, :left SWT/LEFT, :right SWT/RIGHT, :default SWT/DEFAULT})
(defn as-alignment [a] (or (alignments a) a))
(def form-attachment-setter 
  {:numerator    #(set! (.numerator %1) %2)
   :denominator  #(set! (.denominator %1) %2)
   :alignment    #(set! (.alignment %1) (as-alignment %2))
   :control      #(set! (.control %1) %2)
   :offset       #(set! (.offset %1) %2)
   :pct          #(do (set! (.numerator %1) %2)
                      (set! (.denominator %1) 100))})
(defn form-attachment 
  [spec] 
  (let [spec (merge {:offset 0 :alignment :default} spec)
        fa (FormAttachment.)]
    (doseq [[attr val] spec]
      ((form-attachment-setter attr) fa val))
    fa))

(import 'org.eclipse.swt.layout.FormData)
(def form-data-setters 
  {:width   #(set! (.width %1) %2) 
   :height  #(set! (.height %1) %2)
   :bottom  #(set! (.bottom %1) (form-attachment %2))
   :top     #(set! (.top %1) (form-attachment %2))
   :left    #(set! (.left %1) (form-attachment %2))
   :right   #(set! (.right %1) (form-attachment %2))})
(defn form-data [& {:as spec}]
  (let [fd (FormData.)]
    (doseq [[attr val] spec]
      ((form-data-setters attr) fd val))
    fd))

(import 'org.eclipse.swt.layout.FormLayout)
(def form-layout-setters 
  {:margin-bottom #(set! (.marginBottom %1) %2)
   :margin-height #(set! (.marginHeight %1) %2)
   :margin-left   #(set! (.marginLeft   %1) %2)
   :margin-right  #(set! (.marginRight  %1) %2)
   :margin-top    #(set! (.marginTop    %1) %2)
   :spacing       #(set! (.spacing      %1) %2)})

(defn form-layout [& {:as spec}]
  (let  [fl (FormLayout.)]
    (doseq [[attr val] spec]
      ((form-layout-setters attr) fl val))
    fl))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Layout: FlowLayout
(import '[org.eclipse.swt.layout FillLayout])
(defn fill-layout 
  ([] (FillLayout.))
  ([{:keys [margin]}]
    (if margin
      (let [f (fill-layout)]
        (set! (.marginWidth f) margin)
        (set! (.marginHeight f) margin)
        f)
      (fill-layout))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Event Mgt
(import '[org.eclipse.swt.widgets Listener])
(import '[org.eclipse.swt.events KeyListener])

(defn listener* [f] 
  (reify Listener
    (handleEvent [this event] (f event))))

(defmacro listener [event-name & body]
  `(listener* (fn [~event-name] ~@body)))

(defn key-listener* 
  [f]
  (reify KeyListener 
    (keyPressed [this event])
    (keyReleased [this event] (f event))))

(defmacro key-listener [event-name & body]
  `(key-listener* (fn [~event-name] ~@body)))

;;;;;;
;;;;;;
(import 'org.eclipse.swt.widgets.Display)
(import 'org.eclipse.swt.widgets.Shell)

(defn display [] (Display/getCurrent))

(defn active-shell []
  (-> (org.eclipse.swt.widgets.Display/getDefault)
    .getActiveShell))

(defn doasync*
  "function f will be called on the UI Thread asynchronously.
   Non-blocking operation.
   Returns a promise to which the return value of f, or the exception
   thrown by f, will be delivered.
   Note: use the promise with care, or you could create deadlocks
   (especially if you call doasync* from the UI thread and wait there
   for the promise to be delivered! - won't ever have a chance to happen)"
  [f]
  (let [a (promise)]
    (DisplayUtil/asyncExec
      #(deliver
         a
         (try
           (f)
           (catch Exception e e))))
    a))

(defn dosync*
  "function f will be called on the UI Thread either right away if the current
   thread is already the UI thread, either asap asynchronously.
   In any case, will block until f has executed.
   Returns the value returned by the body, or rethrows the thrown exception"
  [f]
  (let [a (promise)
        e (promise)]
    (DisplayUtil/syncExec
      #(try
         (deliver a (f))
         (catch Exception exc (deliver e exc))))
    (if (realized? e)
      (throw e)
      (deref a))))

(defmacro doasync
  "body will be executed on the UI Thread asynchronously.
   Non-blocking operation.
   Returns a promise to which the return value of the body excution, or the exception
   thrown while executing the body, will be delivered.
   Note: use the promise with care, or you could create deadlocks
   (especially if you call doasync* from the UI thread and wait there
   for the promise to be delivered! - won't ever have a chance to happen)"
  [& body]
  `(doasync* (fn [] ~@body)))
  
(defmacro dosync
  "body will be executed on the UI Thread either right away if the current
   thread is already the UI thread, either asap asynchronously.
   In any case, will block until the body has executed.
   Returns the value returned by the body, or rethrows the thrown exception"
  [& body]
  `(dosync* (fn [] ~@body)))


(defn beep
  "Executes a beep sound"
  []
  (DisplayUtil/beep))

;; Shell SWT.<> Styles:
; BORDER, CLOSE, MIN, MAX, NO_TRIM, RESIZE, TITLE, ON_TOP, TOOL, SHEET
; APPLICATION_MODAL, MODELESS, PRIMARY_MODAL, SYSTEM_MODAL
(defn new-shell 
  ([] (new-shell (display)))
  ([display & options] ; Subject to change 
    (Shell. display (apply bit-or options))))
