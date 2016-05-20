#!/usr/bin/env bash

set -o nounset

source $(dirname $0)/common.sh

#create workload
workload_id=$(
${curl} --request POST \
    --header "Content-Type: application/json" \
    --data-binary "{
    \"sequence_start\": \"0\",
    \"sequence_stop\": \"99\",
    \"task\": \"sleep 4\",
    \"description\": \"a new description\"
}" \
${api_server}/workloads | jq '.id')
echo ${workload_id}