#!/usr/bin/env bash

execution_id=$(curl -s localhost:4567/executions |jq -r '.[-1] .id')
echo ${execution_id}
curl -s localhost:4567/executions/${execution_id}


