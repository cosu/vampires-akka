#!/usr/bin/env bash

export BIND_HOST=`curl http://ip.cosu.ro/?ip`
export SERVER_OPTS="-Dconfig.file=$1"
shift
echo "Binding to ${BIND_HOST}"

VAMPIRES_SERVER_HOME="${HOME}/vampires-akka/server/"

${VAMPIRES_SERVER_HOME}/bin/server "$@"