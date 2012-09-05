#!/bin/bash
## $1 is giturl; $2 is app
echo "begin: git clone $1  /data/rundemo/git_temp/$2"
git clone $1  /data/rundemo/git_temp/$2
echo "done: git clone $1  /data/rundemo/git_temp/$2"
cd /data/rundemo/git_temp/$2
echo "begin: mvn dependency:build-classpath -Dmdep.outputFile=classpath -q"
mvn dependency:build-classpath -Dmdep.outputFile=classpath -q
echo "begin: mvn dependency:build-classpath -Dmdep.outputFile=classpath -q"
## mkdir -p /data/rundemo/appprojects/$3/src
## cp -rp /data/rundemo/git_temp/$3/$2/src /data/rundemo/appprojects/$3/src
## cp -p /data/rundemo/git_temp/$3/$2/pom.xml /data/rundemo/appprojects/$3/pom.xml
