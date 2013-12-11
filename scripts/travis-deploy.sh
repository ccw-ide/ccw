#!/bin/bash

sudo apt-get install -qq ftp

FTP_UPDATESITE_ROOT=/www/updatesite/branch
BUILD_DIR="${TRAVIS_BUILD_DIR}/ccw.updatesite/target/repository"
UPDATESITE=${QUALIFIER}

PRODUCTS_DIR="${TRAVIS_BUILD_DIR}/ccw.product/target/products"


## Push the p2 repository for the build <travisbuild>-<branch>-<gitSha1>
## and also the documentation files
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
lcd ${TRAVIS_BUILD_DIR}/doc/target/html
mkdir doc
cd doc
mput * 
cd ${FTP_UPDATESITE_ROOT}/${TRAVIS_BRANCH}
mkdir doc
cd doc
mput *
quit
EOF

test $? || ( echo "FTP Push for build ${UPDATESITE} failed with error code $?" ; exit $? )

wget http://updatesite.ccw-ide.org/branch/${TRAVIS_BRANCH}/${UPDATESITE}/content.jar || ( echo "Test that FTP Push for build ${UPDATESITE} worked failed: was unable to fetch http://updatesite.ccw-ide.org/branch/${TRAVIS_BRANCH}/${UPDATESITE}/content.jar" ; exit 1 )

wget http://updatesite.ccw-ide.org/branch/${TRAVIS_BRANCH}/${UPDATESITE}/doc/documentation.html || ( echo "Test that FTP Push for build ${UPDATESITE} worked failed: was unable to fetch http://updatesite.ccw-ide.org/branch/${TRAVIS_BRANCH}/${UPDATESITE}/doc/documentation.html" ; exit 1 )

## UPDATE The branch p2 repository by referencing this build's p2 repository

# Create compositeArtifacts.xml 
cat <<EOF > ${TRAVIS_BUILD_DIR}/compositeArtifacts.xml
<?xml version='1.0' encoding='UTF-8'?>
<?compositeArtifactRepository version='1.0.0'?>
<repository name='&quot;Counterclockwise Travis CI Last Build - Branch ${TRAVIS_BRANCH}&quot;'
    type='org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository' version='1.0.0'>
  <properties size='1'>
    <property name='p2.timestamp' value='1243822502440'/>
  </properties>
  <children size='1'>
    <child location='./${UPDATESITE}'/>
  </children>
</repository>
EOF

test $? || ( echo "Problem while creating file compositeArtifacts.xml for build ${UPDATESITE}" ; exit $? )

# Create compositeContent.xml
cat <<EOF > ${TRAVIS_BUILD_DIR}/compositeContent.xml
<?xml version='1.0' encoding='UTF-8'?>
<?compositeMetadataRepository version='1.0.0'?>
<repository name='&quot;Counterclockwise Travis CI Last Build - Branch ${TRAVIS_BRANCH}&quot;'
    type='org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository' version='1.0.0'>
  <properties size='1'>
    <property name='p2.timestamp' value='1243822502499'/>
  </properties>
  <children size='1'>
    <child location='./${UPDATESITE}'/>
  </children>
</repository>
EOF

test $? || ( echo "Problem while creating file compositeContent.xml for build ${UPDATESITE}" ; exit $? )

# Push branch p2 repository files via FTP
ftp -pn ${FTP_HOST} <<EOF
quote USER ${FTP_USER}
quote PASS ${FTP_PASSWORD}
bin
prompt off
lcd ${TRAVIS_BUILD_DIR}
cd ${FTP_UPDATESITE_ROOT}/${TRAVIS_BRANCH}
put compositeArtifacts.xml
put compositeContent.xml
quit
EOF
test $? || ( echo "Problem while updating branch ${TRAVIS-BRANCH} repository artifacts for build ${UPDATESITE}" ; exit $? )


[ -d ${PRODUCTS_DIR} ] || ( echo "Skipping ftp publication of CCW products for missing directory ${PRODUCTS_DIR}"; exit 1; )

# Create directory products in ftp
ftp -pn ${FTP_HOST} <<EOF
quote USER ${FTP_USER}
quote PASS ${FTP_PASSWORD}
bin
prompt off
lcd ${PRODUCTS_DIR}
cd ${FTP_UPDATESITE_ROOT}/${TRAVIS_BRANCH}/${UPDATESITE}
mkdir products 
quit
EOF

# iterate over the products to push in parallel
PRODUCTS="ccw-linux.gtk.x86.zip ccw-linux.gtk.x86_64.zip ccw-macosx.cocoa.x86_64.zip ccw-win32.win32.x86.zip ccw-win32.win32.x86_64.zip"
for PRODUCT in ${PRODUCTS}
do
# Push CCW products files via FTP
ftp -pn ${FTP_HOST} <<EOF &
quote USER ${FTP_USER}
quote PASS ${FTP_PASSWORD}
bin
prompt off
lcd ${PRODUCTS_DIR}
cd ${FTP_UPDATESITE_ROOT}/${TRAVIS_BRANCH}/${UPDATESITE}/products
put ${PRODUCT}
quit
EOF
done

wait

for PRODUCT in ${PRODUCTS}
do
# --spider option only checks for file presence, without downloading it
wget --spider http://updatesite.ccw-ide.org/branch/${UPDATESITE}/products/${PRODUCT}  || ( echo "Problem while pushing CCW product ${PRODUCT} via FTP" ; exit $? )
echo "Pushed product ${PRODUCT}"
done

