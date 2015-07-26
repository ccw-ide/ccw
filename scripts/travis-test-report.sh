#!/bin/bash

sudo apt-get install -qq ftp

FTP_UPDATESITE_ROOT=/www/updatesite/branch
TESTS_DIR="${TRAVIS_BUILD_DIR}/ccw.core.test/target/surefire-reports"
PAREDIT_TESTS_DIR="${TRAVIS_BUILD_DIR}/paredit.clj/target/test-reports"
SCREENSHOTS_DIR="${TRAVIS_BUILD_DIR}/ccw.core.test/screenshots"
UPDATESITE=travis${QUALIFIER}
ERROR=ERROR-${ECLIPSE_TARGET}-${TRAVIS_JDK_VERSION}


# Create infrastructure
ftp -pn ${FTP_HOST} <<EOF
quote USER ${FTP_USER}
quote PASS ${FTP_PASSWORD}
bin
prompt off
cd ${FTP_UPDATESITE_ROOT}
mkdir ${BRANCH}
cd ${BRANCH}
mkdir ${UPDATESITE}
cd ${UPDATESITE}
mkdir ${ERROR}
quit
EOF


# Report paredit.clj unit tests
[ -d ${PAREDIT_TESTS_DIR} ] || echo "Skipping ftp reporting for missing directory ${PAREDIT_TESTS_DIR}"
[ -d ${PAREDIT_TESTS_DIR} ] && ftp -pn ${FTP_HOST} <<EOF
quote USER ${FTP_USER}
quote PASS ${FTP_PASSWORD}
bin
prompt off
cd ${FTP_UPDATESITE_ROOT}/${BRANCH}/${UPDATESITE}/${ERROR}
mkdir paredit
cd paredit
lcd ${PAREDIT_TESTS_DIR}
mput *
quit
EOF


# Report ccw.core.tests integration tests
[ -d ${TESTS_DIR} ] || echo "Skipping ftp reporting for missing directory ${TESTS_DIR}"
[ -d ${TESTS_DIR} ] && ftp -pn ${FTP_HOST} <<EOF
quote USER ${FTP_USER}
quote PASS ${FTP_PASSWORD}
bin
prompt off
cd ${FTP_UPDATESITE_ROOT}/${BRANCH}/${UPDATESITE}/${ERROR}
lcd ${TESTS_DIR}
mput *
quit
EOF

# Report ccw.core.tests integration tests screenshots
[ -d ${SCREENSHOTS_DIR} ] || echo "Skipping ftp reporting for missing directory ${SCREENSHOTS_DIR}"
[ -d ${SCREENSHOTS_DIR} ] && ftp -pn ${FTP_HOST} <<EOF
quote USER ${FTP_USER}
quote PASS ${FTP_PASSWORD}
bin
prompt off
cd ${FTP_UPDATESITE_ROOT}/${BRANCH}/${UPDATESITE}/${ERROR}
lcd ${SCREENSHOTS_DIR}
mput *
quit
EOF
