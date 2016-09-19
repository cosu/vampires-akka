#!/bin/bash - 
set -o nounset                              

source $(dirname $0)/common.sh


function help_message {
    echo "$0 -p PROVIDER -t TYPE -c COUNT (-m)"
}

if [ $# -eq 0 ];
then
    help_message
    exit 0
fi

MONITORING=false

while getopts ":p:t:c:n:j:m" opt; do
    case $opt in
        p)
            echo "Provider: $OPTARG" >&2
            PROVIDER=$OPTARG
            ;;
        t)
            echo "Type: $OPTARG" >&2
            TYPE=$OPTARG
            ;;
        c)
            echo "count: $OPTARG" >&2
            COUNT=$OPTARG
            ;;
        n)
            echo "Number of jobs: $OPTARG" >&2
            NUMBER_JOBS=$OPTARG
            ;;
        j)
            echo "Job: $OPTARG" >&2
            JOB=$OPTARG
            ;;
        m)
            echo "Monitoring: True" >&2
            MONITORING=true
            ;;
        \?)
            echo "Invalid option: -$OPTARG" >&2
            exit 1
            ;;
        :)
            echo "Option -$OPTARG requires an argument." >&2
            exit 1
            ;;
    esac
done

config_id=$(create_config ${PROVIDER} ${TYPE} ${COUNT})
workload_id=$(create_workload ${NUMBER_JOBS} "${JOB}")

execution_id=$(create_execution ${config_id} ${workload_id})

if [ ${MONITORING} = true ]; then
    monitor ${execution_id}
else
    echo ${execution_id}
fi
