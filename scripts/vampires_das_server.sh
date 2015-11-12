#!/usr/bin/env bash

export SERVER_OPTS="-Dconfig.file=$1"
shift
${HOME}/vampires-akka/server/bin/server "$@"

