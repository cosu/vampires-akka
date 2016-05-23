#!/bin/bash - 


set -o nounset                       

./monitorExec.sh $(./all.sh|grep execution | cut -f2 -d\ )

