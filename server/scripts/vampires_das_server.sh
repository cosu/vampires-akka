#!/usr/bin/env bash

IP=$(ip -f inet addr show ib0 |grep inet |awk '{print $2}'|cut -f1 -d/)

export SERVER_OPTS="-Dconfig.file=$1"
export BIND_HOST="0.0.0.0"
export HOST=${IP}
shift
echo "Vampires-Akka server: Binding to IP:${BIND_HOST} . Public host: ${HOST}"

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

${APP_HOME}/bin/server "$@"

