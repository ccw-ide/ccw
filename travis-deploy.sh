#!/bin/bash

sudo apt-get install -qq ftp

FTP_UPDATESITE_ROOT=/www/updatesite/branch
BUILD_DIR="${TRAVIS_BUILD_DIR}/ccw.updatesite/target/repository"
PADDED_TRAVIS_BUILD_NUMBER=`printf "%0*d" 6 ${TRAVIS_BUILD_NUMBER}`
UPDATESITE=${TRAVIS_BRANCH}-travis${PADDED_TRAVIS_BUILD_NUMBER}-git${TRAVIS_COMMIT}

ftp -pn ${FTP_HOST} <<EOF
quote USER ${FTP_USER}
quote PASS ${FTP_PASSWORD}
bin
prompt off
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
