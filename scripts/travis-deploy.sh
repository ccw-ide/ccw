#!/bin/bash

sudo apt-get install -qq ftp

FTP_UPDATESITE_ROOT=/www/updatesite/branch
REPOSITORY_DIR="${TRAVIS_BUILD_DIR}/ccw.product/target/repository"
UPDATESITE=travis${QUALIFIER}

PRODUCTS_DIR="${TRAVIS_BUILD_DIR}/ccw.product/target/products"


## Push the p2 repository for the build $UPDATESITE
## and also the documentation files
ftp -pn ${FTP_HOST} <<EOF
quote USER ${FTP_USER}
quote PASS ${FTP_PASSWORD}
bin
prompt off
lcd ${REPOSITORY_DIR}
cd ${FTP_UPDATESITE_ROOT}
mkdir ${BRANCH}
cd ${BRANCH}
mkdir ${UPDATESITE}
cd ${UPDATESITE}
lcd features
mkdir features
cd features
mput *
lcd ../binary
cd ..
mkdir binary
cd binary
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
mput * 
cd ${FTP_UPDATESITE_ROOT}/${BRANCH}
mkdir travis-doc
cd travis-doc
mput *
quit
EOF

test $? || ( echo "FTP Push for build ${UPDATESITE} failed with error code $?" ; exit $? )

wget http://updatesite.ccw-ide.org/branch/${BRANCH}/${UPDATESITE}/content.jar || ( echo "Test that FTP Push for build ${UPDATESITE} worked failed: was unable to fetch http://updatesite.ccw-ide.org/branch/${BRANCH}/${UPDATESITE}/content.jar" ; exit 1 )

wget http://updatesite.ccw-ide.org/branch/${BRANCH}/${UPDATESITE}/documentation.html || ( echo "Test that FTP Push for build ${UPDATESITE} worked failed: was unable to fetch http://updatesite.ccw-ide.org/branch/${BRANCH}/${UPDATESITE}/documentation.html" ; exit 1 )

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

test $? || ( echo "Problem while creating file compositeArtifacts.xml for build ${UPDATESITE}" ; exit $? )

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

test $? || ( echo "Problem while creating file compositeContent.xml for build ${UPDATESITE}" ; exit $? )

# Push branch p2 repository files via FTP
ftp -pn ${FTP_HOST} <<EOF
quote USER ${FTP_USER}
quote PASS ${FTP_PASSWORD}
bin
prompt off
lcd ${TRAVIS_BUILD_DIR}
cd ${FTP_UPDATESITE_ROOT}/${BRANCH}
put compositeArtifacts.xml
put compositeContent.xml
quit
EOF
test $? || ( echo "Problem while updating branch ${BRANCH} repository artifacts for build ${UPDATESITE}" ; exit $? )


[ -d ${PRODUCTS_DIR} ] || ( echo "Skipping ftp publication of CCW products for missing directory ${PRODUCTS_DIR}"; exit 1; )

# Create directory products in ftp
ftp -pn ${FTP_HOST} <<EOF
quote USER ${FTP_USER}
quote PASS ${FTP_PASSWORD}
bin
prompt off
lcd ${PRODUCTS_DIR}
cd ${FTP_UPDATESITE_ROOT}/${BRANCH}/${UPDATESITE}
mkdir products 
quit
EOF

# iterate over the products to push in parallel
cd ${PRODUCTS_DIR}
PRODUCTS="`ls Counterclockwise*.zip`"
for PRODUCT in ${PRODUCTS}
do
# Push CCW products files via FTP
ftp -pn ${FTP_HOST} <<EOF &
quote USER ${FTP_USER}
quote PASS ${FTP_PASSWORD}
bin
prompt off
lcd ${PRODUCTS_DIR}
cd ${FTP_UPDATESITE_ROOT}/${BRANCH}/${UPDATESITE}/products
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

