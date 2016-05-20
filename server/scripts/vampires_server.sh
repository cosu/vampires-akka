#!/usr/bin/env bash

export SERVER_OPTS="${SERVER_OPTS} -Dconfig.file=$1"
shift
if [[ $1 != "local" ]]; then
    IP=`curl -s http://ip.cosu.ro/?ip |  tr -d '[[:space:]]'`
    export HOST=${IP}
    echo "Public host: ${HOST}"
else
    shift
    echo "Public host: localhost"
fi
export BIND_HOST="0.0.0.0"

echo "Vampires-Akka server: Binding to IP:${BIND_HOST}"
echo "Server Opts: ${SERVER_OPTS}"

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
