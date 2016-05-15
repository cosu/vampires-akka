#!/bin/bash -
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
        \"count\": \"1\"
    }
]
}" \
${api_server}/configurations | jq '.id')


#select the first workload
workload_id=$(curl -s localhost:4567/workloads |jq '.[0] .id')
echo ${config_id}
echo ${workload_id}


execution_id=$(curl -s \
     --request POST \
     --header "Content-Type: application/json" \
     --data-binary "{
    \"configuration\": $config_id,
    \"workload\": $workload_id,
    \"type\" : \"sample\"
}" \
${api_server}/executions |jq '.id')

echo "started $execution_id"