#!/usr/bin/env bash


source $(dirname $0)/common.sh

if [[ $# -ne 1 ]]; then
    echo "provide exec id"
    exit 1
fi

execution_id=$1

${curl} ${api_server}/executions/${execution_id}| jq -r '.info.status'