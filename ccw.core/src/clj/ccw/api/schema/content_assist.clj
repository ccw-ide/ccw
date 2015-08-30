(ns ccw.api.schema.content-assist
  "Schemas serving as data API for content assist features.
   See ccw.api.content-assist"
  (:require
    [schema.core :as s]
    [ccw.schema.core :as cs])
  (:import ccw.editors.clojure.IClojureEditor))

(s/defschema ContextInformationMap
  {(s/optional-key :information-display) String
   (s/optional-key :display-string)       String
   (s/optional-key :image)               org.eclipse.swt.graphics.Image
   (s/optional-key :context-information-position) s/Int
   (s/optional-key :context-information-position-start) s/Int
   (s/optional-key :context-information-position-stop) s/Int})

(s/defschema CompletionProposalMap
  {:replacement (s/either String {:text String, :offset s/Int, :length s/Int})
   (s/optional-key :cursor-position) s/Int
   (s/optional-key :image) org.eclipse.swt.graphics.Image
   (s/optional-key :display-string) String
   (s/optional-key :context-information-delay) (cs/deref (s/maybe ContextInformationMap))
   (s/optional-key :additional-proposal-info-delay) (cs/deref (s/maybe String)) ; or with monitor ...
   (s/optional-key :display-string-style) [s/Int]
   (s/optional-key :auto-insertable?) s/Bool})

(s/defschema CompletionProposalProviderFn
  (s/=>* (s/maybe [CompletionProposalMap]) [[IClojureEditor s/Any s/Int]]))

(s/defschema CompletionProposalProvider
  {:label String
   :provider (s/either
               CompletionProposalProviderFn
               (cs/deref CompletionProposalProviderFn))})

(s/defschema ContextInformationProviderFn
  "Signature of function that can produce ContextInformation map"
  (s/=>* (s/maybe ContextInformationMap) [[IClojureEditor s/Any s/Int]]))

(s/defschema ContextInformationProvider
  {:label    String
   :provider (s/either
               ContextInformationProviderFn
               (cs/deref ContextInformationProviderFn))})
