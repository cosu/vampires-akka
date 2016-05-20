#!/usr/bin/env bash

source $(dirname $0)/common.sh

if [[ $# -ne 1 ]]; then
    echo "provide file"
    exit 1
fi

file=$1


if [[ ! -f ${file} ]]; then
    echo "file ${file} does not exist"
    exit 1
fi


${curl} -F file=@${file} ${api_server}/upload

