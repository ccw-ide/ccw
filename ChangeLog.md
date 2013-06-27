## Changes between Counterclockwise 0.12.3 and ...

### Build Process totally rewritten

For people wanting to build Counterclockwise from scratch, or to work with Counterclockwise.

The Build Process now uses [Maven](http://maven.apache.org) + [Tycho](http://www.eclipse.org/tycho).

- It is now fully automated, from fetching Eclipse or non Eclipse dependencies, to building an update site for the codebase, to building Standalone Counterclockwise products for the codebase.

In a nutshell:

``` 
git clone https://github.com/laurentpetit/ccw
cd ccw
mvn verify
cd ccw.product/target/products # the products for Windows / Linux / OS X
cd ../../../ccw.updatesite/target/repository # the Software Update Site 
```

For more information on installing a full-fledged dev environment, see the Wiki Page [How To Build](https://code.google.com/p/counterclockwise/wiki/HowToBuild)

### New Software Update Site

The software update site has been updated to its new location:
- Stable Versions: http://updatesite.ccw-ide.org/stable
- Beta Versions: http://updatesite.ccw-ide.org/beta

For more information on the available Software Update Sites and their retention policies, and more, see the Wiki Page [Update Sites](https://code.google.com/p/counterclockwise/wiki/UpdateSites)

### Editor

- More intuitive Ctrl+Enter: hitting Ctrl+Enter when the cursor is located at the top level selects the preceding top level form to be sent to the REPL. Only when the cursor is right in front of a top level form will it be selected instead of the previous one. (Fix Issue #580)

e.g. if the caret is materialized with the symbol |:

``` clojure
(defn foo [] ...)|
(defn bar [] ....)
;; => foo's declaration will be sent to the REPL

(defn baz [] ...)
|(defn qix [] ...)
;; => qix's declaration will be sent to the REPL
```

### Repl 

- A bug had slipped in the project classpath management preventing native libraries to load properly, for instance when trying to work with Overtone. Fix Issue #577 
