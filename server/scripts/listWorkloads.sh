#!/usr/bin/env bash

set -o nounset

source $(dirname $0)/common.sh

${curl} ${api_server}/workloads