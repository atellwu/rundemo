#!/bin/bash
## $1 is giturl; $2 is app; $3 is branch; $4 is mvn opt,$5 is subdir
echo "git clone $1  /data/rundemo/git_temp/$2"
git clone $1  /data/rundemo/git_temp/$2
echo "cd /data/rundemo/git_temp/$2"
cd /data/rundemo/git_temp/$2
if [ $5 ]; then
  cd $5
fi
echo "git checkout $3"
git checkout $3
echo "mvn dependency:build-classpath -Dmdep.outputFile=classpath $4 -q"
mvn dependency:build-classpath -Dmdep.outputFile=classpath $4 -q