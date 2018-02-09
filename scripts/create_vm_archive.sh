#!/bin/bash

version=`./scripts/version.sh`

# TODO check that we are in the right directory
cd static/downloads
ln -s ../../doc/readme_vm.txt README.txt
zip -v nanodefiner_vm_$version.zip nanodefiner.ova README.txt
rm README.txt
