#!/usr/bin/env bash

IP=`curl -s http://ip.cosu.ro/?ip |  tr -d '[[:space:]]'`
export BIND_HOST="0.0.0.0"
export HOST=${IP}
export SERVER_OPTS="-Dconfig.file=$1"
shift
echo "Vampires-Akka server: Binding to IP:${BIND_HOST} . Public host: ${HOST}"

VAMPIRES_SERVER_HOME="${HOME}/vampires-akka/server/"

${VAMPIRES_SERVER_HOME}/bin/server "$@"
