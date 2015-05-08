#!/bin/bash
# start
ln -s ${JAVA_HOME}/bin/java ${5}/${4}
PATH="${5}:$PATH"
${4} -cp ${1}:${2} ${3} 2>&1