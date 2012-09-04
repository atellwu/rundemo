#!/bin/bash
# shutdown
PID_FILE=${4}/pid
if [ -f $PID_FILE ]  
 then
  PID=`cat $PID_FILE 2>/dev/null`
  kill -9 $PID 
  rm -f $PID_FILE
fi
# start
java -cp ${1}:${2} ${3} 2>&1 &
echo $! > $PID_FILE

