#!/bin/bash

export HOST=$(curl http://169.254.169.254/latest/meta-data/public-ipv4)
export BIND_HOST=$(curl http://169.254.169.254/latest/meta-data/local-ipv4)
export HOME=/home/ubuntu
export SERVER_IP=$1
shift
export KAMON_SIGAR_FOLDER=/tmp

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

${APP_HOME}/client "$@"
