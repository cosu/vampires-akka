#!/usr/bin/env bash

set -o nounset

source ./common.sh

if [[ $# -ne 1 ]]; then
    echo "provide exec id"
    exit 1
fi

${curl} --request DELETE ${api_server}'/executions/'$1

${curl} ${api_server}/executions/$1