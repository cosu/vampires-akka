#!/usr/bin/env bash

set -o nounset

source $(dirname $0)/common.sh

if [[ $# -ne 1 ]]; then
    echo "provide exec id"
    exit 1
fi

execution_id=$1

status="running"
while [[ ${status} == "running" || ${status} == "starting" ]];  do
    exec=$( ${curl} ${api_server}/executions/${execution_id})
    completed=$(echo ${exec}| jq -r '.info.completed')
    echo completed ${completed} status ${status}
    status=$(echo ${exec}| jq -r '.info.status')
    sleep 0.1
done

${curl} ${api_server}/executions/${execution_id}
