#!/usr/bin/env bash

set -o nounset

api_server="http://localhost:4567"

#create a configuration
config_id=$(curl -s --request POST \
--header "Content-Type: application/json" \
--data-binary "{
\"description\" : \"my optional description\",
\"resources\":
[
    {
        \"provider\": \"local\",
        \"type\": \"local\",
        \"count\": \"2\"
    }
]
}" \
${api_server}/configurations | jq '.id')


#select the first workload
workload_id=$(curl -s localhost:4567/workloads |jq '.[0] .id')

#execute
execution_id=$(curl -s \
     --request POST \
     --header "Content-Type: application/json" \
     --data-binary "{
    \"configuration\": $config_id,
    \"workload\": $workload_id,
    \"type\" : \"sample\"
}" \
${api_server}/executions |jq -r '.id')

echo "started $execution_id"

status=""
while [[ ${status} != "finished" ]];  do
    exec=$(curl -s localhost:4567/executions/${execution_id})
    completed=$(echo $exec| jq -r '.info.completed')
    echo completed ${completed}
    status=$(echo ${exec}| jq -r '.info.status')
    sleep 0.1
done

curl -s localhost:4567/executions/${execution_id}
