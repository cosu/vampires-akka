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


# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`"/$link"
    fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`/.." >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null


srun ${APP_HOME}/bin/client "$@"
