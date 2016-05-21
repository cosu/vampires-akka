#!/usr/bin/env bash

set -o nounset

source $(dirname $0)/common.sh


if [[ $# -ne 2 ]]; then
    echo "provide workload id and config_id"
    echo "Usage: $0 <workload_id> <config_id>"
    exit 1
fi


workload_id=$1
config_id=$2

#execute
execution_id=$(
${curl} --request POST \
     --header "Content-Type: application/json" \
     --data-binary "{
    \"configuration\": \"$config_id\",
    \"workload\": \"$workload_id\",
    \"type\" : \"full\"
}" \
${api_server}/executions |jq -r '.id')

echo execution ${execution_id}
