## Product

### TODO

- Update version to 0.14.0-SNAPSHOT, using maven tycho helpers, and explain how to do so in the build page
- Change build/product target from Juno to Kepler. Don't change the default deployed product yet.
- Bring the www.ccw-ide.org web site to life (1 landing page, 1 installation page)
- Have a Welcome Page: 
  - for the Project Homepage on the Internet, 
  - for Donations

### DONE

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
