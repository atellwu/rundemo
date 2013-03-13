#!/bin/bash
# start
ln -s /home/wukezhu/software/jdk1.6.0_32/bin/java ${5}/${4}
PATH="${5}:$PATH"
${4} -cp ${1}:${2} ${3} 2>&1