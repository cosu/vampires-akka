#!/bin/bash

export HOST=$(curl http://169.254.169.254/latest/meta-data/public-ipv4)
export BIND_HOST=$(curl http://169.254.169.254/latest/meta-data/local-ipv4)
export HOME=/home/ubuntu
export SERVER_IP=$1
shift
export KAMON_SIGAR_FOLDER=/tmp

${HOME}/vampires-akka-dist/client-1.0-SNAPSHOT/bin/client "$@"
