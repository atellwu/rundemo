#!/bin/bash
javac -d ${1} -cp ${2} ${3} 2>&1
echo "[info]compile ${4} done."
