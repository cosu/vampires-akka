#!/usr/bin/env bash

export SERVER_OPTS="-Dconfig.file=$1"
export SERVER_IP=$(ip -f inet addr show ib0 |grep inet |awk '{print $2}'|cut -f1 -d/)
shift
${HOME}/vampires-akka/server/bin/server "$@"

