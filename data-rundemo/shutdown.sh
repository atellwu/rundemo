#!/bin/bash
# shutdown
PID_FILE=${1}/pid
if [ -f $PID_FILE ] 
 then 
  PID=`cat $PID_FILE 2>/dev/null`
  kill -9 $PID 
  rm -f $PID_FILE
fi
