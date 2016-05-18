#!/usr/bin/env bash

set -o nounset

source ./common.sh

${curl} ${api_server}/configurations
