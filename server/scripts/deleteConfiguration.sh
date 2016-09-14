#!/usr/bin/env bash


source $(dirname $0)/common.sh

if [[ $# -ne 1 ]]; then
    echo "provide configuration id"
    exit 1
fi

id=$1

${curl} --request DELETE  ${api_server}/configurations/${id}
