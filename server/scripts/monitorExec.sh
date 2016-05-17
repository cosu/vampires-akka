#!/usr/bin/env bash

set -o nounset


api_server="http://localhost:4567"

if [[ $# -ne 1 ]]; then
    echo "provide exec id"
    exit 1
fi

execution_id=$1

status="running"
while [[ ${status} == "running" || ${status} == "starting" ]];  do
    exec=$(curl -s localhost:4567/executions/${execution_id})
    completed=$(echo $exec| jq -r '.info.completed')
    echo completed ${completed} status ${status}
    status=$(echo ${exec}| jq -r '.info.status')
    sleep 0.1
done

curl -s localhost:4567/executions/${execution_id}
