#!/bin/bash

if [[ "" == $1 ]]; then
    echo "usage: set-version.sh <new-version>"
    exit 1
fi

mvn org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=$1
