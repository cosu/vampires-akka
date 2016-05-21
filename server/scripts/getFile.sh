#!/usr/bin/env bash


source $(dirname $0)/common.sh

if [[ $# -ne 1 ]]; then
    echo "provide file   id"
    exit 1
fi

file_id=$1

${curl} ${api_server}/upload/${file_id}