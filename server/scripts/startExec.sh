#!/usr/bin/env bash

set -o nounset

api_server="http://localhost:4567"

##create a configuration
config_id=$(
curl -s --request POST \
--header "Content-Type: application/json" \
--data-binary "{
\"description\" : \"my optional description\",
\"resources\":
[
    {
        \"provider\": \"local\",
        \"type\": \"local\",
        \"count\": \"1\"
    }
]
}" \
${api_server}/configurations | jq '.id')

#create workload
workload_id=$(
curl -s --request POST \
    --header "Content-Type: application/json" \
    --data-binary "{
    \"sequence_start\": \"0\",
    \"sequence_stop\": \"99\",
    \"task\": \"sleep 4\",
    \"description\": \"a new description\"
}" \
${api_server}/workloads | jq '.id')

echo ${config_id} ${workload_id}

#execute
execution_id=$(curl -s \
     --request POST \
     --header "Content-Type: application/json" \
     --data-binary "{
    \"configuration\": $config_id,
    \"workload\": $workload_id,
    \"type\" : \"full\"
}" \
${api_server}/executions |jq -r '.id')

echo ${execution_id}