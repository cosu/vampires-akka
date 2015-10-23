#!/bin/bash

export BIND_IP=$(curl http://169.254.169.254/latest/meta-data/public-ipv4)
export BIND_HOSTNAME=$(curl http://169.254.169.254/latest/meta-data/local-ipv4)
export SERVER_IP=$1
export HOME=/home/ubuntu
shift

/home/ubuntu/vampires-akka/client/build/install/client/bin/client "$@"
