#!/usr/bin/env bash

IP=`curl http://ip.cosu.ro/?ip`
export BIND_HOST=${IP}
export HOST="0.0.0.0"
export SERVER_OPTS="-Dconfig.file=$1"
shift
echo "Binding to ${BIND_HOST} ${HOST}"

VAMPIRES_SERVER_HOME="${HOME}/vampires-akka/server/"

${VAMPIRES_SERVER_HOME}/bin/server "$@"
