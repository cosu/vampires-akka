#!/usr/bin/env bash

execution_id=$(curl -s localhost:4567/executions |jq '.[0] .id')
curl -s localhost:4567/executions/${execution_id}


