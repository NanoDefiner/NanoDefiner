#!/bin/bash
version=`./scripts/version.sh`
#version=${versionComplete:0:5}
branch=`git branch | grep "*"`
branch=${branch:2}
commit=`git rev-parse HEAD`
commit=${commit:0:7}
buildDate=`date --rfc-3339=seconds`
echo "# Auto-generated file, do not edit" > src/main/resources/build.properties
echo "build.version=$version [$branch@$commit]" >> src/main/resources/build.properties
echo "build.date=$buildDate" >> src/main/resources/build.properties
cp scripts/banner.template src/main/resources/banner.txt
sed -i -e "s/\${application.version}/v$version/" src/main/resources/banner.txt
