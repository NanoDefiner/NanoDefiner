#!/bin/bash

# TODO check that we are in the right directory
mvn package
cd target
mkdir archive
cd archive
ln -s ../NanoDefiner*.war NanoDefiner.war
ln -s ../../doc/readme_standalone.txt README.txt
ln -s ../../doc/readme_upgrade.txt UPGRADE.txt
ln -s ../../config
ln -s ../../scripts/start_server.sh
ln -s ../../scripts/stop_server.sh
mkdir /tmp/nanodefiner_archive
zip -v /tmp/nanodefiner_archive/nanodefiner.zip NanoDefiner.war README.txt UPGRADE.txt config/img/* config/knowledge/hashes.properties config/knowledge/release/* config/*default config/locales/* *_server.sh
cd ..
rm -r archive
