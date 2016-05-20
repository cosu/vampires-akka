#!/usr/bin/env bash

set -ev
# run tests
./gradlew check --info


 install the client locally
./gradlew installDist

# run the server

VAMPIRES_HOME=server/build/install/server
export VAMPIRES_CLIENT="client/build/install/client/scripts/vampires.sh 127.0.0.1"

${VAMPIRES_HOME}/scripts/vampires_server.sh conf/local.conf local&


sleep 5

exec_id=$(./server/scripts/startExec.sh  |grep execution | awk ' {print $2}')

./server/scripts/monitorExec.sh ${exec_id}

status=$(./server/scripts/getStatus.sh ${exec_id})


if [[ ${status} == "finished" ]]; then
    echo "Success!"
    jps | grep Server | awk '{print $1}' | xargs kill -SIGINT
    exit 0
else
    jps | grep Server | awk '{print $1}' | xargs kill

    echo "Failed!"
    exit 1
fi