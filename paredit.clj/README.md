# Paredit.clj

paredit in clojure, tailored for clojure

# "coordinates" for build tools 

##maven/ivy:
        <dependency>
                <groupId>org.lpetit</groupId>
                <artifactId>paredit.clj</artifactId>
                <version>0.13.0.STABLE001</version>
        </dependency>

##lein/cake:
        [org.lpetit/paredit.clj "0.13.0.STABLE001"]

# Dependencies

* Clojure 1.5.1
* Clojure contrib 1.2
* Parsley 0.9.2

# Design notes
##A central multimethod paredit.core/paredit, whose signature is:

        ([:paredit-command {:parse-tree parsetree} {:keys [:text :offset :length]})

* :paredit-command is one of the currently accepted commands, see the content of the var paredit.core-commands/*paredit-commands* for the whole list
* parsetree is the result of applying paredit.parser/parse to :text's value
* :text's value is the text corresponding to parsetree
* :offset is the current position of the caret in the text
* :length is the length of the current selection, or 0 if no selection
 
and which returns: 

        {:keys [:text :offset :length] ({:keys [:text :offset :length]} & more) :modifs}   

* :text's value is the text resulting from applying the command
* :offset's value is the new offset of the caret
* :length's value is the new length of the selection
* :modifs's value is a sequence of deltas to apply to the original text in order to obtain the resulting text, and for each element in :modif's value
** :offset's value is the start offset of the text to replace
** :length's value is the length of the text to replace
** :text's value is the text to insert

##parsetree is a datastructure returned by invoking the function paredit.parser/parse on the source code:

        (paredit.parser/parse "(some source code)\n(foo :bar baz")

The result of paredit.parser/parse is a datastructure following clojure.core/xml conventions : :tag is used to name a parsetree node, :content is used
to list children of a parsetree node. parsetree terminals are always java.lang.Strings.

# Usage example
##Require the namespace   
        user=>(require 'paredit.core) ; automatically requires paredit.parser as well
        nil
##Send the source code to the parser
        user=>(def parse-tree (paredit.parser/parse "(foo bar)"))
        #'user/parse-tree
## "raise sexp" example        
        user=>(paredit.core/paredit :paredit-raise-sexp {:parse-tree parse-tree} {:text "(foo bar)" :offset 5 :length 0}) ; Let's raise "bar" over "(foo bar)"
        {:modifs ({:offset 0, :length 9, :text "bar"}), :text "bar", :offset 0, :length 0}
Note that the result provides the fully replaced text, while you also have a list of deltas to apply to the original text if this better suits your needs        
## "split expression" example        
        user=>(paredit.core/paredit :paredit-split-sexp {:parse-tree parse-tree} {:text "(foo bar)" :offset 5 :length 0}) ; Let's raise "bar" over "(foo bar)"
        {:modifs ({:offset 4, :length 1, :text ") ("}), :text "(foo) (bar)", :offset 5, :length 0}      
