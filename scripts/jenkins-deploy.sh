#!/bin/bash

UPDATESITE=${QUALIFIER}

# FTP dirs are prefixed with FTP. Local dirs have no prefix.
REPOSITORY_DIR="${WORKSPACE}/ccw.product/target/repository"
PRODUCTS_DIR="${WORKSPACE}/ccw.product/target/products"

FTP_BRANCH_DIR=/www/updatesite/branch/${BRANCH}
FTP_UPDATESITE_DIR=${FTP_BRANCH_DIR}/${UPDATESITE}

echo "REPOSITORY_DIR:${REPOSITORY_DIR}"
echo "FTP_UPDATESITE_DIR:${FTP_UPDATESITE_DIR}"

# put p2 repository in the right branch / versioned subdirecty updatesite
# put documentation at the root of the update site so that it is self-documented
# put documentation at the root of the branch site to serve as the up to date generated documentation
lftp ftp://${FTP_USER}:${FTP_PASSWORD}@${FTP_HOST} <<EOF
set ftp:passive-mode true
mirror -R --verbose=3 ${REPOSITORY_DIR}/ ${FTP_UPDATESITE_DIR}
mirror -R --verbose=3 -x target -I *.html ${WORKSPACE}/doc/target/html/ ${FTP_UPDATESITE_DIR}
mirror -R --verbose=3 -x target -I *.html ${WORKSPACE}/doc/target/html/ ${FTP_BRANCH_DIR}/jenkins-doc
quit
EOF

test $? || exit $?

wget http://updatesite.ccw-ide.org/branch/${BRANCH}/${UPDATESITE}/content.jar || exit $?

wget http://updatesite.ccw-ide.org/branch/${BRANCH}/${UPDATESITE}/documentation.html || exit $?

## UPDATE The branch p2 repository by referencing this build's p2 repository
# Create compositeArtifacts.xml 
cat <<EOF > ${WORKSPACE}/compositeArtifacts.xml
<?xml version='1.0' encoding='UTF-8'?>
<?compositeArtifactRepository version='1.0.0'?>
<repository name='&quot;Counterclockwise Jenkins CI Last Build - Branch ${BRANCH}&quot;'
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
cat <<EOF > ${WORKSPACE}/compositeContent.xml
<?xml version='1.0' encoding='UTF-8'?>
<?compositeMetadataRepository version='1.0.0'?>
<repository name='&quot;Counterclockwise Jenkins CI Last Build - Branch ${BRANCH}&quot;'
    type='org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository' version='1.0.0'>
  <properties size='1'>
    <property name='p2.timestamp' value='1243822502499'/>
  </properties>
  <children size='1'>
    <child location='./${UPDATESITE}'/>
  </children>
</repository>
EOF

# don't do this anymore, now travis' the boss again
## Push branch p2 repository files via FTP
#ftp -pn ${FTP_HOST} <<EOF
#quote USER ${FTP_USER}
#quote PASS ${FTP_PASSWORD}
#bin
#prompt off
#lcd ${WORKSPACE}
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
mirror -R --verbose=3 -x ccw -I *.zip ${PRODUCTS_DIR}/ ${FTP_UPDATESITE_DIR}/products
quit
EOF

cd ${PRODUCTS_DIR}
PRODUCTS="`ls Counterclockwise*.zip`"
for PRODUCT in ${PRODUCTS}
do
    # --spider option only checks for file presence, without downloading it
    wget --spider http://updatesite.ccw-ide.org/branch/${BRANCH}/${UPDATESITE}/products/${PRODUCT}  || exit $?
done
