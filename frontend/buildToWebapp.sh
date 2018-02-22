#!/bin/bash

shellDir=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
calledFrom=$PWD
# path in resources e.g. com/clouway/app/
indexFilesPath=com/clouway/app/adapter/http/get
cd $shellDir/
npm run build
rm -rf ../backend/src/main/webapp/static/
mkdir -p ../backend/src/main/resources/$indexFilesPath
mv  build/{static/,favicon.ico} ../backend/src/main/webapp/
cp -r index/ ../backend/src/main/webapp/static/
mv build/index.html build/homePage.html
mv build/homePage.html ../backend/src/main/resources/$indexFilesPath
mv ../backend/src/main/webapp/static/index/index.html ../backend/src/main/resources/$indexFilesPath
rm -rf build/
cd $calledFrom
