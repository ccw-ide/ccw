# How to build the CCW plugin in Eclipse.

This page explains how to install a development environment for hacking on CCW.

## Prerequisites
 
- Java 6
- Eclipse Standard 4.3.1 (aka Kepler) http://eclipse.org/downloads/packages/eclipse-standard-431/keplersr1
- Install maven 3.1.1 from the command line:
  - download from http://mirrors.linsrv.net/apache/maven/maven-3/3.1.1/binaries/apache-maven-3.1.1-bin.zip
  - unzip somewhere, e.g. /path/to/maven, create an environment variable M2_HOME=/path/to/maven, add $M2_HOME/bin to your path

## Command Line (maven based) Environment

### Compile / Test / Package

- cd into `ccw folder`

        cd ccw

- To clean + compile + unit tests + integration tests + build updatesite + build win/lin/osx products:

        mvn clean verify

- To only launch tests

        mvn clean test

Tip: you can add the `-DskipTests` option to avoid launching the tests

## Developing within Eclipse

### Install Eclipse Development Environment

- Clone CCW Repository 
  - https://github.com/laurentpetit/ccw
- In Eclipse, import the repository projects (`ccw/ccw.branding`, `ccw/ccw.core`, `ccw/ccw.core.test`, `ccw/ccw.parent`, `ccw/ccw.product`, `ccw/ccw.target.e37`, `ccw/ccw.target.e42`, `ccw/ccw.updatesite`, `ccw/paredit.clj`)
- cd into `ccw` and invoke `mvn clean install`
  - This will install all CCW dependencies in folder `ccw.core/lib/`
  - The operation must be be repeated after each update of ccw.core dependencies
- Set the Eclipse Target Platform for your tests to Indigo by selecting the provided one in `ccw/ccw.target.e37`:
  - Go to Window > Preferences > Plug-in Development > Target Platform
  - Check the target platform named `ccw.target.e37`, Click Finish

### Run from the Eclipse Development Environment

Select one of the 3 resources in your Eclispe workspace, depending on what you intend to do, and then invoke the "Debug > Debug as" contextual menu on them:

- to debug the plugin: "ccw.core/Counterclockwise Plugin.launch"
- to debug the product: "ccw.core/Counterclockwise Product.launch"
- to launch the Junit (SWTBot) tests: "ccw.core.test/Counterclockwise Product Tests.launch" (some tests don't pass as of july 1st, 2013)

### Install your development version in your Eclipse Environment

- You need to build from the command line. E.G. via `mvn install` from the `ccw` directory.
- The Software Update Site zip is at location `ccw/ccw.updatesite/target/ccw.updatesite-<version>.zip`

## Manage Version Numbers

Use the script `script/set-version.sh` as such:

```
# If you want to set version to 0.20.0-SNAPSHOT
cd ccw
script/set-version.sh 0.20.0-SNAPSHOT
mvn org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=0.20.0-SNAPSHOT
```

This is just a shortcut for the following maven goal:

```
# If you want to set version to 0.20.0-SNAPSHOT
cd ccw
mvn org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=0.20.0-SNAPSHOT
```


This updates POMs, MANIFESTs and feature.xml

## Add clojure.test based tests

- Add the test namespace in `ccw.core.test/src/clj` using the usual namespace structure for folders
- Add the namespace to the list of namespaces to be tested in the java file `ccw.core.test/src/java/ccw/core/ClojureTests.java`


## References

- http://software.2206966.n2.nabble.com/Good-solution-for-non-osgi-jars-td5098103.html : original idea for using maven-dependency-plugin to copy deps into lib/
- https://github.com/reficio/p2-maven-plugin : easy to grok tutorial for beginning with maven tycho
- https://maven.apache.org/plugins/maven-dependency-plugin/copy-mojo.html : maven dependency:copy reference
- https://maven.apache.org/plugins/maven-dependency-plugin/unpack-dependencies-mojo.html : maven dependency:unpack-dependencies reference
