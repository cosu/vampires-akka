#!/usr/bin/env bash

IP=`curl -s http://ip.cosu.ro/?ip |  tr -d '[[:space:]]'`
export BIND_HOST="0.0.0.0"
export HOST=${IP}
export SERVER_OPTS="-Dconfig.file=$1"
shift
echo "Vampires-Akka server: Binding to IP:${BIND_HOST} . Public host: ${HOST}"

VAMPIRES_SERVER_HOME="${HOME}/vampires-akka/server/"

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
