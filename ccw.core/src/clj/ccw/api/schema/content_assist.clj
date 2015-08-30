(ns ccw.api.schema.content-assist
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
  {:replacement {:text String, :offset s/Int, :length s/Int}
   :cursor-position s/Int
   (s/optional-key :image) org.eclipse.swt.graphics.Image
   :display-string String
   ;;:context-information ContextInformationMap
   :additional-proposal-info (cs/reference String) ; or with monitor ...
   :display-string-style [s/Int]
   (s/optional-key :auto-insertable?) s/Bool})

(s/defschema CompletionProposalProviderFn
  (s/=>* (s/maybe [CompletionProposalMap]) [[IClojureEditor s/Any s/Int]]))

(s/defschema CompletionProposalProvider
  {:label String
   :provider (s/either
               CompletionProposalProviderFn
               (cs/reference CompletionProposalProviderFn))})

(s/defschema ContextInformationProviderFn
  "Signature of function that can produce ContextInformation map"
  (s/=>* (s/maybe ContextInformationMap) [[IClojureEditor s/Any s/Int]]))

(s/defschema ContextInformationProvider
  {:label    String
   :provider (s/either
               ContextInformationProviderFn
               (cs/reference ContextInformationProviderFn))})
