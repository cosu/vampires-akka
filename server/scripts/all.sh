#!/usr/bin/env bash

set -o nounset

source $(dirname $0)/common.sh

##create a configuration
config_id=$(
${curl} --request POST \
--header "Content-Type: application/json" \
--data-binary "{
\"description\" : \"my optional description\",
\"resources\":
[
    {
        \"resource_description\":
        {
            \"provider\": \"local\",
            \"type\": \"local\"
        },
        \"count\": \"1\"
    }
]
}" \
${api_server}/configurations | jq -r '.id')


#\"task\": \"wget -O /dev/null http://speedtest.wdc01.softlayer.com/downloads/test10.zip\",
#create workload
workload_id=$(
${curl} --request POST \
    --header "Content-Type: application/json" \
    --data-binary "{
    \"sequence_start\": \"0\",
    \"sequence_stop\": \"9\",
    \"task\": \"ping -c 10 google.com\",


    \"description\": \"a new description\"
}" \
${api_server}/workloads | jq -r '.id')

echo configuration ${config_id}
echo workload ${workload_id}

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
