#!/bin/bash
#SBATCH --time=00:30:00
#SBATCH --ntasks-per-node=1

. /etc/bashrc
. /etc/profile.d/modules.sh
module load java/jdk-1.8.0
export KAMON_SIGAR_FOLDER=/tmp

#bind on infiniband
export HOST=$(ip -f inet addr show ib0 |grep inet |awk '{print $2}'|cut -f1 -d/)


srun ${HOME}/vampires-akka-dist/client-1.0-SNAPSHOT/bin/client "$@"
