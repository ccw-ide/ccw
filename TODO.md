## Product

### TODO

- Make the pull requests test mechanism work
- Change build/product target from Juno to Kepler. Don't change the default deployed product yet.
en fait, ce sont vraiment 2 opérationss séparées
ca doit etre o(n) avec une bonne constant 😃
peut etre meme legerement dependante de n (rien qu'un peu, allez), qui sait ?
donc 2 opérations séparées :
reindent = on demande à appliquer l'indentation par défaut à une sélection
autoshift = suite à un décalage positif ou négatif de n espaces d'une ligne, on applique aveuglément (attention au strings multilignes) le meme decalage aux lignes suivantes = tout le contenu de la form en début de ligne, et tous les freres qui suivent
j'ai implémenté reindent
je pense effectivement qu'il faut que j'implemente autoshift
Si l'utilisateur appuie juste sur Tab sur une ligne = j'indente juste cette ligne-là, et je fais autoshift sur les autres
Si l'utilisateur fait Ctrl+I sur une sélection (ou tab quand ça marchera) => uniquement indent-lines, pas sûr pour l'autoshift

4 min


quid de l'autoshift à chaque frappe?
Christophe • 4 min
je me pose la question

4 min


ça ferait pleurer les gens d'emacs
Christophe • 4 min
genre t'appuies sur espace et zou tout se réindente magiquement
si ça pourrait pas les perfs ... avec plaisir ! 😃
s/pourrait/pourri
bah deja ça s'active automatiquement que si on est en debut de ligne
et j'appelle aussi autoshift apres les operations de manipulation comme "raise-over" => la form dégage la parente, se décale de X vers la gauche, tout le contenu et les freres se decalent vers la gauche
Allez je le fais ?
Je vais déjà livrer ce que j'ai, ça corrigera le bug, et je fais ça jeudi prochain. On va bien rigoler
- Bring in changes pushed by Gunnar:
  - Gunnar Volkel: Removes namespace switching comments from REPL history 6da6cb73
  - Gunnar Volkel: Adds preferences for REPL history 39dbf2508e
  - Gunnar Volkel: Input modification resets history search => 02d4cb6c5
- Bring the www.ccw-ide.org web site to life (1 landing page, 1 installation page)
- Have a Welcome Page: 
  - for the Project Homepage on the Internet, 
  - for Donations
- Enable in-tycho test compilation of Clojure namespaces to check that at least they AOT-compile correctly

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
