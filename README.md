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