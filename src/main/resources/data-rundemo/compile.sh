#!/bin/bash
dest=$1
classpath=$2
app=$3
pageid=$4
javaFileName=$5
javac -d ${dest} -cp ${dest}:${classpath} -sourcepath /data/rundemo/appprojects/${app}/src/main/java/ /data/rundemo/javaprojects/${app}/${pageid}/src/${javaFileName} 2>&1
echo "[info]compile ${javaFileName} done."

