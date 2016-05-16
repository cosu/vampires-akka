#!/usr/bin/env bash

api_server="http://localhost:4567"

states="running starting canceled finished"


execs=$(curl -s ${api_server}/executions)
for state in ${states}; do
    echo "===>"${state}
    echo $(curl -s ${api_server}/executions | jq ".[] | select(.info.status | contains(\"${state}\"))") | jq -r '.id'
done