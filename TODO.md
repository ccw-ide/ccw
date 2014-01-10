## Product

### TODO

DONE- Automatically open nrepl links that appear in Console Views (and a switch to preven this behaviour)
DONE- Handle source path exclusions so that it's possible to do weird things in project.clj (nesting source paths, etc.)
DONE - Create Alt+L R for starting a new lein repl :headless command
- Have the new clojure project remember the location status from opening to opening
- Have hyperlink & doc work even if there's no project attached to the active REPL (try to resolve in the project of the current file)
- Have Stacktrace links in stacktraces work (again) for clojure links
- Generic Lein Launcher: helper for discovering and typing project tasks (would be the start of the disconnected infrastructure)
- New Clojure Project Wizard: helper for lein templates
- Static analysis based on heuristics for clojure projects
- Have the REPL View become visually inactive when the connection is closed
- History of commands in the Generic Lein Launcher
- Investigate the Debug Area
- Have Java completion, etc. work again

- Autoshift:
DONE  - extract logic from Eclipse, add tests
DELAYED  - make it work with raise over, etc.
DELAYED  - make it work with cut/copy/paste
- document
- release

- Make the pull requests test mechanism work
DONE - Change build/product target from Juno to Kepler. Don't change the default deployed product yet.
DELAYED - Bring in changes pushed by Gunnar:
DELAYED   - Gunnar Volkel: Removes namespace switching comments from REPL history 6da6cb73
DELAYED   - Gunnar Volkel: Adds preferences for REPL history 39dbf2508e
DELAYED   - Gunnar Volkel: Input modification resets history search => 02d4cb6c5
DELAYED - Bring the www.ccw-ide.org web site to life (1 landing page, 1 installation page)
DELAYED - Have a Welcome Page: 
DELAYED  - for the Project Homepage on the Internet, 
DELAYED  - for Donations
DELAYED - Enable in-tycho test compilation of Clojure namespaces to check that at least they AOT-compile correctly

### DONE

- Document how to add Clojure Tests
- Document how to bump version numbers
- Update the How to Build Wiki page
- Update the Update Sites Wiki page
- do the updatesite switch: make cgrand legacy, make ccw-ide official
- Product now contains Software Update Sites for CCW, Indigo
- Incorporate by default:
  - XML, JS, CSS support
  - Eclipse ColorTheme
- Have the qualifier match the <branch>-travis<build>-git<sha1> convention 
- Incorporate all images in all sizes for the different platforms sent by Tom Hickey (/Users/laurentpetit/Counterclockwise/Counterclockwise-glyph.zip)
- Install the EGit, m2e features etc. as is done for CCW so that they can be updated via Software Update Site
- Brand the product: better About page
- Fix Bug with Create new project link
- Fix Bug reporting wrong files when an error occurs (e.g. http://updatesite.ccw-ide.org/branch/master/ERROR-master-travis000048-juno-openjdk7-git0a7469ca03d907dd2fc3b8848fbec3d007088683/ )
- Add Linux 32 bits to the created products
- Fix Bug in Travis preventing Juno to work
- Definitively move from "openbar" p2 repository to fine tuned target platforms
- Splash screen with progress bar
  - EGit support
  - Maven Support
- Have a Welcome Page: 
  - Quick start for creating a project, 
  - for the Project Documentation on the Internet, 
  - for Clojure documentation on the Internet
 - Open in the Java Perspective instead of the Resources Perspective
