#!/bin/bash
# shutdown
# $1 is process name
PID=`pidof $1 2>/dev/null`
if [ ! -z $PID ]; then
  kill -9 $PID
fi