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
