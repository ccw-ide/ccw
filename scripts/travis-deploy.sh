#!/bin/bash

UPDATESITE=travis${QUALIFIER}

# FTP dirs are prefixed with FTP. Local dirs have no prefix.
REPOSITORY_DIR="${TRAVIS_BUILD_DIR}/ccw.product/target/repository"
PRODUCTS_DIR="${TRAVIS_BUILD_DIR}/ccw.product/target/products"

FTP_BRANCH_DIR=/www/updatesite/branch/${BRANCH}
FTP_UPDATESITE_DIR=${FTP_BRANCH_DIR}/${UPDATESITE}

# put p2 repository in the right branch / versioned subdirecty updatesite
# put documentation at the root of the update site so that it is self-documented
# put documentation at the root of the branch site to serve as the up to date generated documentation
lftp ftp://${FTP_USER}:${FTP_PASSWORD}@${FTP_HOST} <<EOF
set ftp:passive-mode true
mirror -R --verbose=3 -x bin ${REPOSITORY_DIR}/ ${FTP_UPDATESITE_DIR}
mirror -R --verbose=3 -I *.html ${TRAVIS_BUILD_DIR}/doc/target/html/ ${FTP_UPDATESITE_DIR}
mirror -R --verbose=3 -I *.html ${TRAVIS_BUILD_DIR}/doc/target/html/ ${FTP_BRANCH_DIR}/travis-doc
quit
EOF

test $? || exit $?

wget http://updatesite.ccw-ide.org/branch/${BRANCH}/${UPDATESITE}/content.jar || exit 1

wget http://updatesite.ccw-ide.org/branch/${BRANCH}/${UPDATESITE}/documentation.html || exit 1

## UPDATE The branch p2 repository by referencing this build's p2 repository
# Create compositeArtifacts.xml 
cat <<EOF > ${TRAVIS_BUILD_DIR}/compositeArtifacts.xml
<?xml version='1.0' encoding='UTF-8'?>
<?compositeArtifactRepository version='1.0.0'?>
<repository name='&quot;Counterclockwise Travis CI Last Build - Branch ${BRANCH}&quot;'
    type='org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository' version='1.0.0'>
  <properties size='1'>
    <property name='p2.timestamp' value='1243822502440'/>
  </properties>
  <children size='1'>
    <child location='./${UPDATESITE}'/>
  </children>
</repository>
EOF

# Create compositeContent.xml
cat <<EOF > ${TRAVIS_BUILD_DIR}/compositeContent.xml
<?xml version='1.0' encoding='UTF-8'?>
<?compositeMetadataRepository version='1.0.0'?>
<repository name='&quot;Counterclockwise Travis CI Last Build - Branch ${BRANCH}&quot;'
    type='org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository' version='1.0.0'>
  <properties size='1'>
    <property name='p2.timestamp' value='1243822502499'/>
  </properties>
  <children size='1'>
    <child location='./${UPDATESITE}'/>
  </children>
</repository>
EOF

# DEACTIVATED : Jenkins' the boss atm
## Push branch p2 repository files via FTP
#ftp -pn ${FTP_HOST} <<EOF
#quote USER ${FTP_USER}
#quote PASS ${FTP_PASSWORD}
#bin
#prompt off
#lcd ${TRAVIS_BUILD_DIR}
#cd ${FTP_UPDATESITE_ROOT}/${BRANCH}
#put compositeArtifacts.xml
#put compositeContent.xml
#quit
#EOF
#test $? || exit $?


[ -d ${PRODUCTS_DIR} ] || exit $?

# iterate over the products to push in parallel

lftp ftp://${FTP_USER}:${FTP_PASSWORD}@${FTP_HOST} <<EOF
set ftp:passive-mode true
mirror -R -I *.zip ccw --verbose=3 ${PRODUCTS_DIR}/ ${FTP_UPDATESITE_DIR}/products
quit
EOF

cd ${PRODUCTS_DIR}
PRODUCTS="`ls Counterclockwise*.zip`"
for PRODUCT in ${PRODUCTS}
do
    # --spider option only checks for file presence, without downloading it
    wget --spider http://updatesite.ccw-ide.org/branch/${UPDATESITE}/products/${PRODUCT}  || exit $?
done
