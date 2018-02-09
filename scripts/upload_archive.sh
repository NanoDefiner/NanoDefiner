#!/bin/bash
version=`./scripts/version.sh`
scp /tmp/nanodefiner_archive/nanodefiner.zip nanodefine:/home/sysadmin/git/NanoDefiner.stable/static/downloads/nanodefiner_upgrade_standalone_$version.zip
