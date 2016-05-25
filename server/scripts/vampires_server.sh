#!/usr/bin/env bash

set -o errexit
set -o pipefail
set -o nounset

_app_home(){
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
    echo ${APP_HOME}
}

_usage(){
    echo "Usage: $0 <config_file> (local)"
    echo "   <config_file> : the path to a config file"
    echo "   local - optional argument - forces the server to listen on the localhost address"
}

if [[ ! -f $1 ]]; then
    echo "missing config file"
    _usage
    exit 1
fi

export SERVER_OPTS="${SERVER_OPTS:-} -Dconfig.file=$1"
shift

if [[ ${1:-} == "local" ]]; then
    shift
    echo "Setting host to: localhost"
else
    IP=$(curl -s http://ip.cosu.ro/?ip |  tr -d '[[:space:]]')
    export HOST=${IP}
    echo "Setting host to: ${HOST}"
fi

echo "Server Opts: ${SERVER_OPTS}"

APP_HOME=$(_app_home)
${APP_HOME}/bin/server "$@"
