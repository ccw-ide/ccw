;*******************************************************************************
;* Copyright (c) 2015 Laurent PETIT.
;* All rights reserved. This program and the accompanying materials
;* are made available under the terms of the Eclipse Public License v1.0
;* which accompanies this distribution, and is available at
;* http://www.eclipse.org/legal/epl-v10.html
;*
;* Contributors:
;*    Andrea Richiardi - initial implementation (code reviewed by Laurent Petit)
;*******************************************************************************/

(ns ^{:author "Andrea Richiardi" }
  ccw.extensions
  "Wrappers around IConfigurationElement and IExtensionRegistry classes.
   The generated data structure follows Chapter 8 of The Joy of
   Clojure (try xml/emit after extension->map)."
  (:import [org.eclipse.core.runtime IConfigurationElement
                                     IExtensionRegistry
                                     Platform
                                     IRegistryEventListener]))

(defn configuration-elements
  "Returns the IConfigurationElement(s) associated with the input extension id."
  [extension-id]
  (let [registry (Platform/getExtensionRegistry)]
    (.getConfigurationElementsFor ^IExtensionRegistry registry extension-id)))

(defn element-name
  "Return the name of the input IConfigurationElement."
  [^IConfigurationElement element]
  {:pre [(not (nil? element))]}
  (.getName element))

(defn element-attribute
  "Return attributes of the input IConfigurationElement with the given name, or all if no name.
   It does NOT call seq on the result."
  ([^IConfigurationElement element attr-name]
    {:pre [(not (nil? element)) (not (nil? attr-name))]}
    (.getAttribute element attr-name))
  ([^IConfigurationElement element attr-name locale]
    {:pre [(not (nil? element)) (not (nil? attr-name)) (not (nil? locale))]}
    (.getAttribute element attr-name locale)))

(defn element-children
  "Return children of the input IConfigurationElement with the given name, or all if no name.
   It does NOT call seq on the result."
  ([^IConfigurationElement element]
    {:pre [(not (nil? element))]}
    (.getChildren element))
  ([^IConfigurationElement element attr-name]
    {:pre [(not (nil? element)) (not (nil? attr-name))]}
    (.getChildren element attr-name)))

(defn attributes->map
  "Helper for element->descriptor, returns the map of attributes of the element.
   Empty if the element does not contain attributes."
  [^IConfigurationElement element]
  {:pre [(not (nil? element))]}
  (reduce #(assoc %1 (keyword %2) (element-attribute element %2)) {} (.getAttributeNames element)))

(defn element->map
  "Return a descriptor from an IConfigurationElement. It doesn't consider the element's children,
   but it appends the name of the IConfigurationElement as key. The resulting structure follows
   Chapter 8 of The Joy of Clojure."
  [^IConfigurationElement element]
  {:pre [(not (nil? element))]}
  {:tag (.getName element)
   :attrs (attributes->map element)
   :content (map element->map (element-children element))})

(defn extension->map
  "Return a map of the Eclipse extension point identified by the input id. The resulting structure follows
   Chapter 8 of The Joy of Clojure."
  [extension-id]
  (when-let [conf-elements (configuration-elements extension-id)]
    {:tag 'extension
     :attrs { :point extension-id }
     :content (map #(element->map %1) conf-elements)}))

(defn element-valid?
  "Check if the element is valid. Check IConfigurationElement's javadoc."
  [^IConfigurationElement element]
  (and (not (nil? element)) (.isValid element)))

(defn create-executable-ext
  "Create an Executable Extension by invoking IConfigurationElement.createExecutableExtension
  (check the eclipse's javadoc)."
  [^IConfigurationElement element attr-name]
  {:pre [(not (nil? element)) (not (nil? attr-name))]}
  (.createExecutableExtension element attr-name))

(defn add-extension-listener
  "Adds a IRegistryEventListener to the registry for the associated extension-id."
  [^IRegistryEventListener listener extension-id]
  (let [^IExtensionRegistry registry (Platform/getExtensionRegistry)]
    (.addListener registry listener extension-id)))

(defn mock-element
  "Mocks a IConfigurationElement using the parameters tag, attrs, valid and kids in input instead."
  [& args]
  (let [{:keys [tag attrs valid kids]
         :or {tag (str "mock" (rand-int 300)) attrs nil valid false kids nil}} args]
;    (println tag attrs valid kids)
    (reify
      IConfigurationElement
      (getDeclaringExtension [this]
        )
      (getAttributeAsIs [this string]
        )
      (getNamespace [this]
        )
      (getContributor [this]
        )
      (getAttributeNames [this]
        (into-array String (map #(clojure.core/name %1) (keys attrs))))
      (getChildren [this string]
        (into-array IConfigurationElement (filter #(= string (element-name %1)) kids)))
      (getAttribute [this string]
        (get attrs (keyword string)))
      (getAttribute [this string locale]
        (get attrs (keyword string)))
      (getNamespaceIdentifier [this]
        )
      (equals [this object]
        )
      (createExecutableExtension [this string]
        )
      (getParent [this]
        )
      (getValue [this]
        )
      (getValue [this string]
        )
      (isValid [this]
        valid)
      (getChildren [this]
        (into-array IConfigurationElement kids))
      (getValueAsIs [this]
        )
      (getName [this]
        tag))))

(defn- mock-zero-kids-element
  ([]
    (mock-element :tag (str "kid" (rand-int 300))))
  ([attrs]
    (mock-element :tag (str "kid" (rand-int 300)) :attrs attrs))
  ([tag attrs]
      (mock-element :tag tag :attrs attrs)))

(defn- mock-with-kids
  ([tag attrs kid-number]
    (mock-element :tag tag :attrs attrs :kids (repeatedly kid-number mock-zero-kids-element)))
  ([tag attrs kid-number & kid-list-of-attrs]
    {:pre [(= kid-number (count kid-list-of-attrs))]}
    (mock-element :tag tag :attrs attrs :kids (map #(mock-zero-kids-element %1) kid-list-of-attrs))))
