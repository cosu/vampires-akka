#!/usr/bin/env bash

set -o nounset


source $(dirname $0)/common.sh

states="running starting canceled finished failed"

execs=$(${curl} ${api_server}/executions)
for state in ${states}; do
    echo "===>"${state}
    echo $(echo ${execs}| jq ".[] | select(.info.status | contains(\"${state}\"))") \
        | jq -j ' ["id", .id , "duration", .info.elapsed|tostring] |join(" ") + "\n"'
    echo
done
