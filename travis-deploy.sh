#!/bin/bash

FTP_UPDATESITE_ROOT=/www/updatesite/branch
BUILD_DIR="${TRAVIS_BUILD_DIR}/ccw.updatesite/target/repository"
UPDATESITE="${TRAVIS_BRANCH}-travis-${TRAVIS_BUILD_NUMBER}_git-${TRAVIS_COMMIT}"

ftp -Vipn ${FTP_HOST} <<EOF
quote USER ${FTP_USER}
quote PASS ${FTP_PASSWORD}
bin
lcd ${BUILD_DIR}
cd ${FTP_UPDATESITE_ROOT}
mkdir ${TRAVIS_BRANCH}
cd ${TRAVIS_BRANCH}
mkdir ${UPDATESITE}
cd ${UPDATESITE}
lcd features
mkdir features
cd features
mput *
lcd ../plugins
cd ..
mkdir plugins
cd plugins
mput *
lcd ..
cd ..
put artifacts.jar
put content.jar
quit
EOF
test $? && wget http://updatesite.ccw-ide.org/branch/${TRAVIS_BRANCH}/${UPDATESITE}/content.jar
exit $?
