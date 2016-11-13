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
        \"count\": \"2\"
    }
]
}" \
${api_server}/configurations | jq '.id')
