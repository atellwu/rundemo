#!/bin/bash
## $1 is tempAppDir; $2 is subdir; $3 is mvn opt
echo "cd $1"
cd $1
if [ $2 ]; then
  cd $2
fi
echo "mvn dependency:build-classpath -Dmdep.outputFile=classpath $3 -q"
mvn dependency:build-classpath -Dmdep.outputFile=classpath $3 -q
