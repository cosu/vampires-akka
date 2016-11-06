#!/usr/bin/env bash

set -o nounset

source $(dirname $0)/common.sh

#create workload
workload_id=$(
${curl} --request POST \
    --header "Content-Type: application/json" \
    --data-binary "{
    \"file\": \"sleep 1\nsleep 2\nsleep 3\",
    \"description\": \"a new description\"
}" \
${api_server}/workloads | jq '.id')
echo ${workload_id}