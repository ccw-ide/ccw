#!/bin/bash

UPDATESITE=travis${QUALIFIER}

# FTP dirs are prefixed with FTP. Local dirs have no prefix.
TESTS_DIR="${TRAVIS_BUILD_DIR}/ccw.core.test/target/surefire-reports"
PAREDIT_TESTS_DIR="${TRAVIS_BUILD_DIR}/paredit.clj/target/test-reports"
SCREENSHOTS_DIR="${TRAVIS_BUILD_DIR}/ccw.core.test/screenshots"
ERROR=ERROR-${ECLIPSE_TARGET}-${TRAVIS_JDK_VERSION}

FTP_BRANCH_DIR=/www/updatesite/branch/${BRANCH}
FTP_UPDATESITE_DIR=${FTP_BRANCH_DIR}/${UPDATESITE}
FTP_ERROR_DIR=${FTP_UPDATESITE_DIR}/${ERROR}

# Report paredit.clj unit tests
[ -d ${PAREDIT_TESTS_DIR} ] || echo "Skipping ftp reporting for missing directory ${PAREDIT_TESTS_DIR}"
[ -d ${PAREDIT_TESTS_DIR} ] && lftp ftp://${FTP_USER}:${FTP_PASSWORD}@${FTP_HOST} <<EOF
set ftp:passive-mode true
mirror -R --verbose=3 ${PAREDIT_TESTS_DIR}/ ${FTP_ERROR_DIR}/paredit
quit
EOF

# Report ccw.core.tests integration tests
[ -d ${TESTS_DIR} ] || echo "Skipping ftp reporting for missing directory ${TESTS_DIR}"
[ -d ${TESTS_DIR} ] && lftp ftp://${FTP_USER}:${FTP_PASSWORD}@${FTP_HOST} <<EOF
set ftp:passive-mode true
mirror -R --verbose=3 ${TESTS_DIR}/ ${FTP_ERROR_DIR}
quit
EOF

# Report ccw.core.tests integration tests screenshots
[ -d ${SCREENSHOTS_DIR} ] || echo "Skipping ftp reporting for missing directory ${SCREENSHOTS_DIR}"
[ -d ${SCREENSHOTS_DIR} ] && lftp ftp://${FTP_USER}:${FTP_PASSWORD}@${FTP_HOST} <<EOF
set ftp:passive-mode true
mirror -R --verbose=3 ${SCREENSHOTS_DIR}/ ${FTP_ERROR_DIR}
quit
EOF
