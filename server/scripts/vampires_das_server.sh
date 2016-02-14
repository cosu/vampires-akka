#!/usr/bin/env bash

IP=$(ip -f inet addr show ib0 |grep inet |awk '{print $2}'|cut -f1 -d/)

export SERVER_OPTS="-Dconfig.file=$1"
export BIND_HOST="0.0.0.0"
export HOST=${IP}
shift
echo "Vampires-Akka server: Binding to IP:${BIND_HOST} . Public host: ${HOST}"

${HOME}/vampires-akka/server/bin/server "$@"

