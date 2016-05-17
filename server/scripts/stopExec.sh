#!/usr/bin/env bash

set -o nounset

api_server="http://localhost:4567"

if [[ $# -ne 1 ]]; then
    echo "provide exec id"
    exit 1
fi

curl -s --request DELETE \
${api_server}'/executions/'$1

curl -s ${api_server}/executions/$1