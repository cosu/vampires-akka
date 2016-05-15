#!/bin/bash
#SBATCH --time=00:30:00
#SBATCH --ntasks-per-node=1

. /etc/bashrc
. /etc/profile.d/modules.sh
module load java/jdk-1.8.0


randomdir=$(head /dev/urandom | tr -dc A-Za-z0-9 | head -c5)
export KAMON_SIGAR_FOLDER=/tmp/${randomdir}
#bind on infiniband
export HOST=$(ip -f inet addr show ib0 |grep inet |awk '{print $2}'|cut -f1 -d/)


srun ${HOME}/vampires-akka-dist/client-1.0-SNAPSHOT/bin/client "$@"

rm -fr ${KAMON_SIGAR_FOLDER}
