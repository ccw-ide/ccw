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

- Addition of 2 important paredit/structural manipulation features: slurp and barf, backward and forward. (Fixes Issue #486: Add support for barfage and slurpage)

See the Keybindings documentation for Slurp / Barf : https://code.google.com/p/counterclockwise/wiki/EditorKeyBindingsFeatures

- Fixed wrong keybinding for switching Rainbow Parens: "Cmd+( R" instead of "Cmd+(+R" (replace Cmd by Ctrl for Windows/Linux ; space indicates that you first type Cmd+(, then type R)

- Experimental Feature: enabling auto-reevaluation of last command typed in the REPL, after a successful evaluation from the Editor
  - when the "repeat last expression" REPL command is active
  - when the user sends a form tom evaluate to the REPL
  - if the form evaluated without error
  - then the last expression evaluated from the REPL input is evaluated agai

A common usecase for this is while you're defining a function in the Editor and you want to repeat the same test expression again and again until it passes. The workflow then becomes:
- define the function in the editor, eval via Cmd+Enter
- switch to the REPL, enter a test expression, send the test expression via Cmd+Enter
- Click on the "Repeat last evaluation ..." button in the REPL
- switch to the editor, modify the function, eval via Cmd+Enter ... when the eval succeeds, the last expression entered in the REPl is reevaluated ... repeat ...


### Repl 

- A bug had slipped in the project classpath management preventing native libraries to load properly, for instance when trying to work with Overtone. Fix Issue #577 

- Reverting the behaviour of the "Evaluate selection" when sending to the REPL : back to using 'in-ns instead of 'ns while transitioning to the selection's namespace
Fixes Issue #533: ns is sent to REPL instead of in-ns

- Launch repl ack timeout set to 60 sec instead of 30

- Better IPv6 support: replaced "localhost" occurences by "127.0.0.1"

- "Quiet command report mode" Preference has been removed. Now considered always ON by default. So always quiet.

### Stability

- There were reports of restart issues after Eclipse crashes. Hopefully this has been improved in this release (Fix Issue #568	CCW Beta 0.13.0.201304190010 prevents Eclipse from loading)

### Leiningen

- Internally switched to Leiningen 2.1.2

- Better exception reporting in the Problem Markers, e.g. when a dependency cannot be found, etc.

### Better plays as an Eclipse plugin

- CCW plugin does not start unnecessarily when invoking the project's contextual menu

