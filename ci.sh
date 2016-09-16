#!/usr/bin/env bash

set -ev
# run tests
./gradlew check --parallel --parallel-threads=4


#nstall the client locally
./gradlew installDist 

./gradlew integrationTest

# run the server

VAMPIRES_HOME=server/build/install/server
export VAMPIRES_CLIENT="client/build/install/client/scripts/vampires.sh 127.0.0.1"

${VAMPIRES_HOME}/scripts/vampires_server.sh conf/local.conf local&


sleep 5

exec_id=$(./server/scripts/all.sh  |grep execution | awk ' {print $2}')

./server/scripts/monitorExec.sh ${exec_id}

job_status=$(./server/scripts/getStatus.sh ${exec_id})


jps | grep Server | awk '{print $1}' | xargs kill


echo "http://cosu.github.io/vampires-akka/server/tests/test/"
echo "http://cosu.github.io/vampires-akka/client/tests/test/"
echo "http://cosu.github.io/vampires-akka/server/tests/integrationTest/"
echo "http://cosu.github.io/vampires-akka/server/jacoco/test/html/"
echo "http://cosu.github.io/vampires-akka/client/jacoco/test/html/"

if [[ ${job_status} == "finished" ]]; then
    echo "Success!"
    exit 0
else
    echo "Failed!"
    exit 1
fi
