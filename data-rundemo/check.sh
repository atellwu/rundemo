#!/bin/bash
# check
# if <pid_file> exsit, and <pid> is runnig, looping.
# if <pid_file> not exsit, and <pid> is runnig (rm <pid_file> if neccesery),return "done".
running()
{
    #[ -f $1 ] || return 1
    ps -p $1 >/dev/null 2>/dev/null || return 1
    return 0
}
sleep 0.5s
PID_FILE=${1}/pid
if [ -f $PID_FILE ]; then
  PID=$(cat $PID_FILE)
  while true; 
    do       
      if running $PID
        then
          sleep 0.5s
      else
        # dead pid file - remove
        rm -f $PID_FILE
        break
      fi
  done
fi
echo "done"
