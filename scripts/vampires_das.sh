#!/bin/bash
#SBATCH --time=00:15:00
#SBATCH --ntasks-per-node=1

. /etc/bashrc
. /etc/profile.d/modules.sh
module load java/jdk-1.8.0

#bind on infiniband
export HOST=$(ip -f inet addr show ib0 |grep inet |awk '{print $2}'|cut -f1 -d/)

#server ip is given as first argument
export SERVER_IP=$1
shift

srun ${HOME}/vampires-akka/client/build/install/client/bin/client "$@"
