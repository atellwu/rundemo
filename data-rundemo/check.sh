#!/bin/bash
# check
# return true: if <pid_file> exsit, and <pid> is runnig
# return false: if <pid_file> not exsit, and <pid> is runnig (rm <pid_file> if neccesery)
running()
{
    [ -f $1 ] || return 1
    PID=$(cat $1)
    ps -p $PID >/dev/null 2>/dev/null || return 1
    return 0
}
PID_FILE=${1}/pid
if [ -f $PID_FILE ]
  then            
    if running $PID_FILE
      then
         echo "true"
    else
       # dead pid file - remove
       rm -f $PID_FILE
       echo "false"
    fi
else
  echo "false"
fi
