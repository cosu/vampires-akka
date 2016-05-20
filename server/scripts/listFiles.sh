#!/usr/bin/env bash

source $(dirname $0)/common.sh

${curl}  ${api_server}/upload
