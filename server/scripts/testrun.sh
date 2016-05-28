#!/bin/bash - 


set -o nounset                       

cdir=$(dirname $0)
${cdir}/monitorExec.sh $(${cdir}/all.sh|grep execution | cut -f2 -d\ )

