# Paredit.clj

paredit in clojure, tailored for clojure

# Build tools "coordinates"

##maven/ivy:
        <dependency>
                <groupId>org.lpetit</groupId>
                <artifactId>paredit.clj</artifactId>
                <version>0.12.1.STABLE02</version>
        </dependency>

##lein/cake:
        [org.lpetit/paredit.clj "0.12.1.STABLE02"]

# Dependencies

* Clojure 1.2
* Clojure contrib 1.2
* Laurent's fork of parsley 0.0.6.STABLE02

# Design notes
A central multimethod paredit.core/paredit, whose signature is:

        ([:paredit-command parsetree {:keys [:text :offset :length]})

and which returns: 

        {:keys [:text :offset :length] ({:keys [:text :offset :length]} & more) :modifs}   

parsetree is a datastructure returned by invoking the function paredit.parser/parse on the source code:

        (paredit.parser/parse "(some source code)\n(foo :bar baz")

# Usage example
##Require the namespace   
        user=>(require 'paredit.core) ; automatically requires paredit.parser as well
        nil
##Send the source code to the parser
        user=>(def parse-tree (paredit.parser/parse "(foo bar)"))
        #'user/parse-tree
## "raise sexp" example        
        user=>(paredit.core/paredit :paredit-raise-sexp parse-tree {:text "(foo bar)" :offset 5 :length 0}) ; Let's raise "bar" over "(foo bar)"
        {:modifs ({:offset 0, :length 9, :text "bar"}), :text "bar", :offset 0, :length 0}
Note that the result provides the fully replaced text, while you also have a list of deltas to apply to the original text if this better suits your needs        
## "split expression" example        
        user=>(paredit.core/paredit :paredit-split-sexp parse-tree {:text "(foo bar)" :offset 5 :length 0}) ; Let's raise "bar" over "(foo bar)"
        {:modifs ({:offset 4, :length 1, :text ") ("}), :text "(foo) (bar)", :offset 5, :length 0}      
