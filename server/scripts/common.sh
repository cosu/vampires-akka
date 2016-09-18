#!/usr/bin/env bash

user="admin"
curl="curl -s -u ${user}:${user}"
api_server="http://localhost:4567"


function create_config {
    local provider=$1
    local type=$2
    local count=$3

    local config_id=$(
    ${curl} --request POST \
            --header "Content-Type: application/json" \
            --data-binary "{
    \"description\" : \"my optional description\",
    \"resources\":
    [
    {
        \"resource_description\":
        {
            \"provider\": \"${provider}\",
            \"type\": \"${type}\"
        },
        \"count\": \"${count}\"
    }
    ]
}" \
    ${api_server}/configurations | jq -r '.id')
    echo ${config_id}
}

function create_workload {
    local count=$(($1-1))
    local task="$2"
    local workload_id=$(
    ${curl} --request POST \
            --header "Content-Type: application/json" \
            --data-binary "{
    \"sequence_start\": \"0\",
    \"sequence_stop\": \"$count\",
    \"task\": \"$task\",
    \"description\": \"a new description\"
}" \
    ${api_server}/workloads | jq -r '.id')
    
    echo ${workload_id}
}

function create_execution {
    local config_id=$1
    local workload_id=$2

    local execution_id=$(
    ${curl} --request POST \
        --header "Content-Type: application/json" \
        --data-binary "{
    \"configuration\": \"${config_id}\",
    \"workload\": \"${workload_id}\",
    \"type\" : \"full\"
}" \
    ${api_server}/executions |jq -r '.id')

    echo ${execution_id}
}


function list_workloads {
    ${curl} ${api_server}/workloads
}

function list_configurations {
    ${curl} ${api_server}/configurations
}

function list_providers {
    ${curl} ${api_server}/providers
}


function delete_configuration {
    local id=$1
    ${curl} --request DELETE  ${api_server}/configurations/${id}
}

function delete_workload {
    local id=$1
    ${curl} --request DELETE  ${api_server}/workloads/${id}
}

function get_configuration {
    local id=$1
    ${curl} ${api_server}/configurations/${id}
}

function get_workload {
    local id=$1
    ${curl} ${api_server}/workloads/${id}
}


function get_execution {
    local id=$1
    ${curl} ${api_server}/executions/${id}
}

function list_files {
    ${curl} ${api_server}/upload
}

function _log {
    echo $(date -u +"%Y-%m-%dT%H:%M:%SZ") $1
}

function stop_execution {
    local id=$1
    ${curl} --request DELETE ${api_server}/executions/${id}
}

function monitor {
    local id=$1
    local status="starting"
    local previous_message=""
    while [[ ${status} == "running" || ${status} == "starting" ]];  do
        exec=$( ${curl} ${api_server}/executions/${id})
        completed=$(echo ${exec}| jq -r '.info.completed')
        remaining=$(echo ${exec}| jq -r '.info.remaining')
        status=$(echo ${exec}| jq -r '.info.status')
        message="status: ${status^^} jobs: completed=${completed} remaining=${remaining}"
        if [[ ${message} != ${previous_message} ]]; then
            _log "${message}"
            previous_message=${message}
        fi
        sleep 0.1
    done
}
