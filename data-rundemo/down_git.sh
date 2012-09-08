#!/bin/bash
## $1 is giturl; $2 is app; $3 is branch
echo "begin: git clone $1  /data/rundemo/git_temp/$2"
git clone $1  /data/rundemo/git_temp/$2
echo "done: git clone $1  /data/rundemo/git_temp/$2"
cd /data/rundemo/git_temp/$2
echo "git checkout $3"
git checkout $3
echo "begin: mvn dependency:build-classpath -Dmdep.outputFile=classpath -q"
mvn dependency:build-classpath -Dmdep.outputFile=classpath -q
echo "done: mvn dependency:build-classpath -Dmdep.outputFile=classpath -q"