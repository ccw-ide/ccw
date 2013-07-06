#!/bin/bash

test $1 || ( echo "usage: set-version.sh <new-version>"; exit 1; )

mvn org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=$1
